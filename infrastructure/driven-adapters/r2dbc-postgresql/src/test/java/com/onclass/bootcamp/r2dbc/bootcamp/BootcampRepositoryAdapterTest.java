package com.onclass.bootcamp.r2dbc.bootcamp;

import com.onclass.bootcamp.model.bootcamp.Bootcamp;
import com.onclass.bootcamp.r2dbc.entity.BootcampEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootcampRepositoryAdapterTest {

    @InjectMocks
    private BootcampRepositoryAdapter adapter;
    @Mock
    private BootcampRepository repository;
    @Mock
    private ObjectMapper mapper;

    @Test
    @DisplayName("saveBootcamp should save and return bootcamp")
    void saveBootcamp_shouldSaveAndReturn() {
        Bootcamp bootcamp = Bootcamp.builder().id(1L).name("Java Bootcamp").build();
        BootcampEntity entity = new BootcampEntity();

        when(mapper.map(bootcamp, BootcampEntity.class)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(entity));
        when(mapper.map(entity, Bootcamp.class)).thenReturn(bootcamp);

        StepVerifier.create(adapter.saveBootcamp(bootcamp))
                .expectNext(bootcamp)
                .verifyComplete();
    }

    @Test
    @DisplayName("findBootcampById should return bootcamp")
    void findBootcampById_shouldReturn() {
        BootcampEntity entity = new BootcampEntity();
        Bootcamp bootcamp = Bootcamp.builder().id(1L).name("Java Bootcamp").build();
        when(repository.findById(1L)).thenReturn(Mono.just(entity));
        when(mapper.map(entity, Bootcamp.class)).thenReturn(bootcamp);
        StepVerifier.create(adapter.findBootcampById(1L))
                .expectNext(bootcamp)
                .verifyComplete();
    }

    @Test
    @DisplayName("findBootcampByName should return bootcamp")
    void findBootcampByName_shouldReturn() {
        BootcampEntity entity = new BootcampEntity();
        Bootcamp bootcamp = Bootcamp.builder().id(1L).name("Java Bootcamp").build();
        when(repository.findByName("Java Bootcamp")).thenReturn(Mono.just(entity));
        when(mapper.map(entity, Bootcamp.class)).thenReturn(bootcamp);
        StepVerifier.create(adapter.findBootcampByName("Java Bootcamp"))
                .expectNext(bootcamp)
                .verifyComplete();
    }

    @Test
    @DisplayName("findBootcampsPagedAndSorted should return paged and sorted bootcamps")
    void findBootcampsPagedAndSorted_shouldReturn() {
        BootcampEntity entity = new BootcampEntity();
        Bootcamp bootcamp = Bootcamp.builder().id(1L).name("Java Bootcamp").build();
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        when(repository.findAllBy(pageRequest)).thenReturn(Flux.just(entity));
        when(mapper.map(entity, Bootcamp.class)).thenReturn(bootcamp);
        StepVerifier.create(adapter.findBootcampsPagedAndSorted(0, 10, "name", "asc"))
                .expectNext(bootcamp)
                .verifyComplete();
    }

    @Test
    @DisplayName("findAllByIds should return bootcamps by ids")
    void findAllByIds_shouldReturn() {
        BootcampEntity entity = new BootcampEntity();
        Bootcamp bootcamp = Bootcamp.builder().id(1L).name("Java Bootcamp").build();
        List<Long> ids = List.of(1L);
        when(repository.findAllByIdIn(ids)).thenReturn(Flux.just(entity));
        when(mapper.map(entity, Bootcamp.class)).thenReturn(bootcamp);
        StepVerifier.create(adapter.findAllByIds(ids))
                .expectNext(bootcamp)
                .verifyComplete();
    }

    @Test
    @DisplayName("deleteBootcamp should delete and complete")
    void deleteBootcamp_shouldDeleteAndComplete() {
        when(repository.deleteById(1L)).thenReturn(Mono.empty());
        StepVerifier.create(adapter.deleteBootcamp(1L))
                .verifyComplete();
    }
}

