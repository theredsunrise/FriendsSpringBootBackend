package com.example.demo.friendship.application.port.out.repository;

import com.example.demo.friendship.domain.Friendship;
import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.user.domain.User;

import java.util.UUID;

public interface FriendshipRepository {

    Friendship save(Friendship friendship);

    boolean existsByUserIdAndFriendId(UUID userId, UUID friendId);

    void deleteByUserIdAndFriendId(UUID userId, UUID friendId);

    PageResult<Friendship> findAll(Page page);

    PageResult<User> findAllFriends(UUID userId, Page page);

    void invalidateCache(UUID userId);
}
