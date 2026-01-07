package com.onclass.bootcamp.consumer.dto.response;


import com.onclass.bootcamp.model.capability.CapabilitySummary;

import java.util.List;

public record CapabilityListResponseDto(List<CapabilitySummary> data) {}
