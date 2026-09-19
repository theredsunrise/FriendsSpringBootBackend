package com.example.demo.shared.infrastructure.in.controller.mapper;

import com.example.demo.shared.infrastructure.in.controller.dto.UserResponseDto;
import com.example.demo.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.ERROR)
public interface UserWebMapper {
    UserResponseDto toDto(User user);
}
