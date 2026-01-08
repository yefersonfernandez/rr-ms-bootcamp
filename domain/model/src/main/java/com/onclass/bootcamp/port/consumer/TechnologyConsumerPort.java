package com.onclass.bootcamp.port.consumer;

import com.onclass.bootcamp.model.technology.TechnologySummary;
import reactor.core.publisher.Flux;


public interface TechnologyConsumerPort {
    Flux<TechnologySummary> getTechnologiesByCapabilityId(Long capabilityId);
}
