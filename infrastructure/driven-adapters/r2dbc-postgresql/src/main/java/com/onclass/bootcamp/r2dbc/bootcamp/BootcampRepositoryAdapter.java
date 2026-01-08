package com.onclass.bootcamp.r2dbc.bootcamp;

import com.onclass.bootcamp.model.bootcamp.Bootcamp;
import com.onclass.bootcamp.model.bootcamp.gateways.BootcampRepositoryPort;
import com.onclass.bootcamp.r2dbc.entity.BootcampEntity;
import com.onclass.bootcamp.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Slf4j
public class BootcampRepositoryAdapter extends ReactiveAdapterOperations<
        Bootcamp,
        BootcampEntity,
        Long,
        BootcampRepository
        > implements BootcampRepositoryPort {

    public BootcampRepositoryAdapter(BootcampRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Bootcamp.class));
    }

    @Override
    public Mono<Bootcamp> saveBootcamp(Bootcamp bootcamp) {
        return super.save(bootcamp);
    }

    @Override
    public Mono<Void> deleteBootcamp(Long bootcampId) {
        return repository.deleteById(bootcampId);
    }

    @Override
    public Mono<Bootcamp> findBootcampByName(String name) {
        return repository.findByName(name)
                .map(super::toEntity);
    }

    @Override
    public Mono<Bootcamp> findBootcampById(Long bootcampId) {
        return repository.findById(bootcampId)
                .map(super::toEntity);
    }

    @Override
    public Flux<Bootcamp> findBootcampsPagedAndSorted(int page, int size, String sortBy, String order) {
        return Mono.just(order)
                .map(ord -> ord.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC)
                .map(direction -> Sort.by(direction, sortBy))
                .map(sort -> PageRequest.of(page, size, sort))
                .flatMapMany(pageRequest -> repository.findAllBy(pageRequest))
                .map(super::toEntity)
                .doOnNext(boc -> log.info("[DB] bootcamp_id={}, name={}, capabilityCount={}", boc.getId(), boc.getName(), boc.getCapabilityCount()));
    }
}
