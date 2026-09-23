package com.example.demo.friendship.application.service;

import com.example.demo.friendship.application.exception.FriendshipAlreadyExistsException;
import com.example.demo.friendship.application.port.out.repository.FriendshipRepository;
import com.example.demo.friendship.domain.Friendship;
import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.shared.infrastructure.out.event.UserDeletedIntegrationEvent;
import com.example.demo.user.application.exception.UserNotFoundException;
import com.example.demo.user.application.port.out.repository.UserRepository;
import com.example.demo.user.domain.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock
    Page page;

    @Mock
    UserRepository userRepository;

    @Mock
    FriendshipRepository friendshipRepository;

    @InjectMocks
    FriendshipService friendshipService;

    @Nested
    class HandleIntegrationEvent {

        @Test
        void handleUserDeleted() {
            Long userId = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);

            friendshipService.handleIntegrationEvent(
                    new UserDeletedIntegrationEvent(userId)
            );

            verify(friendshipRepository).invalidateCache(userId);

            verifyNoInteractions(userRepository);
            verifyNoMoreInteractions(friendshipRepository);
        }
    }

    @Nested
    class Add {

        @Test
        void add() {
            Long id = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
            Long userId = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
            Long friendId = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);

            Friendship friendship = Friendship.builder()
                    .id(id)
                    .userId(userId)
                    .friendId(friendId)
                    .createdAt(null)
                    .build();

            when(userRepository.existsById(userId)).thenReturn(true);
            when(userRepository.existsById(friendId)).thenReturn(true);
            when(friendshipRepository.existsByUserIdAndFriendId(userId, friendId))
                    .thenReturn(false);
            when(friendshipRepository.save(friendship))
                    .thenReturn(friendship);

            Friendship result = friendshipService.add(friendship);

            assertThat(result)
                    .usingRecursiveComparison()
                    .ignoringFields("createdAt")
                    .isEqualTo(friendship);

            assertThat(result.getCreatedAt()).isNotNull();

            verify(userRepository).existsById(userId);
            verify(userRepository).existsById(friendId);
            verify(friendshipRepository)
                    .existsByUserIdAndFriendId(userId, friendId);
            verify(friendshipRepository).save(friendship);

            verifyNoMoreInteractions(userRepository, friendshipRepository);
        }

        @ParameterizedTest
        @CsvSource({
                "false, true",
                "true, false"
        })
        void userDoesNotExist(
                boolean existsUserId,
                boolean existsFriendId
        ) {
            Long id = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
            Long userId = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
            Long friendId = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);

            Friendship friendship = Friendship.builder()
                    .id(id)
                    .userId(userId)
                    .friendId(friendId)
                    .createdAt(null)
                    .build();

            when(userRepository.existsById(userId))
                    .thenReturn(existsUserId);

            if (existsUserId) {
                when(userRepository.existsById(friendId))
                        .thenReturn(existsFriendId);
            }

            assertThrows(
                    UserNotFoundException.class,
                    () -> friendshipService.add(friendship)
            );

            verify(userRepository).existsById(userId);

            if (existsUserId) {
                verify(userRepository).existsById(friendId);
            }

            verifyNoInteractions(friendshipRepository);
            verifyNoMoreInteractions(userRepository);
        }

        @Test
        void friendshipAlreadyExists() {
            Long id = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
            Long userId = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
            Long friendId = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);

            Friendship friendship = Friendship.builder()
                    .id(id)
                    .userId(userId)
                    .friendId(friendId)
                    .createdAt(null)
                    .build();

            when(userRepository.existsById(userId)).thenReturn(true);
            when(userRepository.existsById(friendId)).thenReturn(true);
            when(friendshipRepository.existsByUserIdAndFriendId(userId, friendId))
                    .thenReturn(true);

            assertThrows(
                    FriendshipAlreadyExistsException.class,
                    () -> friendshipService.add(friendship)
            );

            verify(userRepository).existsById(userId);
            verify(userRepository).existsById(friendId);

            verify(friendshipRepository)
                    .existsByUserIdAndFriendId(userId, friendId);

            verify(friendshipRepository, never()).save(any(Friendship.class));

            verifyNoMoreInteractions(userRepository, friendshipRepository);
        }
    }

    @Nested
    class DeleteFromUser {

        @Test
        void delete() {
            Long userId = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
            Long friendId = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);

            when(userRepository.existsById(userId)).thenReturn(true);
            when(userRepository.existsById(friendId)).thenReturn(true);

            friendshipService.deleteFromUser(userId, friendId);

            verify(userRepository).existsById(userId);
            verify(userRepository).existsById(friendId);

            verify(friendshipRepository)
                    .deleteByUserIdAndFriendId(userId, friendId);

            verifyNoMoreInteractions(userRepository, friendshipRepository);
        }

        @ParameterizedTest
        @CsvSource({
                "false, true",
                "true, false"
        })
        void userDoesNotExist(
                boolean existsUserId,
                boolean existsFriendId
        ) {
            Long userId = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
            Long friendId = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);

            when(userRepository.existsById(userId))
                    .thenReturn(existsUserId);

            if (existsUserId) {
                when(userRepository.existsById(friendId))
                        .thenReturn(existsFriendId);
            }

            assertThrows(
                    UserNotFoundException.class,
                    () -> friendshipService.deleteFromUser(userId, friendId)
            );

            verify(userRepository).existsById(userId);

            if (existsUserId) {
                verify(userRepository).existsById(friendId);
            }

            verifyNoInteractions(friendshipRepository);
            verifyNoMoreInteractions(userRepository);
        }
    }

    @Nested
    class FindAll {

        @Test
        void findAll() {
            PageResult expected = mock(PageResult.class);

            when(friendshipRepository.findAll(page))
                    .thenReturn(expected);

            PageResult<Friendship> result =
                    friendshipService.findAll(page);

            assertThat(result).isSameAs(expected);

            verify(friendshipRepository).findAll(page);

            verifyNoInteractions(userRepository);
            verifyNoMoreInteractions(friendshipRepository);
        }
    }

    @Nested
    class FindAllFriends {

        @Test
        void findAll() {
            Long userId = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);

            PageResult<User> expected = mock(PageResult.class);

            when(userRepository.existsById(userId))
                    .thenReturn(true);

            when(friendshipRepository.findAllFriends(userId, page))
                    .thenReturn(expected);

            PageResult<User> result =
                    friendshipService.findAllFriends(userId, page);

            assertThat(result).isSameAs(expected);

            verify(userRepository).existsById(userId);
            verify(friendshipRepository).findAllFriends(userId, page);

            verifyNoMoreInteractions(userRepository, friendshipRepository);
        }

        @Test
        void userDoesNotExist() {
            Long userId = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);

            when(userRepository.existsById(userId))
                    .thenReturn(false);

            assertThrows(
                    UserNotFoundException.class,
                    () -> friendshipService.findAllFriends(userId, page)
            );

            verify(userRepository).existsById(userId);

            verifyNoInteractions(friendshipRepository);
            verifyNoMoreInteractions(userRepository);
        }
    }
}
