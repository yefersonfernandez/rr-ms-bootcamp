package com.onclass.bootcamp.api.mapper;

import com.onclass.bootcamp.api.dto.request.BootcampRequestDto;
import com.onclass.bootcamp.api.dto.response.BootcampResponseDto;
import com.onclass.bootcamp.model.bootcamp.Bootcamp;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BootcampMapper {
    Bootcamp toModel(BootcampRequestDto bootcampRequestDto);
    BootcampResponseDto toBootcampResponseDto(Bootcamp bootcamp);
}
