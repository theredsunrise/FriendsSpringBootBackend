package com.example.demo.user.infrastructure.out.repository;

import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.user.domain.User;
import com.example.demo.user.infrastructure.out.repository.jpa.SpringDataUserRepository;
import com.example.demo.user.infrastructure.out.repository.jpa.maper.UserJpaMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest()
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan("com.example.demo")
@Import({
        UserAdapter.class,
        UserJpaMapperImpl.class
})
@Testcontainers
class UserAdapterPostgresPagingTest {

    private static final LocalDate DEFAULT_BIRTH_DATE =
            LocalDate.of(1990, 1, 1);

    private static final String DEFAULT_RESIDENCE = "Bratislava";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withCommand("postgres", "-c", "wal_level=logical");

    @Autowired
    private UserAdapter userAdapter;

    @Autowired
    private SpringDataUserRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
        repository.flush();
    }

    @Test
    void shouldKeepKeysetPaginationStableWhenNewUsersAreInsertedBetweenPages() {
        Instant baseTime = Instant.now().minus(1, ChronoUnit.HOURS);

        List<User> initialUsers = IntStream.range(0, 10)
                .mapToObj(index ->
                        user(index, baseTime.minus(index, ChronoUnit.SECONDS)))
                .map(userAdapter::save)
                .toList();

        PageResult<User> firstPage =
                userAdapter.findAll(new Page(null, 3));

        assertEquals(3, firstPage.content().size());
        assertTrue(firstPage.hasNext());

        Set<Long> insertedUserIds = Stream.of(
                        user(100, baseTime.plusSeconds(1)),
                        user(101, baseTime.plusSeconds(2))
                )
                .map(userAdapter::save)
                .map(User::getId)
                .collect(Collectors.toSet());

        Set<Long> seenUserIds = new LinkedHashSet<>(
                firstPage.content().stream()
                        .map(User::getId)
                        .toList()
        );

        Page page = firstPage.nextPage();

        while (page != null && page.token() != null) {
            PageResult<User> result = userAdapter.findAll(page);

            List<Long> pageUserIds = result.content().stream()
                    .map(User::getId)
                    .toList();

            assertTrue(
                    seenUserIds.addAll(pageUserIds),
                    "A user was returned more than once while paging"
            );

            assertTrue(
                    pageUserIds.stream().noneMatch(insertedUserIds::contains),
                    "A user inserted after the first page appeared on a later page"
            );

            page = result.hasNext()
                    ? result.nextPage()
                    : null;
        }

        assertEquals(
                initialUsers.stream()
                        .map(User::getId)
                        .collect(Collectors.toSet()),
                seenUserIds
        );

        assertEquals(initialUsers.size(), seenUserIds.size());
    }

    @Test
    void shouldReturnUsersInCreatedAtDescendingAndIdAscendingOrder() {
        Instant baseTime = Instant.now().minus(1, ChronoUnit.HOURS);

        List.of(
                user(1, baseTime),
                user(2, baseTime),
                user(3, baseTime.minusSeconds(1)),
                user(4, baseTime.minusSeconds(2))
        ).forEach(userAdapter::save);

        List<User> users = userAdapter
                .findAll(new Page(null, 10))
                .content();

        assertEquals(4, users.size());

        for (int i = 1; i < users.size(); i++) {
            User previous = users.get(i - 1);
            User current = users.get(i);

            assertFalse(previous.getCreatedAt().isBefore(current.getCreatedAt()), "Users are not ordered by createdAt descending");

            if (previous.getCreatedAt().equals(current.getCreatedAt())) {
                assertTrue(
                        compareId(previous.getId(), current.getId()) < 0,
                        "Users with the same createdAt are not ordered by id ascending"
                );
            }
        }
    }

    @Test
    void shouldPersistUserAndGenerateId() {
        User savedUser = userAdapter.save(user(200, Instant.now()));

        assertNotNull(savedUser.getId());
        assertEquals("paging-user-200", savedUser.getUsername());
        assertTrue(repository.existsById(savedUser.getId()));
    }

    @Test
    void shouldReturnPersistedUserById() {
        User savedUser = userAdapter.save(user(201, Instant.now()));

        User loadedUser = userAdapter.getById(savedUser.getId())
                .orElseThrow();

        assertEquals(savedUser.getId(), loadedUser.getId());
        assertEquals(savedUser.getUsername(), loadedUser.getUsername());
        assertEquals(savedUser.getName(), loadedUser.getName());
        assertEquals(
                savedUser.getCreatedAt().truncatedTo(ChronoUnit.MICROS),
                loadedUser.getCreatedAt()
        );
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {
        assertTrue(userAdapter.getById(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE)).isEmpty());
    }

    @Test
    void shouldDeleteUserAndEvictUserCache() {
        User savedUser = userAdapter.save(user(202, Instant.now()));

        assertTrue(userAdapter.getById(savedUser.getId()).isPresent());

        userAdapter.deleteById(savedUser.getId());

        assertFalse(repository.existsById(savedUser.getId()));
        assertTrue(userAdapter.getById(savedUser.getId()).isEmpty());
    }

    @Test
    void shouldFindUserByIdAndUsername() {
        User savedUser = userAdapter.save(user(203, Instant.now()));

        assertTrue(userAdapter.existsById(savedUser.getId()));
        assertTrue(userAdapter.existsByUsername(savedUser.getUsername()));

        assertFalse(userAdapter.existsById(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE)));
        assertFalse(userAdapter.existsByUsername("missing-username"));
    }

    private static int compareId(Long left, Long right) {
        return Long.compare(left, right);
    }

    private static User user(int index, Instant createdAt) {
        return User.builder()
                .name("Name" + index)
                .surname("Surname" + index)
                .username("paging-user-" + index)
                .birthDate(DEFAULT_BIRTH_DATE)
                .residence(DEFAULT_RESIDENCE)
                .createdAt(createdAt)
                .build();
    }
}
