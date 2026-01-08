package com.onclass.bootcamp.port.consumer;

import com.onclass.bootcamp.model.capability.CapabilitySummary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CapabilityConsumerPort {
    Mono<Void> associateCapabilities(Long bootcampId, List<Long> capabilityIds);
    Flux<CapabilitySummary> getCapabilitiesByBootcampId(Long bootcampId);
    Mono<Void> deleteAssociatedDataByBootcampId(Long bootcampId);
}
