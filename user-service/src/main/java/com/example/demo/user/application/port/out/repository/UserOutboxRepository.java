package com.example.demo.user.application.port.out.repository;

import com.example.demo.shared.domain.OutBoxEventType;
import com.example.demo.shared.domain.OutBoxStatus;
import com.example.demo.user.domain.User;

public interface UserOutboxRepository {
    User save(
            String eventGroup,
            String eventsTopic,
            OutBoxEventType eventType,
            OutBoxStatus eventStatus,
            User user);

    int deleteAllCompleted();

    int updateStatus(Long id, OutBoxStatus status);
}
