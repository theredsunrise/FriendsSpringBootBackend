package com.example.demo.friendship.application.port.in;

import com.example.demo.friendship.domain.Friendship;
import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.shared.infrastructure.out.event.IntegrationEvent;
import com.example.demo.user.domain.User;


public interface FriendshipUseCase {
    Friendship add(Friendship friendship);

    void deleteFromUser(Long userId, Long friendId);

    PageResult<Friendship> findAll(Page page);

    PageResult<User> findAllFriends(Long userId, Page page);

    void handleIntegrationEvent(IntegrationEvent integrationEvent);
}
