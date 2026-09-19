package com.example.demo.user.infrastructure.out.repository.mapper;

import com.example.demo.user.domain.User;
import com.example.demo.user.infrastructure.out.repository.dto.UserMongoEntity;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserJpaMapper {

    @Mapping(target = "uuid", source = "id")
    @Mapping(target = "id", ignore = true)
    UserMongoEntity toMongoEntity(User user);

    @Mapping(target = "id", source = "uuid")
    @BeanMapping(ignoreUnmappedSourceProperties = "id")
    User toDomain(UserMongoEntity mongoEntity);
}