package com.example.demo.friendship.infrastructure.service;

import com.example.demo.friendship.application.port.in.FriendshipUseCase;
import com.example.demo.friendship.application.port.out.repository.FriendshipRepository;
import com.example.demo.friendship.application.service.FriendshipService;
import com.example.demo.friendship.domain.Friendship;
import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.shared.infrastructure.out.event.UserDeletedIntegrationEvent;
import com.example.demo.user.application.port.out.repository.UserRepository;
import com.example.demo.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Service
public class SpringBootFriendshipService extends FriendshipService implements FriendshipUseCase {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserDeletedEvent(UserDeletedIntegrationEvent event) {
        handleIntegrationEvent(event);
    }

    public SpringBootFriendshipService(
            UserRepository userRepository,
            FriendshipRepository friendshipRepository) {
        super(userRepository, friendshipRepository);
    }

    @Override
    public Friendship add(Friendship friendship) {
        return super.add(friendship);
    }

    @Override
    public void deleteFromUser(UUID userId, UUID friendId) {
        super.deleteFromUser(userId, friendId);
    }

    @Override
    public PageResult<Friendship> findAll(Page page) {
        return super.findAll(page);
    }

    @Override
    public PageResult<User> findAllFriends(UUID userId, Page page) {
        return super.findAllFriends(userId, page);
    }
}
