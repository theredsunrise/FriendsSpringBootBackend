package com.example.demo.friendship.infrastructure.out.repository.jpa.mapper;

import com.example.demo.user.domain.User;
import com.example.demo.friendship.infrastructure.out.repository.jpa.dto.projection.UserWithFriendship;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.ERROR)
public interface UserWithFriendshipMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = "friendshipCreatedAt")
    User toUser(UserWithFriendship dto);
}
