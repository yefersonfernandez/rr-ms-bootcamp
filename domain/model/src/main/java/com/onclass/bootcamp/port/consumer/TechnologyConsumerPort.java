package com.onclass.bootcamp.port.consumer;

import com.onclass.bootcamp.model.technology.TechnologySummary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CapabilityTechnologyConsumerPort {
    Flux<TechnologySummary> getTechnologiesByCapabilityId(Long capabilityId);

}
