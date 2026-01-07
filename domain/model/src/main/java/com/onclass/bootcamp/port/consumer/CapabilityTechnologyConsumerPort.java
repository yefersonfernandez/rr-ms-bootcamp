package com.onclass.bootcamp.port.consumer;

import com.onclass.bootcamp.model.technology.TechnologySummary;
import reactor.core.publisher.Flux;

public interface CapabilityTechnologyConsumerPort {
    Flux<TechnologySummary> getTechnologiesByCapabilityId(Long capabilityId);
}

