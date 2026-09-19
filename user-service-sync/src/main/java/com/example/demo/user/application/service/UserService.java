package com.example.demo.user.application.service;

import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.user.application.exception.UserAlreadyExistsException;
import com.example.demo.user.application.exception.UserNotFoundException;
import com.example.demo.user.application.port.in.UserUseCase;
import com.example.demo.user.application.port.out.repository.UserRepository;
import com.example.demo.user.domain.User;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserRepository userRepository;

    @Override
    public User create(User user) {
        String username = user.getUsername();
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("User with username: %s already exists.".formatted(username));
        }
        return userRepository.save(user);
    }

    @Override
    public void delete(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(String.format("User with ID: %s was not found.", userId));
        }
        userRepository.deleteById(userId);
    }

    @Override
    public PageResult<User> findAll(Page page) {
        return userRepository.findAll(page);
    }
}
