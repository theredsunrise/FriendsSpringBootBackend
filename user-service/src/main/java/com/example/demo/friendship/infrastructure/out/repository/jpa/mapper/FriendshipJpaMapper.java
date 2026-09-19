package com.example.demo.friendship.infrastructure.out.repository.jpa.mapper;

import com.example.demo.friendship.domain.Friendship;
import com.example.demo.friendship.infrastructure.out.repository.jpa.dto.FriendshipJpaEntity;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.ERROR)
public interface FriendshipJpaMapper {

    FriendshipJpaEntity toJpaEntity(Friendship friendship);

    Friendship toDomain(FriendshipJpaEntity jpaEntity);
}
