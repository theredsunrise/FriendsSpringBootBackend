package com.example.demo.user.application.port.out.repository;

import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.user.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    boolean existsByUsername(String username);

    boolean existsById(UUID userId);

    Optional<User> getById(UUID userId);

    void deleteById(UUID userId);

    PageResult<User> findAll(Page page);
}
