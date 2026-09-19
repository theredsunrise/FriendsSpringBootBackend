package com.example.demo.friendship.infrastructure.out.repository;

import com.example.demo.friendship.domain.Friendship;
import com.example.demo.friendship.infrastructure.out.repository.dto.FriendshipMongoEntity;
import com.example.demo.friendship.infrastructure.out.repository.mapper.FriendshipJpaMapper;
import com.example.demo.friendship.infrastructure.out.repository.mapper.UserWithFriendshipMapper;
import com.example.demo.shared.infrastructure.out.repository.redis.RedisCacheProperties;
import com.example.demo.shared.infrastructure.out.repository.redis.RedisCachedTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
class FriendshipAdapterCacheInvalidationTest {

    private static final int REDIS_PORT = 6379;
    private static final String FRIENDS_PAGES_KEY = "user-service-sync::friend:pages:%s";
    private static final String FRIENDS_KEY = "user-service-sync::friends:%s:%s:%d";

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7.4-alpine"))
            .withExposedPorts(REDIS_PORT);

    private SpringDataMongoFriendshipRepository repository;
    private FriendshipJpaMapper mapper;
    private LettuceConnectionFactory connectionFactory;
    private RedisCachedTemplate redisTemplate;
    private FriendshipAdapter adapter;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        repository = mock(SpringDataMongoFriendshipRepository.class);
        mapper = mock(FriendshipJpaMapper.class);
        UserWithFriendshipMapper friendshipMapper = mock(UserWithFriendshipMapper.class);

        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration(
                redis.getHost(), redis.getMappedPort(REDIS_PORT));
        connectionFactory = new LettuceConnectionFactory(redisConfiguration);
        connectionFactory.afterPropertiesSet();

        RedisTemplate<String, Object> springRedisTemplate = new RedisTemplate<>();
        springRedisTemplate.setConnectionFactory(connectionFactory);
        springRedisTemplate.setKeySerializer(new StringRedisSerializer());
        springRedisTemplate.setHashKeySerializer(new StringRedisSerializer());
        springRedisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
        springRedisTemplate.setHashValueSerializer(new JdkSerializationRedisSerializer());
        springRedisTemplate.afterPropertiesSet();

        redisTemplate = new RedisCachedTemplate(
                springRedisTemplate,
                new RedisCacheProperties(Duration.ofMinutes(5), "", false, true));
        adapter = new FriendshipAdapter(repository, mapper, friendshipMapper, redisTemplate);
        executor = Executors.newFixedThreadPool(10);

        assert springRedisTemplate.getConnectionFactory() != null;
        springRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
        connectionFactory.destroy();
    }

    @Test
    void invalidatesBothCacheFamiliesForTenUsersInParallel() throws Exception {
        List<UUID> userIds = List.of(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

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
        UUID userId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();
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
        UUID userId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

        seedCacheForUser(userId);
        seedCacheForUser(friendId);

        adapter.deleteByUserIdAndFriendId(userId, friendId);

        assertCacheEmptyForUser(userId);
        assertCacheEmptyForUser(friendId);
    }

    private void seedCacheForUser(UUID userId) {
        redisTemplate.addPagesKeyData(
                FRIENDS_PAGES_KEY.formatted(userId),
                FRIENDS_KEY.formatted(userId, "seed", 10),
                "friends-data");
    }

    private void assertCacheEmptyForUser(UUID userId) {
        assertNull(redisTemplate.getForValue(FRIENDS_KEY.formatted(userId, "seed", 10)));
        assertEmpty(redisTemplate.getForSet(FRIENDS_PAGES_KEY.formatted(userId)));
    }

    private void assertEmpty(Set<Object> values) {
        assertEquals(Set.of(), values == null ? Set.of() : values);
    }

    private Friendship friendship(UUID userId, UUID friendId) {
        return Friendship.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .friendId(friendId)
                .build();
    }
}
