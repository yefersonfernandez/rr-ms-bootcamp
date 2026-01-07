package com.onclass.bootcamp.port.consumer;

import reactor.core.publisher.Mono;

import java.util.List;

public interface CapabilityAssociationConsumerPort {
    Mono<Void> associateCapabilities(Long bootcampId, List<Long> capabilityIds);
}
