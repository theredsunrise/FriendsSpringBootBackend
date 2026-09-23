package com.example.demo.user.application.port.in;

import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.user.domain.User;


public interface UserUseCase {

    User create(User user);

    void delete(Long userId);

    User getById(Long userId);

    PageResult<User> findAll(Page page);
}
