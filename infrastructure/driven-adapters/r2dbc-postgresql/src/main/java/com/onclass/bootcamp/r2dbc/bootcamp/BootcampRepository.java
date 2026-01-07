package com.onclass.bootcamp.r2dbc.bootcamp;

import com.onclass.bootcamp.r2dbc.entity.BootcampEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface BootcampRepository extends ReactiveCrudRepository<BootcampEntity, Long>, ReactiveQueryByExampleExecutor<BootcampEntity> {
    Mono<BootcampEntity> findByName(String name);
}
