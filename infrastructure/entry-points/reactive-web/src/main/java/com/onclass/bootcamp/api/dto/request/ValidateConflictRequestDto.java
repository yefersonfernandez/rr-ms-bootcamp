package com.onclass.bootcamp.api.dto.request;

import java.util.List;

public record ValidateConflictRequestDto(
        Long newBootcampId,
        List<Long> enrolledBootcampIds
) {}