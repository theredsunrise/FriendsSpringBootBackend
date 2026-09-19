package com.example.demo.shared.infrastructure.out.openTelemetry;

import com.mongodb.observability.micrometer.MongodbObservationContext;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationPredicate;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.ObservationView;
import net.ttddyy.observation.tracing.ConnectionContext;
import net.ttddyy.observation.tracing.QueryContext;
import net.ttddyy.observation.tracing.ResultSetContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.server.observation.ServerRequestObservationContext;

import java.util.List;

@Configuration
public class ObservationPredicateConfig {

    public static final String SKIP_DB_KEY = "skipDB";

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/actuator/health"
    );

    private static final List<String> EXCLUDED_OBSERVATIONS = List.of(
            "mongodb.transaction",
            "mongodb.command"
    );

    @Bean
    public ObservationPredicate excludeActuatorHealth(@Lazy ObservationRegistry observationRegistry) {
        return (name, context) -> {
            if (EXCLUDED_OBSERVATIONS.stream().anyMatch(name::equals)) {
                return false;
            }

            if (isDatabaseContext(context)) {
                if (shouldSkipDatabase(context)) {
                    return false;
                }

                Observation currentObservation = observationRegistry.getCurrentObservation();
                return currentObservation == null || !shouldSkipDatabase(currentObservation);
            }

            if (context instanceof ServerRequestObservationContext serverContext
                    && serverContext.getCarrier() != null) {
                var path = serverContext.getCarrier().getRequestURI();
                return EXCLUDED_PATHS.stream().noneMatch(path::contains);
            }

            if (context instanceof ClientRequestObservationContext clientContext
                    && clientContext.getCarrier() != null) {
                var path = clientContext.getCarrier().getURI().getPath();
                return EXCLUDED_PATHS.stream().noneMatch(path::contains);
            }
            return true;
        };
    }

    private boolean isDatabaseContext(Observation.Context context) {
        return context instanceof ConnectionContext
                || context instanceof QueryContext
                || context instanceof ResultSetContext
                || context instanceof MongodbObservationContext;
    }

    private boolean shouldSkipDatabase(Observation.Context context) {
        return context.getLowCardinalityKeyValue(SKIP_DB_KEY) != null;
    }

    private boolean shouldSkipDatabase(ObservationView observation) {
        if (observation == null) {
            return false;
        }

        var context = observation.getContextView();
        if (context.getLowCardinalityKeyValue(SKIP_DB_KEY) != null) {
            return true;
        }

        return shouldSkipDatabase(context.getParentObservation());
    }
}
