package com.example.demo.shared.infrastructure.in.controller.mapper;

import com.example.demo.friendship.domain.Friendship;
import com.example.demo.shared.infrastructure.in.controller.dto.FriendshipResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.ERROR)
public interface FriendshipWebMapper {
    FriendshipResponseDto toDto(Friendship friendship);
}
