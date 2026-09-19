package com.example.demo.shared.infrastructure.out.openTelemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenTelemetryHelper {
    public static final String TRACE_PARENT = "traceparent";
    private final OpenTelemetry openTelemetry;

    public String getTraceParent() throws IOException {
        Map<String, Object> openTelemetryMap = new HashMap<>();
        openTelemetry.getPropagators().getTextMapPropagator().inject(
                Context.current(),
                openTelemetryMap,
                Map::put);

        var tracingContext = openTelemetryMap.getOrDefault(TRACE_PARENT, "").toString();
        log.debug("Tracing context : {}", tracingContext);

        return tracingContext;
    }

    public String getTraceFromProps() throws IOException {
        Map<String, Object> openTelemetryMap = new HashMap<>();
        openTelemetry.getPropagators().getTextMapPropagator().inject(
                Context.current(),
                openTelemetryMap,
                Map::put);

        try (var writer = new StringWriter()) {
            Properties props = new Properties();
            props.putAll(openTelemetryMap);
            props.store(writer, null);

            var tracingContext = writer.toString();
            log.debug("Tracing context : {}", tracingContext);

            return tracingContext;
        }
    }

    public static void setMyTag(String tagValue) {
        Span.current().setAttribute("my.tag", tagValue);
    }
}
