package com.example.demo.friendship.application.service;

import com.example.demo.friendship.application.exception.FriendshipAlreadyExistsException;
import com.example.demo.friendship.application.port.in.FriendshipUseCase;
import com.example.demo.friendship.application.port.out.repository.FriendshipOutboxRepository;
import com.example.demo.friendship.application.port.out.repository.FriendshipRepository;
import com.example.demo.friendship.domain.Friendship;
import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.shared.domain.OutBoxEventType;
import com.example.demo.shared.domain.OutBoxStatus;
import com.example.demo.shared.infrastructure.out.event.IntegrationEvent;
import com.example.demo.shared.infrastructure.out.event.UserDeletedIntegrationEvent;
import com.example.demo.user.application.exception.UserNotFoundException;
import com.example.demo.user.application.port.out.repository.UserRepository;
import com.example.demo.user.domain.User;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;


@RequiredArgsConstructor
public class FriendshipService implements FriendshipUseCase {

    private static final String EVENT_GROUP = "FRIENDSHIP";

    private final String eventsTopic;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendshipOutboxRepository friendshipOutboxRepository;

    @Override
    public void handleIntegrationEvent(IntegrationEvent integrationEvent) {
        switch (integrationEvent) {
            case UserDeletedIntegrationEvent ev -> friendshipRepository.invalidateCache(ev.userId());
        }
    }

    @Override
    public void addToUser(Long userId, Long friendId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with ID: %s does not exist.".formatted(userId));
        }
        if (!userRepository.existsById(friendId)) {
            throw new UserNotFoundException("Friend with ID: %s does not exist.".formatted(friendId));
        }

        if (friendshipRepository.existsByUserIdAndFriendId(userId, friendId)) {
            throw new FriendshipAlreadyExistsException("Friend with ID: %s is already a friend with: %s.".formatted(friendId, userId));
        }
        Friendship newFriendship = friendshipRepository.save(Friendship.builder()
                .userId(userId)
                .friendId(friendId)
                .build());
        friendshipOutboxRepository.save(
                EVENT_GROUP,
                eventsTopic,
                OutBoxEventType.CREATED,
                OutBoxStatus.PENDING,
                newFriendship);
    }

    @Override
    public void deleteFromUser(Long userId, Long friendId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with ID: %s does not exist.".formatted(userId));
        }
        if (!userRepository.existsById(friendId)) {
            throw new UserNotFoundException("Friend with ID: %s does not exist.".formatted(friendId));
        }
        friendshipRepository.deleteByUserIdAndFriendId(userId, friendId);

        Friendship deletedFriendship = Friendship.builder()
                .userId(userId)
                .friendId(friendId)
                .build();

        friendshipOutboxRepository.save(
                EVENT_GROUP,
                eventsTopic,
                OutBoxEventType.DELETED,
                OutBoxStatus.PENDING,
                deletedFriendship);
    }

    @Override
    public PageResult<Friendship> findAll(Page page) {
        return friendshipRepository.findAll(page);
    }

    @Override
    public PageResult<User> findAllFriends(Long userId, Page page) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(String.format("User with ID: %s was not found.", userId));
        }
        return friendshipRepository.findAllFriends(userId, page);
    }
}
