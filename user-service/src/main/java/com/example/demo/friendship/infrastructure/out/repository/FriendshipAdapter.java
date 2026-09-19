package com.example.demo.friendship.infrastructure.out.repository;


import com.example.demo.friendship.application.port.out.repository.FriendshipRepository;
import com.example.demo.friendship.domain.Friendship;
import com.example.demo.friendship.infrastructure.out.repository.jpa.dto.FriendshipJpaEntity;
import com.example.demo.friendship.infrastructure.out.repository.jpa.dto.projection.UserWithFriendship;
import com.example.demo.friendship.infrastructure.out.repository.jpa.mapper.FriendshipJpaMapper;
import com.example.demo.friendship.infrastructure.out.repository.jpa.mapper.UserWithFriendshipMapper;
import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.shared.infrastructure.out.openTelemetry.ObservationPredicateConfig;
import com.example.demo.shared.infrastructure.out.repository.redis.RedisCachedTemplate;
import com.example.demo.user.domain.User;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FriendshipAdapter implements FriendshipRepository {

    private static final String FRIENDS_PAGES_KEY = "user-service::friend:pages:%s";
    private static final String FRIENDS_KEY = "user-service::friends:%s:%s:%d";

    private final RedisCachedTemplate redisTemplate;
    private final SpringDataFriendshipRepository repository;
    private final FriendshipJpaMapper mapper;
    private final UserWithFriendshipMapper friendshipMapper;

    @Override
    @Observed(name = "friendship-invalidate-cache", contextualName = "friendship-invalidate-cache")
    public void invalidateCache(UUID userId) {
        invalidate(FRIENDS_PAGES_KEY, userId);
    }

    @Override
    @Observed(name = "friendship-save", contextualName = "friendship-save",
            lowCardinalityKeyValues = {ObservationPredicateConfig.SKIP_DB_KEY, "true"})
    public Friendship save(Friendship friendship) {
        FriendshipJpaEntity dto = mapper.toJpaEntity(friendship);
        FriendshipJpaEntity savedEntity = repository.save(dto);
        var domainEntity = mapper.toDomain(savedEntity);

        invalidate(FRIENDS_PAGES_KEY, friendship.getUserId());
        invalidate(FRIENDS_PAGES_KEY, friendship.getFriendId());
        return domainEntity;
    }

    @Override
    @Observed(name = "friendship-delete-id", contextualName = "friendship-delete-id")
    public void deleteByUserIdAndFriendId(UUID userId, UUID friendId) {
        repository.deleteByUserIdAndFriendId(userId, friendId);
        invalidate(FRIENDS_PAGES_KEY, userId);
        invalidate(FRIENDS_PAGES_KEY, friendId);
    }

    @Override
    @Observed(name = "friendship-exist-id", contextualName = "friendship-exist-id")
    public boolean existsByUserIdAndFriendId(UUID userId, UUID friendId) {
        return repository.existsByUserIdAndFriendId(userId, friendId);
    }

    @Override
    @Observed(name = "friendship-find-all", contextualName = "friendship-find-all")
    public PageResult<Friendship> findAll(Page page) {

        ScrollPosition scrollPosition = page.parseTimeAndIdScrollPosition(Page.Keys.CREATED_AT_AND_ID);
        Window<FriendshipJpaEntity> result = repository.findAllByOrderByCreatedAtDescIdDesc(
                scrollPosition,
                Limit.of(page.size()));
        List<Friendship> friendships = result.map(mapper::toDomain).toList();
        Friendship lastFriendship = friendships.isEmpty() ? null : friendships.getLast();
        int size = friendships.size();

        Page nextPage = (result.hasNext() && lastFriendship != null)
                ? new Page("%s_%s".formatted(lastFriendship.getCreatedAt(), lastFriendship.getId()), size)
                : new Page(null, size);

        return new PageResult<>(
                friendships,
                page,
                nextPage);
    }

    @Override
    @Observed(name = "friendship-find-friends", contextualName = "friendship-find-friends")
    public PageResult<User> findAllFriends(UUID userId, Page page) {
        val pagesKey = FRIENDS_PAGES_KEY.formatted(userId);
        val dataKey = FRIENDS_KEY.formatted(userId, page.token(), page.size());

        PageResult<User> cachedResult = (PageResult<User>) redisTemplate.getForValue(dataKey);
        if (cachedResult != null) {
            log.info("**** Found cached result for friends {} ****", dataKey);
            return cachedResult;
        }

        Page.Params params = page.tokenToTimeAndId();
        Slice<UserWithFriendship> result = repository.findAllFriends(userId,
                params == null ? UUID.randomUUID() : params.id(),
                params == null ? Instant.now().plus(1, ChronoUnit.DAYS) : params.time(),
                Pageable.ofSize(page.size()));

        Instant lastFriendshipCreatedAt =
                result.getContent().isEmpty() ? null : result.getContent().getLast().getFriendshipCreatedAt();
        List<User> users = result.map(friendshipMapper::toUser).toList();
        int size = users.size();

        Page nextPage = (result.hasNext() && lastFriendshipCreatedAt != null)
                ? new Page("%s_%s".formatted(lastFriendshipCreatedAt, users.getLast().getId()), size)
                : new Page(null, size);

        var pageResult = new PageResult<>(
                users,
                page,
                nextPage);

        log.info("**** Storing pages Key: {}, friends Key: {}", pagesKey, dataKey);
        redisTemplate.addPagesKeyData(pagesKey, dataKey, pageResult);
        return pageResult;
    }

    //test nastavit cesty pre lokalny development
    private void invalidate(String redisPagesKeyTemplate, UUID userId) {
        try {
            String redisPagesKey = redisPagesKeyTemplate.formatted(userId);
            log.info("**** Invalidated cache for user ID {} for key: {}.", userId, redisPagesKey);
            redisTemplate.invalidateByPagesKey(redisPagesKey);
        } catch (Exception e) {
            log.warn("**** Failed to invalidate cache for user ID {}.", userId, e);
        }
    }
}
