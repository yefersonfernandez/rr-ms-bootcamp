package com.onclass.bootcamp.port.consumer;

import com.onclass.bootcamp.model.capability.CapabilitySummary;
import reactor.core.publisher.Flux;

public interface BootcampCapabilityConsumerPort {
    Flux<CapabilitySummary> getCapabilitiesByBootcampId(Long bootcampId);
}
