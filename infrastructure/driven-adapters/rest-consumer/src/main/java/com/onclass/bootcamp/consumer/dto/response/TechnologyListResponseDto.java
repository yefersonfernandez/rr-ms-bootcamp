package com.onclass.bootcamp.consumer.dto.response;

import com.onclass.bootcamp.model.technology.TechnologySummary;

import java.util.List;

public record TechnologyListResponseDto(List<TechnologySummary> data) {}
