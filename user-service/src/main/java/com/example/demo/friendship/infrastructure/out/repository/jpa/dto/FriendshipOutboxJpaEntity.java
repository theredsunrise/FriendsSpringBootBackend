package com.example.demo.friendship.infrastructure.out.repository.jpa.dto;

import com.example.demo.shared.domain.OutBoxEventType;
import com.example.demo.shared.domain.OutBoxStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friendships_outbox")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FriendshipOutboxJpaEntity {

    @Id
    @EqualsAndHashCode.Include
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
}
