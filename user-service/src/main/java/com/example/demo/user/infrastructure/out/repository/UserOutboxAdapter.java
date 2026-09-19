package com.example.demo.user.infrastructure.out.repository;

import com.example.demo.shared.domain.OutBoxEventType;
import com.example.demo.shared.domain.OutBoxStatus;
import com.example.demo.shared.infrastructure.out.openTelemetry.ObservationPredicateConfig;
import com.example.demo.user.application.port.out.repository.UserOutboxRepository;
import com.example.demo.user.domain.User;
import com.example.demo.user.infrastructure.out.repository.jpa.SpringDataUserOutboxRepository;
import com.example.demo.user.infrastructure.out.repository.jpa.dto.UserOutboxJpaEntity;
import com.example.demo.user.infrastructure.out.repository.jpa.maper.UserOutboxJpaMapper;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserOutboxAdapter implements UserOutboxRepository {

    private final SpringDataUserOutboxRepository repository;
    private final UserOutboxJpaMapper mapper;

    @Override
    @Observed(name = "user-outbox-save", contextualName = "user-outbox-save",
            lowCardinalityKeyValues = {ObservationPredicateConfig.SKIP_DB_KEY, "true"})
    public User save(
            String eventGroup,
            String eventsTopic,
            OutBoxEventType eventType,
            OutBoxStatus eventStatus,
            User user) {
        try {
            UserOutboxJpaEntity dto = mapper.toJpaEntity(
                    eventGroup,
                    eventsTopic,
                    eventType,
                    eventStatus,
                    user);
            repository.save(dto);
            return user;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Observed(name = "user-outbox-delete-completed", contextualName = "user-outbox-delete-completed")
    public int deleteAllCompleted() {
        return repository.deleteByEventStatus(OutBoxStatus.COMPLETED);
    }

    @Override
    @Observed(name = "user-outbox-update", contextualName = "user-outbox-update")
    public int updateStatus(Long id, OutBoxStatus status) {
        return repository.updateStatus(id, status);
    }
}
