package com.example.demo.user.infrastructure.out.repository.jpa.maper;

import com.example.demo.user.domain.User;
import com.example.demo.user.infrastructure.out.repository.jpa.dto.UserJpaEntity;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.ERROR)
public interface UserJpaMapper {

    UserJpaEntity toJpaEntity(User user);
    User toDomain(UserJpaEntity jpaEntity);
}
