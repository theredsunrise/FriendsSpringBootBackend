package com.example.demo.friendship.infrastructure.out.repository;

import com.example.demo.friendship.domain.Friendship;
import com.example.demo.friendship.infrastructure.out.repository.dto.FriendshipMongoEntity;
import com.example.demo.friendship.infrastructure.out.repository.mapper.FriendshipJpaMapper;
import com.example.demo.friendship.infrastructure.out.repository.mapper.FriendshipJpaMapperImpl;
import com.example.demo.friendship.infrastructure.out.repository.mapper.UserWithFriendshipMapperImpl;
import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.shared.infrastructure.out.repository.redis.RedisCachedTemplate;
import com.example.demo.user.domain.User;
import com.example.demo.user.infrastructure.out.repository.dto.UserMongoEntity;
import com.example.demo.user.infrastructure.out.repository.mapper.UserJpaMapper;
import com.example.demo.user.infrastructure.out.repository.mapper.UserJpaMapperImpl;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DataMongoTest
@EntityScan("com.example.demo")
@Testcontainers
@Import({
        UserJpaMapperImpl.class,
        FriendshipJpaMapperImpl.class,
        UserWithFriendshipMapperImpl.class,
        FriendshipAdapter.class
})
class FriendshipAdapterTest {

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO =
            new MongoDBContainer("mongo:8.2.12");

    @MockitoBean
    RedisCachedTemplate redisTemplate;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    UserJpaMapper userJpaMapper;

    @Autowired
    FriendshipJpaMapper friendshipJpaMapper;

    @Autowired
    FriendshipAdapter friendshipAdapter;

    private List<User> users;

    @BeforeEach
    void setUp() {
        seedUsers();
    }

    @AfterEach
    void tearDown() {
        cleanDB();
        reset(redisTemplate);
    }

    private void cleanDB() {
        Stream.of(UserMongoEntity.class, FriendshipMongoEntity.class)
                .map(mongoTemplate::getCollectionName)
                .forEach(name -> {
                    mongoTemplate.getCollection(name)
                            .deleteMany(new Document());

                    assertThat(
                            mongoTemplate.getCollection(name)
                                    .countDocuments()
                    ).isZero();
                });
    }

    private void seedUsers() {
        var usersDto = IntStream.range(0, 10)
                .mapToObj(index ->
                        User.builder()
                                .id(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE))
                                .name("Name" + index)
                                .surname("Surname" + index)
                                .username("Username" + index)
                                .residence("Residence" + index)
                                .birthDate(LocalDate.now())
                                .createdAt(Instant.now().plusSeconds(index))
                                .build()
                )
                .map(userJpaMapper::toMongoEntity)
                .toList();

        users = mongoTemplate.insertAll(usersDto)
                .stream()
                .map(userJpaMapper::toDomain)
                .toList();
    }

    private Friendship friendship(int userIndex, int friendIndex) {
        return Friendship.builder()
                .id(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE))
                .userId(users.get(userIndex).getId())
                .friendId(users.get(friendIndex).getId())
                .build();
    }

    private Friendship friendship(int userIndex, int friendIndex, Instant createAt) {
        return Friendship.builder()
                .id(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE))
                .userId(users.get(userIndex).getId())
                .friendId(users.get(friendIndex).getId())
                .createdAt(createAt)
                .build();
    }

    private void insertFriendships(Friendship... friendships) {
        var entities = Stream.of(friendships)
                .map(friendshipJpaMapper::toMongoEntity)
                .toList();

        mongoTemplate.insertAll(entities);
    }

    @Nested
    class Save {

        @Test
        void shouldSaveFriendship() {
            var friendship = friendship(0, 1);

            var result = friendshipAdapter.save(friendship);

            assertThat(result)
                    .usingRecursiveComparison()
                    .isEqualTo(friendship);

            assertThat(
                    friendshipAdapter.existsByUserIdAndFriendId(
                            friendship.getUserId(),
                            friendship.getFriendId()
                    )
            ).isTrue();
        }

        @Test
        void shouldInvalidateCacheForBothUsers() {
            var friendship = friendship(0, 1);

            friendshipAdapter.save(friendship);

            verify(redisTemplate)
                    .invalidateByPagesKey(
                            "user-service-sync::friend:pages:%s"
                                    .formatted(users.getFirst().getId())
                    );

            verify(redisTemplate)
                    .invalidateByPagesKey(
                            "user-service-sync::friend:pages:%s"
                                    .formatted(users.get(1).getId())
                    );
        }

        @Test
        void shouldPersistFriendshipEvenWhenRedisFails() {
            doThrow(new RuntimeException("Redis unavailable"))
                    .when(redisTemplate)
                    .invalidateByPagesKey(anyString());

            var friendship = friendship(0, 1);

            var result = friendshipAdapter.save(friendship);

            assertThat(result)
                    .usingRecursiveComparison()
                    .isEqualTo(friendship);

            assertThat(
                    friendshipAdapter.existsByUserIdAndFriendId(
                            friendship.getUserId(),
                            friendship.getFriendId()
                    )
            ).isTrue();
        }
    }

    @Nested
    class Exists {

        @Test
        void shouldReturnTrueWhenFriendshipExists() {
            var friendship = friendship(0, 1);

            friendshipAdapter.save(friendship);

            assertThat(
                    friendshipAdapter.existsByUserIdAndFriendId(
                            friendship.getUserId(),
                            friendship.getFriendId()
                    )
            ).isTrue();
        }

        @Test
        void shouldReturnFalseWhenFriendshipDoesNotExist() {
            assertThat(
                    friendshipAdapter.existsByUserIdAndFriendId(
                            users.getFirst().getId(),
                            users.get(1).getId()
                    )
            ).isFalse();
        }

        @Test
        void shouldRespectFriendshipDirection() {
            friendshipAdapter.save(friendship(0, 1));

            assertThat(
                    friendshipAdapter.existsByUserIdAndFriendId(
                            users.getFirst().getId(),
                            users.get(1).getId()
                    )
            ).isTrue();

            assertThat(
                    friendshipAdapter.existsByUserIdAndFriendId(
                            users.get(1).getId(),
                            users.getFirst().getId()
                    )
            ).isFalse();
        }
    }

    // ============================================================
    // DELETE
    // ============================================================

    @Nested
    class Delete {

        @Test
        void shouldDeleteFriendship() {
            var friendship = friendship(0, 1);

            friendshipAdapter.save(friendship);

            assertThat(
                    friendshipAdapter.existsByUserIdAndFriendId(
                            friendship.getUserId(),
                            friendship.getFriendId()
                    )
            ).isTrue();

            friendshipAdapter.deleteByUserIdAndFriendId(
                    friendship.getUserId(),
                    friendship.getFriendId()
            );

            assertThat(
                    friendshipAdapter.existsByUserIdAndFriendId(
                            friendship.getUserId(),
                            friendship.getFriendId()
                    )
            ).isFalse();
        }

        @Test
        void shouldInvalidateCacheForBothUsers() {
            var friendship = friendship(0, 1);

            friendshipAdapter.save(friendship);

            reset(redisTemplate);

            friendshipAdapter.deleteByUserIdAndFriendId(
                    friendship.getUserId(),
                    friendship.getFriendId()
            );

            verify(redisTemplate)
                    .invalidateByPagesKey(
                            "user-service-sync::friend:pages:%s"
                                    .formatted(users.getFirst().getId())
                    );

            verify(redisTemplate)
                    .invalidateByPagesKey(
                            "user-service-sync::friend:pages:%s"
                                    .formatted(users.get(1).getId())
                    );
        }

        @Test
        void shouldNotFailWhenFriendshipDoesNotExist() {
            var userId = users.getFirst().getId();
            var friendId = users.get(1).getId();

            friendshipAdapter.deleteByUserIdAndFriendId(
                    userId,
                    friendId
            );

            assertThat(
                    friendshipAdapter.existsByUserIdAndFriendId(
                            userId,
                            friendId
                    )
            ).isFalse();
        }

        @Test
        void shouldNotFailWhenRedisInvalidationFails() {
            doThrow(new RuntimeException("Redis unavailable"))
                    .when(redisTemplate)
                    .invalidateByPagesKey(anyString());

            assertThatCode(() ->
                    friendshipAdapter.deleteByUserIdAndFriendId(
                            users.getFirst().getId(),
                            users.get(1).getId()
                    )
            ).doesNotThrowAnyException();
        }
    }

    // ============================================================
    // INVALIDATE CACHE
    // ============================================================

    @Nested
    class InvalidateCache {

        @Test
        void shouldInvalidateCacheForUser() {
            var userId = users.getFirst().getId();

            friendshipAdapter.invalidateCache(userId);

            verify(redisTemplate)
                    .invalidateByPagesKey(
                            "user-service-sync::friend:pages:%s"
                                    .formatted(userId)
                    );
        }

        @Test
        void shouldNotThrowWhenRedisFails() {
            doThrow(new RuntimeException("Redis unavailable"))
                    .when(redisTemplate)
                    .invalidateByPagesKey(anyString());

            assertThatCode(() ->
                    friendshipAdapter.invalidateCache(
                            users.getFirst().getId()
                    )
            ).doesNotThrowAnyException();
        }
    }

    // ============================================================
    // FIND ALL
    // ============================================================

    @Nested
    class FindAll {

        @Test
        void shouldReturnEmptyPageWhenThereAreNoFriendships() {
            var result = friendshipAdapter.findAll(
                    new Page(null, 10)
            );

            assertThat(result.content())
                    .isEmpty();

            assertThat(result.nextPage())
                    .isNotNull();

            assertThat(result.nextPage().token())
                    .isNull();

            assertThat(result.nextPage().size())
                    .isZero();
        }

        @Test
        void shouldReturnAllFriendships() {
            insertFriendships(
                    friendship(0, 1),
                    friendship(0, 2),
                    friendship(1, 2)
            );

            var result = friendshipAdapter.findAll(
                    new Page(null, 10)
            );

            assertThat(result.content())
                    .hasSize(3);

            assertThat(result.nextPage())
                    .isNotNull();

            assertThat(result.nextPage().token())
                    .isNull();
        }

        @Test
        void shouldRespectPageSize() {
            insertFriendships(
                    friendship(0, 1),
                    friendship(0, 2),
                    friendship(0, 3),
                    friendship(0, 4),
                    friendship(0, 5)
            );

            var result = friendshipAdapter.findAll(
                    new Page(null, 2)
            );

            assertThat(result.content())
                    .hasSize(2);

            assertThat(result.nextPage())
                    .isNotNull();

            assertThat(result.nextPage().token())
                    .isNotNull();

            assertThat(result.nextPage().size())
                    .isEqualTo(2);
        }

        @Test
        void shouldReturnSecondPage() {
            insertFriendships(
                    friendship(0, 1),
                    friendship(0, 2),
                    friendship(0, 3),
                    friendship(0, 4),
                    friendship(0, 5)
            );

            var firstPage = friendshipAdapter.findAll(
                    new Page(null, 2)
            );

            var secondPage = friendshipAdapter.findAll(
                    firstPage.nextPage()
            );

            assertThat(firstPage.content())
                    .hasSize(2);

            assertThat(firstPage.nextPage().token())
                    .isNotNull();

            assertThat(secondPage.content())
                    .hasSize(2);

            assertThat(
                    firstPage.content()
                            .stream()
                            .map(Friendship::getId)
                            .toList()
            ).doesNotContainAnyElementsOf(
                    secondPage.content()
                            .stream()
                            .map(Friendship::getId)
                            .toList()
            );
        }

        @Test
        void shouldReturnLastPageWithoutNextToken() {
            insertFriendships(
                    friendship(0, 1),
                    friendship(0, 2),
                    friendship(0, 3),
                    friendship(0, 4),
                    friendship(0, 5)
            );

            var firstPage = friendshipAdapter.findAll(
                    new Page(null, 2)
            );

            var secondPage = friendshipAdapter.findAll(
                    firstPage.nextPage()
            );

            var thirdPage = friendshipAdapter.findAll(
                    secondPage.nextPage()
            );

            assertThat(firstPage.content())
                    .hasSize(2);

            assertThat(secondPage.content())
                    .hasSize(2);

            assertThat(thirdPage.content())
                    .hasSize(1);

            assertThat(thirdPage.nextPage())
                    .isNotNull();

            assertThat(thirdPage.nextPage().token())
                    .isNull();
        }

        @Test
        void shouldReturnAllFriendshipsExactlyOnceAcrossPages() {
            insertFriendships(
                    friendship(0, 1),
                    friendship(0, 2),
                    friendship(0, 3),
                    friendship(0, 4),
                    friendship(0, 5),
                    friendship(0, 6),
                    friendship(0, 7)
            );

            Page page = new Page(null, 3);

            var allFriendships = new ArrayList<Friendship>();

            while (true) {
                var result = friendshipAdapter.findAll(page);

                allFriendships.addAll(result.content());

                if (result.nextPage().token() == null) {
                    break;
                }

                page = result.nextPage();
            }

            assertThat(allFriendships)
                    .hasSize(7);

            assertThat(
                    allFriendships.stream()
                            .map(Friendship::getId)
                            .distinct()
                            .count()
            ).isEqualTo(7);
        }
    }

    // ============================================================
    // FIND ALL FRIENDS
    // ============================================================

    @Nested
    class FindAllFriends {

        @Test
        void shouldReturnFriends() {
            insertFriendships(
                    friendship(0, 1),
                    friendship(0, 2),
                    friendship(0, 3)
            );

            var result = friendshipAdapter.findAllFriends(
                    users.getFirst().getId(),
                    new Page(null, 10)
            );

            assertThat(result.content())
                    .hasSize(3);

            assertThat(result.content())
                    .extracting(User::getId)
                    .containsExactlyInAnyOrder(
                            users.get(1).getId(),
                            users.get(2).getId(),
                            users.get(3).getId()
                    );
        }

        @Test
        void shouldReturnEmptyWhenUserHasNoFriends() {
            var result = friendshipAdapter.findAllFriends(
                    users.getFirst().getId(),
                    new Page(null, 10)
            );

            assertThat(result.content())
                    .isEmpty();

            assertThat(result.nextPage())
                    .isNotNull();

            assertThat(result.nextPage().token())
                    .isNull();
        }

        @Test
        void shouldRespectPageSize() {
            insertFriendships(
                    friendship(0, 1),
                    friendship(0, 2),
                    friendship(0, 3),
                    friendship(0, 4),
                    friendship(0, 5)
            );

            var result = friendshipAdapter.findAllFriends(
                    users.getFirst().getId(),
                    new Page(null, 2)
            );

            assertThat(result.content())
                    .hasSize(2);

            assertThat(result.nextPage().token())
                    .isNotNull();
        }

        @Test
        void shouldReturnSecondPageWithoutDuplicates() {
            insertFriendships(
                    friendship(0, 1),
                    friendship(0, 2),
                    friendship(0, 3),
                    friendship(0, 4),
                    friendship(0, 5)
            );

            var firstPage = friendshipAdapter.findAllFriends(
                    users.getFirst().getId(),
                    new Page(null, 2)
            );

            var secondPage = friendshipAdapter.findAllFriends(
                    users.getFirst().getId(),
                    firstPage.nextPage()
            );

            assertThat(firstPage.content())
                    .hasSize(2);

            assertThat(secondPage.content())
                    .hasSize(2);

            assertThat(
                    firstPage.content()
                            .stream()
                            .map(User::getId)
                            .toList()
            ).doesNotContainAnyElementsOf(
                    secondPage.content()
                            .stream()
                            .map(User::getId)
                            .toList()
            );
        }

        @Test
        void shouldReturnLastPageWithoutNextToken() {
            insertFriendships(
                    friendship(0, 1),
                    friendship(0, 2),
                    friendship(0, 3),
                    friendship(0, 4),
                    friendship(0, 5)
            );

            var firstPage = friendshipAdapter.findAllFriends(
                    users.getFirst().getId(),
                    new Page(null, 2)
            );

            var secondPage = friendshipAdapter.findAllFriends(
                    users.getFirst().getId(),
                    firstPage.nextPage()
            );

            var thirdPage = friendshipAdapter.findAllFriends(
                    users.getFirst().getId(),
                    secondPage.nextPage()
            );

            assertThat(firstPage.content())
                    .hasSize(2);

            assertThat(secondPage.content())
                    .hasSize(2);

            assertThat(thirdPage.content())
                    .hasSize(1);

            assertThat(thirdPage.nextPage().token())
                    .isNull();
        }

        @Test
        void shouldNotReturnUserItself() {
            insertFriendships(
                    friendship(0, 1),
                    friendship(0, 2)
            );

            var result = friendshipAdapter.findAllFriends(
                    users.getFirst().getId(),
                    new Page(null, 10)
            );

            assertThat(result.content())
                    .extracting(User::getId)
                    .doesNotContain(users.getFirst().getId());
        }

        @Test
        void shouldReturnAllFriendsExactlyOnceAcrossPages() {
            insertFriendships(
                    friendship(0, 1),
                    friendship(0, 2),
                    friendship(0, 3),
                    friendship(0, 4),
                    friendship(0, 5),
                    friendship(0, 6),
                    friendship(0, 7)
            );

            Page page = new Page(null, 3);

            var allFriends = new ArrayList<User>();

            while (true) {
                var result = friendshipAdapter.findAllFriends(
                        users.getFirst().getId(),
                        page
                );

                allFriends.addAll(result.content());

                if (result.nextPage().token() == null) {
                    break;
                }

                page = result.nextPage();
            }

            assertThat(allFriends)
                    .hasSize(7);

            assertThat(
                    allFriends.stream()
                            .map(User::getId)
                            .distinct()
                            .count()
            ).isEqualTo(7);
        }
    }

    // ============================================================
    // CACHE
    // ============================================================

    @Nested
    class Cache {

        @Test
        void shouldReturnCachedResultWithoutMongoQuery() {
            var userId = users.getFirst().getId();
            var page = new Page(null, 10);

            var cachedResult = new PageResult<>(
                    List.of(users.get(1)),
                    page,
                    new Page(null, 1)
            );

            when(redisTemplate.getForValue(anyString()))
                    .thenReturn(cachedResult);

            var result = friendshipAdapter.findAllFriends(
                    userId,
                    page
            );

            assertThat(result)
                    .isSameAs(cachedResult);

            verify(redisTemplate)
                    .getForValue(
                            "user-service-sync::friends:%s:%s:%d"
                                    .formatted(
                                            userId,
                                            page.token(),
                                            page.size()
                                    )
                    );

            verify(redisTemplate, never())
                    .addPagesKeyData(
                            anyString(),
                            anyString(),
                            any()
                    );
        }

        @Test
        void shouldReturnAllFriendsAcrossPagesWithoutDuplicates() {
            var userId = users.getFirst().getId();
            var createdAt = Instant.now();
            var friendships = IntStream.range(1,
                    users.size()).mapToObj(friendIndex ->
                    friendship(0, friendIndex, createdAt.plusSeconds(friendIndex))
            ).toList();
            insertFriendships(friendships.toArray(Friendship[]::new));

            var page = new Page(null, 2);
            var collectedUsers = new ArrayList<User>();
            do {
                var result = friendshipAdapter.findAllFriends(userId, page);
                assertThat(result).satisfies(r -> {
                    var contentSize = r.content().size();
                    var nextPage = r.nextPage();
                    assertEquals(nextPage.size(), contentSize);
                });
                collectedUsers.addAll(result.content());
                page = result.nextPage();


            } while (page.token() != null);
            assertThat(collectedUsers).extracting(User::getUsername).isSortedAccordingTo(Comparator.reverseOrder());
            assertEquals(users.size() - 1, collectedUsers.size());
        }

        @Test
        void shouldStoreResultAfterCacheMiss() {
            insertFriendships(
                    friendship(0, 1),
                    friendship(0, 2)
            );

            when(redisTemplate.getForValue(anyString()))
                    .thenReturn(null);

            var page = new Page(null, 10);

            var result = friendshipAdapter.findAllFriends(
                    users.getFirst().getId(),
                    page
            );

            verify(redisTemplate)
                    .addPagesKeyData(
                            "user-service-sync::friend:pages:%s"
                                    .formatted(
                                            users.getFirst().getId()
                                    )
                            ,

                            "user-service-sync::friends:%s:%s:%d"
                                    .formatted(
                                            users.getFirst().getId(),
                                            page.token(),
                                            page.size()
                                    )
                            ,
                            result
                    );
        }

        @Test
        void shouldNotStoreResultWhenCacheHit() {
            var page = new Page(null, 10);

            var cachedResult = new PageResult<>(
                    List.of(users.get(1)),
                    page,
                    new Page(null, 1)
            );

            when(redisTemplate.getForValue(anyString()))
                    .thenReturn(cachedResult);

            friendshipAdapter.findAllFriends(
                    users.getFirst().getId(),
                    page
            );

            verify(redisTemplate, never())
                    .addPagesKeyData(
                            anyString(),
                            anyString(),
                            any()
                    );
        }
    }
}
