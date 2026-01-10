package com.onclass.bootcamp.r2dbc.bootcamp;

import com.onclass.bootcamp.r2dbc.entity.BootcampEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BootcampRepository extends ReactiveCrudRepository<BootcampEntity, Long>, ReactiveQueryByExampleExecutor<BootcampEntity> {
    Mono<BootcampEntity> findByName(String name);
    Flux<BootcampEntity> findAllBy(Pageable pageable);
    Flux<BootcampEntity> findAllByIdIn(List<Long> bootcampIds);
}
