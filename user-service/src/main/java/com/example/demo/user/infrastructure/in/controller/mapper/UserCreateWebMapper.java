package com.example.demo.user.infrastructure.in.controller.mapper;

import com.example.demo.user.domain.User;
import com.example.demo.user.infrastructure.in.controller.dto.UserCreateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.ERROR)
public interface UserCreateWebMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toDomain(UserCreateDto dto);
}
