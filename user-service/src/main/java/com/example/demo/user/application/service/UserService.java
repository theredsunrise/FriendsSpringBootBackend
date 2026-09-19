package com.example.demo.user.application.service;

import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.shared.domain.OutBoxEventType;
import com.example.demo.shared.domain.OutBoxStatus;
import com.example.demo.user.application.exception.UserAlreadyExistsException;
import com.example.demo.user.application.exception.UserNotFoundException;
import com.example.demo.user.application.port.in.UserUseCase;
import com.example.demo.user.application.port.out.repository.UserOutboxRepository;
import com.example.demo.user.application.port.out.repository.UserRepository;
import com.example.demo.user.domain.User;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

//test dorobit lepsie logovanie
//test mongo url ma password
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private static final String EVENT_GROUP = "USER";

    private final String eventsTopic;
    private final UserRepository userRepository;
    private final UserOutboxRepository userOutboxRepository;

    @Override
    public User create(User user) {
        String username = user.getUsername();
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("User with username: %s already exists.".formatted(username));
        }

        User newUser = userRepository.save(user);
        userOutboxRepository.save(
                EVENT_GROUP,
                eventsTopic,
                OutBoxEventType.CREATED,
                OutBoxStatus.PENDING,
                newUser);
        return newUser;
    }

    @Override
    public void delete(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(String.format("User with ID: %s was not found.", userId));
        }
        userRepository.deleteById(userId);

        User deletedUser = User.builder()
                .id(userId)
                .name("None")
                .surname("None")
                .username("None")
                .birthDate(LocalDate.now())
                .residence("None")
                .build();

        userOutboxRepository.save(
                EVENT_GROUP,
                eventsTopic,
                OutBoxEventType.DELETED,
                OutBoxStatus.PENDING,
                deletedUser);
    }

    @Override
    public User getById(UUID userId) {
        return userRepository.getById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User with ID: %s was not found.", userId))
        );
    }

    @Override
    public PageResult<User> findAll(Page page) {
        return userRepository.findAll(page);
    }
}
