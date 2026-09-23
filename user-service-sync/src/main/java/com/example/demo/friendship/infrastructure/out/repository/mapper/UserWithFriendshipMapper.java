package com.example.demo.friendship.infrastructure.out.repository.mapper;

import com.example.demo.user.domain.User;
import com.example.demo.friendship.infrastructure.out.repository.dto.projection.UserWithFriendship;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.ERROR)
public interface UserWithFriendshipMapper {

    @Mapping(target = "id", source = "userId")
    @BeanMapping(ignoreUnmappedSourceProperties = {"id", "friendshipCreatedAt"})
    User toUser(UserWithFriendship dto);
}
