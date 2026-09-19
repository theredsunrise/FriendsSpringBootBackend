package com.example.demo.user.infrastructure.in.controller;

import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.shared.infrastructure.in.controller.dto.PageDto;
import com.example.demo.shared.infrastructure.in.controller.dto.UserResponseDto;
import com.example.demo.shared.infrastructure.in.controller.dto.UsersResponseDto;
import com.example.demo.shared.infrastructure.in.controller.mapper.PageWebMapper;
import com.example.demo.shared.infrastructure.in.controller.mapper.UserWebMapper;
import com.example.demo.shared.infrastructure.out.openTelemetry.OpenTelemetryHelper;
import com.example.demo.user.application.port.in.UserUseCase;
import com.example.demo.user.domain.User;
import com.example.demo.user.infrastructure.in.controller.dto.UserCreateDto;
import com.example.demo.user.infrastructure.in.controller.mapper.UserCreateWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Endpoints for managing user accounts")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase useCase;
    private final PageWebMapper pageWebMapper;
    private final UserWebMapper userWebMapper;
    private final UserCreateWebMapper userCreateWebMapper;

    @PostMapping()
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserResponseDto> create(@Valid @RequestBody UserCreateDto dto) {
        OpenTelemetryHelper.setMyTag("user-new");

        User user = userCreateWebMapper.toDomain(dto);
        User createdUser = useCase.create(user);

        return new ResponseEntity<>(
                userWebMapper.toDto(createdUser),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/all")
    @Operation(summary = "Fetch users")
    public ResponseEntity<UsersResponseDto> findAll(@Valid @ModelAttribute PageDto pageDto) {
        OpenTelemetryHelper.setMyTag("users-find-all");

        Page currentPage = pageWebMapper.toDomain(pageDto);
        PageResult<User> result = useCase.findAll(currentPage);

        return ResponseEntity.ok(
                new UsersResponseDto(
                        result.content().stream().map(userWebMapper::toDto).toList(),
                        pageWebMapper.toDto(result.currentPage()),
                        pageWebMapper.toDto(result.nextPage())
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an existing user by ID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        OpenTelemetryHelper.setMyTag("user-delete");
        useCase.delete(id);

        return ResponseEntity.noContent().build();
    }
}
