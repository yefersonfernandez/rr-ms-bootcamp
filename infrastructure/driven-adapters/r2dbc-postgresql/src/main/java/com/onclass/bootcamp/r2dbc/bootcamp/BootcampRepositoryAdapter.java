package com.onclass.bootcamp.r2dbc.bootcamp;

import com.onclass.bootcamp.model.bootcamp.Bootcamp;
import com.onclass.bootcamp.model.bootcamp.gateways.BootcampRepositoryPort;
import com.onclass.bootcamp.r2dbc.entity.BootcampEntity;
import com.onclass.bootcamp.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
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
}
