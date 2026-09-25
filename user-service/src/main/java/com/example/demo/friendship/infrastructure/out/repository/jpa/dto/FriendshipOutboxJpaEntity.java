package com.example.demo.friendship.infrastructure.out.repository.jpa.dto;

import com.example.demo.shared.domain.OutBoxEventType;
import com.example.demo.shared.domain.OutBoxStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

@Entity
@Table(name = "friendships_outbox")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipOutboxJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_topic", nullable = false, length = 50)
    private String eventTopic;

    @Column(name = "event_id", nullable = false, length = 50)
    private String eventId;

    @Column(name = "event_payload", nullable = false, columnDefinition = "TEXT")
    private String eventPayload;

    @Column(name = "tracingspancontext", nullable = false)
    private String tracingSpanContext;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private OutBoxEventType eventType;

    @Column(name = "event_group", nullable = false, length = 20)
    private String eventGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status", nullable = false, length = 20)
    private OutBoxStatus eventStatus;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        FriendshipOutboxJpaEntity that = (FriendshipOutboxJpaEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
