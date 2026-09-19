package com.example.demo.friendship.application.service;

import com.example.demo.friendship.application.exception.FriendshipAlreadyExistsException;
import com.example.demo.friendship.application.port.in.FriendshipUseCase;
import com.example.demo.friendship.application.port.out.repository.FriendshipRepository;
import com.example.demo.friendship.domain.Friendship;
import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.shared.infrastructure.out.event.IntegrationEvent;
import com.example.demo.shared.infrastructure.out.event.UserDeletedIntegrationEvent;
import com.example.demo.user.application.exception.UserNotFoundException;
import com.example.demo.user.application.port.out.repository.UserRepository;
import com.example.demo.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class FriendshipService implements FriendshipUseCase {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    @Override
    public void handleIntegrationEvent(IntegrationEvent integrationEvent) {
        switch (integrationEvent) {
            case UserDeletedIntegrationEvent ev -> friendshipRepository.invalidateCache(ev.userId());
        }
    }

    @Override
    public Friendship add(Friendship friendship) {
        UUID userId = friendship.getUserId();
        UUID friendId = friendship.getFriendId();
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with ID: %s does not exist.".formatted(userId));
        }
        if (!userRepository.existsById(friendId)) {
            throw new UserNotFoundException("Friend with ID: %s does not exist.".formatted(friendId));
        }
        if (friendshipRepository.existsByUserIdAndFriendId(userId, friendId)) {
            throw new FriendshipAlreadyExistsException("Friend with ID: %s is already a friend with: %s.".formatted(friendId, userId));
        }
        return friendshipRepository.save(friendship);
    }

    @Override
    public void deleteFromUser(UUID userId, UUID friendId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with ID: %s does not exist.".formatted(userId));
        }
        if (!userRepository.existsById(friendId)) {
            throw new UserNotFoundException("Friend with ID: %s does not exist.".formatted(friendId));
        }
        friendshipRepository.deleteByUserIdAndFriendId(userId, friendId);
    }

    @Override
    public PageResult<Friendship> findAll(Page page) {
        return friendshipRepository.findAll(page);
    }

    @Override
    public PageResult<User> findAllFriends(UUID userId, Page page) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(String.format("User with ID: %s was not found.", userId));
        }
        return friendshipRepository.findAllFriends(userId, page);
    }
}
