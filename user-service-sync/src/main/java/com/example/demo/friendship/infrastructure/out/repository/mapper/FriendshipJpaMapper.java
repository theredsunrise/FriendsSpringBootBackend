package com.example.demo.friendship.infrastructure.out.repository.mapper;

import com.example.demo.friendship.domain.Friendship;
import com.example.demo.friendship.infrastructure.out.repository.dto.FriendshipMongoEntity;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface FriendshipJpaMapper {
    @Mapping(target = "uuid", source = "id")
    @Mapping(target = "id", ignore = true)
    FriendshipMongoEntity toMongoEntity(Friendship friendship);

    @Mapping(target = "id", source = "uuid")
    @BeanMapping(ignoreUnmappedSourceProperties = "id")
    Friendship toDomain(FriendshipMongoEntity friendship);
}
