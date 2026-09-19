package com.example.demo.friendship.infrastructure.in.controller;

import com.example.demo.friendship.application.port.in.FriendshipUseCase;
import com.example.demo.friendship.domain.Friendship;
import com.example.demo.friendship.infrastructure.in.controller.dto.AddFriendToUserDto;
import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.shared.infrastructure.in.controller.dto.FriendshipsResponseDto;
import com.example.demo.shared.infrastructure.in.controller.dto.PageDto;
import com.example.demo.shared.infrastructure.in.controller.dto.UsersResponseDto;
import com.example.demo.shared.infrastructure.in.controller.mapper.FriendshipWebMapper;
import com.example.demo.shared.infrastructure.in.controller.mapper.PageWebMapper;
import com.example.demo.shared.infrastructure.in.controller.mapper.UserWebMapper;
import com.example.demo.shared.infrastructure.out.openTelemetry.OpenTelemetryHelper;
import com.example.demo.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/friendships")
@Tag(name = "Friends", description = "Endpoints for managing friends of an user")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipUseCase friendshipUseCase;
    private final PageWebMapper pageWebMapper;
    private final FriendshipWebMapper friendshipWebMapper;
    private final UserWebMapper userWebMapper;

    @PostMapping
    @Operation(summary = "Add a new friendship")
    public ResponseEntity<Void> addToUser(@Valid @RequestBody AddFriendToUserDto dto) {
        OpenTelemetryHelper.setMyTag("friendship-add-new");
        friendshipUseCase.addToUser(dto.userId(), dto.friendId());

        return new ResponseEntity<>(
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{userId}/friendship/{friendId}")
    @Operation(summary = "Delete a friendship by user ID and friend ID")
    public ResponseEntity<Void> deleteFromUser(@PathVariable UUID userId, @PathVariable UUID friendId) {
        OpenTelemetryHelper.setMyTag("friendship-delete");

        friendshipUseCase.deleteFromUser(userId, friendId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/friends")
    @Operation(summary = "Fetch friends")
    public ResponseEntity<UsersResponseDto> findAllFriends(
            @RequestParam UUID userId,
            @Valid @ModelAttribute PageDto pageDto) {
        OpenTelemetryHelper.setMyTag("friendship-user-friends-all");

        Page currentPage = pageWebMapper.toDomain(pageDto);
        PageResult<User> result = friendshipUseCase.findAllFriends(userId, currentPage);

        return ResponseEntity.ok(
                new UsersResponseDto(
                        result.content().stream().map(userWebMapper::toDto).toList(),
                        pageWebMapper.toDto(result.currentPage()),
                        pageWebMapper.toDto(result.nextPage())
                )
        );
    }

    @GetMapping("/all")
    @Operation(summary = "Fetch friendships")
    public ResponseEntity<FriendshipsResponseDto> findAll(@Valid @ModelAttribute PageDto pageDto) {
        OpenTelemetryHelper.setMyTag("friendships-all");

        Page currentPage = pageWebMapper.toDomain(pageDto);
        PageResult<Friendship> result = friendshipUseCase.findAll(currentPage);

        return ResponseEntity.ok(
                new FriendshipsResponseDto(
                        result.content().stream().map(friendshipWebMapper::toDto).toList(),
                        pageWebMapper.toDto(result.currentPage()),
                        pageWebMapper.toDto(result.nextPage())
                )
        );
    }
}
