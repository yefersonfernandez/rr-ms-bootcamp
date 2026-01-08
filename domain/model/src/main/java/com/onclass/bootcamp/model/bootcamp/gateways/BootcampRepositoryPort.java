package com.onclass.bootcamp.model.bootcamp.gateways;

import com.onclass.bootcamp.model.bootcamp.Bootcamp;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BootcampRepositoryPort {
    Mono<Bootcamp> saveBootcamp(Bootcamp bootcamp);
    Mono<Void> deleteBootcamp(Long bootcampId);
    Mono<Bootcamp> findBootcampByName(String name);
    Mono<Bootcamp> findBootcampById(Long bootcampId);
    Flux<Bootcamp> findBootcampsPagedAndSorted(int page, int size, String sortBy, String order);
}
