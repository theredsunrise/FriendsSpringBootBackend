package com.example.demo.friendship.infrastructure.out.repository;

import com.example.demo.friendship.application.port.out.repository.FriendshipOutboxRepository;
import com.example.demo.friendship.domain.Friendship;
import com.example.demo.friendship.infrastructure.out.repository.jpa.dto.FriendshipOutboxJpaEntity;
import com.example.demo.friendship.infrastructure.out.repository.jpa.mapper.FriendshipOutboxJpaMapper;
import com.example.demo.shared.domain.OutBoxEventType;
import com.example.demo.shared.domain.OutBoxStatus;
import com.example.demo.shared.infrastructure.out.openTelemetry.ObservationPredicateConfig;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FriendshipOutboxAdapter implements FriendshipOutboxRepository {

    private final SpringDataFriendshipOutboxRepository repository;
    private final FriendshipOutboxJpaMapper mapper;

    @Override
    @Observed(name = "friendship-outbox-save", contextualName = "friendship-outbox-save",
            lowCardinalityKeyValues = {ObservationPredicateConfig.SKIP_DB_KEY, "true"})
    public Friendship save(
            String eventGroup,
            String eventsTopic,
            OutBoxEventType eventType,
            OutBoxStatus eventStatus,
            Friendship friendship) {
        try {
            FriendshipOutboxJpaEntity dto = mapper.toJpaEntity(
                    eventGroup,
                    eventsTopic,
                    eventType,
                    eventStatus,
                    friendship);
            repository.save(dto);
            return friendship;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Observed(name = "friendship-outbox-delete-completed", contextualName = "friendship-outbox-delete-completed")
    public int deleteAllCompleted() {
        return repository.deleteByEventStatus(OutBoxStatus.COMPLETED);
    }

    @Override
    @Observed(name = "friendship-outbox-update", contextualName = "friendship-outbox-update")
    public int updateStatus(Long id, OutBoxStatus status) {
        return repository.updateStatus(id, status);
    }
}
