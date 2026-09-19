package com.example.demo.user.infrastructure.in.controller;

import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.shared.infrastructure.in.controller.dto.PageDto;
import com.example.demo.shared.infrastructure.in.controller.dto.UsersResponseDto;
import com.example.demo.shared.infrastructure.in.controller.mapper.PageWebMapper;
import com.example.demo.shared.infrastructure.in.controller.mapper.UserWebMapper;
import com.example.demo.shared.infrastructure.out.openTelemetry.OpenTelemetryHelper;
import com.example.demo.user.application.port.in.UserUseCase;
import com.example.demo.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Endpoints for managing user accounts")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase useCase;
    private final PageWebMapper pageWebMapper;
    private final UserWebMapper userWebMapper;

    @GetMapping("/all")
    @Operation(summary = "Fetch users")
    public ResponseEntity<UsersResponseDto> getUsers(@Valid @ModelAttribute PageDto pageDto) {
        OpenTelemetryHelper.setMyTag("replica-users-all");

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
}
