package com.onclass.bootcamp.usecase.bootcamp;

import com.onclass.bootcamp.enums.ExceptionMessages;
import com.onclass.bootcamp.exceptions.BootcampAlreadyExistsException;
import com.onclass.bootcamp.exceptions.BootcampCapabilitiesCountException;
import com.onclass.bootcamp.model.bootcamp.Bootcamp;
import com.onclass.bootcamp.model.bootcamp.gateways.BootcampRepositoryPort;
import com.onclass.bootcamp.port.consumer.CapabilityConsumerPort;
import com.onclass.bootcamp.port.consumer.TechnologyConsumerPort;
import com.onclass.bootcamp.port.sqs.SqsSenderPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootcampUseCaseTest {
    @Mock
    private BootcampRepositoryPort bootcampRepositoryPort;
    @Mock
    private CapabilityConsumerPort capabilityConsumerPort;
    @Mock
    private TechnologyConsumerPort technologyConsumerPort;
    @Mock
    private SqsSenderPort sqsSenderPort;

    @InjectMocks
    private BootcampUseCase bootcampUseCase;

    private static final String BOOTCAMP_NAME = "TestBootcamp";

    @Test
    @DisplayName("Should fail if Bootcamp already exists by name")
    void saveBootcamp_shouldThrowExceptionWhenBootcampAlreadyExists() {
        Bootcamp bootcamp = Bootcamp.builder().name(BOOTCAMP_NAME).capabilityIds(List.of(1L)).build();
        when(bootcampRepositoryPort.findBootcampByName(BOOTCAMP_NAME)).thenReturn(Mono.just(new Bootcamp()));

        StepVerifier.create(bootcampUseCase.saveBootcamp(bootcamp))
                .expectErrorMatches(throwable -> throwable instanceof BootcampAlreadyExistsException
                        && throwable.getMessage().equals(ExceptionMessages.BOOTCAMP_ALREADY_EXISTS.format(BOOTCAMP_NAME)))
                .verify();
    }

    @Test
    @DisplayName("Should fail if capability count is invalid")
    void saveBootcamp_shouldThrowExceptionWhenCapabilitiesCountInvalid() {
        Bootcamp bootcamp = Bootcamp.builder().name(BOOTCAMP_NAME).capabilityIds(List.of()).build();

        StepVerifier.create(bootcampUseCase.saveBootcamp(bootcamp))
                .expectErrorMatches(BootcampCapabilitiesCountException.class::isInstance)
                .verify();
    }

    @Test
    @DisplayName("Should save successfully and send report to SQS")
    void saveBootcamp_shouldSaveSuccessfullyWhenNotExists() {
        Bootcamp bootcamp = Bootcamp.builder().id(1L).name(BOOTCAMP_NAME).capabilityIds(List.of(1L)).build();

        when(bootcampRepositoryPort.findBootcampByName(BOOTCAMP_NAME)).thenReturn(Mono.empty());
        when(bootcampRepositoryPort.saveBootcamp(any())).thenReturn(Mono.just(bootcamp));
        when(capabilityConsumerPort.associateCapabilities(anyLong(), anyList())).thenReturn(Mono.empty());

        when(sqsSenderPort.sendBootcampReportMessage(any())).thenReturn(Mono.empty());

        StepVerifier.create(bootcampUseCase.saveBootcamp(bootcamp))
                .expectNext(bootcamp)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should list bootcamps with their capabilities and technologies")
    void getBootcampsWithCapabilities_shouldReturnFlux() {
        Bootcamp bootcamp = Bootcamp.builder().id(1L).name(BOOTCAMP_NAME).capabilityIds(List.of(1L)).build();

        when(bootcampRepositoryPort.findBootcampsPagedAndSorted(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(Flux.just(bootcamp));
        when(capabilityConsumerPort.getCapabilitiesByBootcampId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(bootcampUseCase.getBootcampsWithCapabilities(0, 10, "name", "asc"))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should delete bootcamp and its associations")
    void deleteBootcamp_shouldDeleteSuccessfully() {
        Bootcamp bootcamp = Bootcamp.builder().id(1L).name(BOOTCAMP_NAME).build();

        when(bootcampRepositoryPort.findBootcampById(1L)).thenReturn(Mono.just(bootcamp));
        when(capabilityConsumerPort.deleteAssociatedDataByBootcampId(1L)).thenReturn(Mono.empty());
        when(bootcampRepositoryPort.deleteBootcamp(1L)).thenReturn(Mono.empty());

        StepVerifier.create(bootcampUseCase.deleteBootcamp(1L))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should validate schedule conflicts and return true if none exist")
    void validateConflicts_shouldReturnTrueWhenNoConflicts() {
        Bootcamp candidate = Bootcamp.builder().id(1L).name(BOOTCAMP_NAME).build();

        when(bootcampRepositoryPort.findBootcampById(1L)).thenReturn(Mono.just(candidate));
        when(bootcampRepositoryPort.findAllByIds(anyList())).thenReturn(Flux.empty());

        StepVerifier.create(bootcampUseCase.validateConflicts(1L, List.of(2L, 3L)))
                .expectNext(true)
                .verifyComplete();
    }
}