package com.onclass.bootcamp.model.bootcamp.gateways;

import com.onclass.bootcamp.model.bootcamp.Bootcamp;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BootcampRepositoryPort {
    Mono<Bootcamp> saveBootcamp(Bootcamp bootcamp);
    Mono<Bootcamp> findBootcampById(Long bootcampId);
    Mono<Bootcamp> findBootcampByName(String name);
    Flux<Bootcamp> findBootcampsPagedAndSorted(int page, int size, String sortBy, String order);
    Flux<Bootcamp> findAllByIds(List<Long> bootcampIds);
    Mono<Void> deleteBootcamp(Long bootcampId);
}
