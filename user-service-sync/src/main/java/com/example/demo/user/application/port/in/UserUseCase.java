package com.example.demo.user.application.port.in;

import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.user.domain.User;

import java.util.UUID;

public interface UserUseCase {
    User create(User user);

    void delete(UUID userId);

    PageResult<User> findAll(Page page);
}
