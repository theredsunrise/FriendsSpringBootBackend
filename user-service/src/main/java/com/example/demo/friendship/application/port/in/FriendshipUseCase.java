package com.example.demo.friendship.application.port.in;

import com.example.demo.friendship.domain.Friendship;
import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.shared.infrastructure.out.event.IntegrationEvent;
import com.example.demo.user.domain.User;

import java.util.UUID;

public interface FriendshipUseCase {

    void addToUser(UUID userId, UUID friendId);

    void deleteFromUser(UUID userId, UUID friendId);

    PageResult<Friendship> findAll(Page page);

    PageResult<User> findAllFriends(UUID userId, Page page);

    void handleIntegrationEvent(IntegrationEvent integrationEvent);
}
