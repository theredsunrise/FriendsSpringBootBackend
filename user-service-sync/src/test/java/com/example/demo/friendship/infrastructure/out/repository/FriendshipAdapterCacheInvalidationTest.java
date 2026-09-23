package com.example.demo.friendship.infrastructure.out.repository;

import com.example.demo.friendship.domain.Friendship;
import com.example.demo.friendship.infrastructure.out.repository.dto.FriendshipMongoEntity;
import com.example.demo.friendship.infrastructure.out.repository.mapper.FriendshipJpaMapper;
import com.example.demo.shared.infrastructure.out.repository.redis.RedisCachedTemplate;
import com.example.demo.shared.infrastructure.out.repository.redis.RedisTemplateConfig;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@DataRedisTest
@Import({
        RedisCachedTemplate.class,
        RedisTemplateConfig.class
})
@EntityScan("com.example.demo.friendship.infrastructure.out.repository")
@Repository("com.example.demo.friendship.infrastructure.out.repository")
@ComponentScan("com.example.demo.friendship.infrastructure.out.repository")
@Testcontainers
class FriendshipAdapterCacheInvalidationTest {

    private static final String FRIENDS_PAGES_KEY = "user-service-sync::friend:pages:%s";
    private static final String FRIENDS_KEY = "user-service-sync::friends:%s:%s:%d";

    @Container
    @ServiceConnection
    static final RedisContainer REDIS = new RedisContainer(
            DockerImageName.parse("redis:7-alpine"));

    @MockitoBean
    private SpringDataMongoFriendshipRepository repository;
    @MockitoBean
    private FriendshipJpaMapper mapper;

    @Autowired
    private FriendshipAdapter adapter;
    @Autowired
    private RedisCachedTemplate cachedRedisTemplate;
    @Autowired
    private RedisTemplate<String, Object> springRedisTemplate;

    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        executor = Executors.newFixedThreadPool(10);
    }

    @AfterEach
    void tearDown() {
        springRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
        executor.shutdownNow();
    }

    @Test
    void invalidatesBothCacheFamiliesForTenUsersInParallel() throws Exception {
        List<Long> userIds = List.of(
                java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE),
                java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE),
                java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE),
                java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE),
                java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE),
                java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE),
                java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE),
                java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE),
                java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE),
                java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE));

        userIds.forEach(this::seedCacheForUser);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Object>> tasks = userIds.stream()
                .map(userId -> executor.submit(() -> {
                    start.await();
                    adapter.invalidateCache(userId);
                    return null;
                }))
                .toList();

        start.countDown();
        for (Future<?> task : tasks) {
            task.get();
        }

        userIds.forEach(this::assertCacheEmptyForUser);
    }

    @Test
    void saveInvalidatesBothCacheFamiliesForBothUsers() {
        Long userId = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        Long friendId = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        Friendship friendship = friendship(userId, friendId);
        FriendshipMongoEntity savedEntity = new FriendshipMongoEntity();

        seedCacheForUser(userId);
        seedCacheForUser(friendId);
        when(mapper.toMongoEntity(friendship)).thenReturn(savedEntity);
        when(repository.save(savedEntity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(friendship);

        adapter.save(friendship);

        assertCacheEmptyForUser(userId);
        assertCacheEmptyForUser(friendId);
    }

    @Test
    void deleteInvalidatesBothCacheFamiliesForBothUsers() {
        Long userId = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        Long friendId = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);

        seedCacheForUser(userId);
        seedCacheForUser(friendId);

        adapter.deleteByUserIdAndFriendId(userId, friendId);

        assertCacheEmptyForUser(userId);
        assertCacheEmptyForUser(friendId);
    }

    private void seedCacheForUser(Long userId) {
        cachedRedisTemplate.addPagesKeyData(
                FRIENDS_PAGES_KEY.formatted(userId),
                FRIENDS_KEY.formatted(userId, "seed", 10),
                "friends-data");
    }

    private void assertCacheEmptyForUser(Long userId) {
        assertNull(cachedRedisTemplate.getForValue(FRIENDS_KEY.formatted(userId, "seed", 10)));
        assertEmpty(cachedRedisTemplate.getForSet(FRIENDS_PAGES_KEY.formatted(userId)));
    }

    private void assertEmpty(Set<Object> values) {
        assertEquals(Set.of(), values == null ? Set.of() : values);
    }

    private Friendship friendship(Long userId, Long friendId) {
        return Friendship.builder()
                .id(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE))
                .userId(userId)
                .friendId(friendId)
                .build();
    }
}
