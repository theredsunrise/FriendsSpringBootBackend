package com.example.demo.friendship.application.port.out.repository;

import com.example.demo.friendship.domain.Friendship;
import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.user.domain.User;


public interface FriendshipRepository {

    Friendship save(Friendship friendship);

    void deleteByUserIdAndFriendId(Long userId, Long friendId);

    boolean existsByUserIdAndFriendId(Long userId, Long friendId);

    PageResult<Friendship> findAll(Page page);

    PageResult<User> findAllFriends(Long userId, Page page);

    void invalidateCache(Long userId);
}
