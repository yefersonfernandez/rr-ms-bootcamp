package com.onclass.bootcamp.port.consumer;

import reactor.core.publisher.Mono;

import java.util.List;

public interface CapabilityAssociationConsumerPort {

    Mono<Void> deleteAssociationsByBootcampId(Long bootcampId);
    Mono<Void> deleteCapabilityById(Long capabilityId);
}
