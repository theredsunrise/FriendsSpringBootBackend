package com.example.demo.user.application.port.out.repository;

import com.example.demo.user.domain.User;
import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    void deleteById(Long userId);

    boolean existsByUsername(String username);

    boolean existsById(Long userId);

    Optional<User> getById(Long userId);

    PageResult<User> findAll(Page page);
}
