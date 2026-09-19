package com.example.demo.shared.infrastructure.in.controller.mapper;

import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.infrastructure.in.controller.dto.PageDto;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL
)
public interface PageWebMapper {

    Page toDomain(PageDto dto);
    PageDto toDto(Page page);
}
