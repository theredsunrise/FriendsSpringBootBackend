package com.example.demo.user.application.port.out.repository;

import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.user.domain.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    boolean existsByUsername(String username);

    boolean existsById(Long userId);

    Optional<User> getById(Long userId);

    void deleteById(Long userId);

    PageResult<User> findAll(Page page);
}
