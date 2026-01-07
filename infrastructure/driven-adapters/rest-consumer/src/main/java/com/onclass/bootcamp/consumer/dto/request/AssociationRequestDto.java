package com.onclass.bootcamp.consumer.dto.request;

import java.util.List;

public record AssociationRequestDto(Long bootcampId, List<Long> capabilityIds) {}
