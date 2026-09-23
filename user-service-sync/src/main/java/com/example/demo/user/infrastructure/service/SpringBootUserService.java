package com.example.demo.user.infrastructure.service;

import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.shared.infrastructure.out.event.UserDeletedIntegrationEvent;
import com.example.demo.user.application.port.in.UserUseCase;
import com.example.demo.user.application.port.out.repository.UserRepository;
import com.example.demo.user.application.service.UserService;
import com.example.demo.user.domain.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class SpringBootUserService extends UserService implements UserUseCase {

    private final ApplicationEventPublisher eventPublisher;

    public SpringBootUserService(
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher) {
        super(userRepository);
        this.eventPublisher = eventPublisher;
    }

    @Override
    public User create(User user) {
        return super.create(user);
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        super.delete(userId);
        eventPublisher.publishEvent(new UserDeletedIntegrationEvent(userId));
    }

    @Override
    public PageResult<User> findAll(Page page) {
        return super.findAll(page);
    }
}
