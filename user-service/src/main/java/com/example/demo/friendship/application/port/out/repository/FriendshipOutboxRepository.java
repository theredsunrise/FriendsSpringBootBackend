package com.example.demo.friendship.application.port.out.repository;

import com.example.demo.friendship.domain.Friendship;
import com.example.demo.shared.domain.OutBoxEventType;
import com.example.demo.shared.domain.OutBoxStatus;

public interface FriendshipOutboxRepository {

    Friendship save(
            String eventGroup,
            String eventsTopic,
            OutBoxEventType eventType,
            OutBoxStatus eventStatus,
            Friendship friendship);

    int deleteAllCompleted();

    int updateStatus(Long id, OutBoxStatus status);

}
