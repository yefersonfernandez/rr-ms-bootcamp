package com.onclass.bootcamp.usecase.bootcamp;

import com.onclass.bootcamp.exceptions.BootcampAlreadyExistsException;
import com.onclass.bootcamp.exceptions.BootcampCapabilitiesCountException;
import com.onclass.bootcamp.exceptions.NotFoundException;
import com.onclass.bootcamp.exceptions.SagaCompensationException;
import com.onclass.bootcamp.model.bootcamp.Bootcamp;
import com.onclass.bootcamp.model.bootcamp.BootcampWithCapabilities;
import com.onclass.bootcamp.model.bootcamp.gateways.BootcampRepositoryPort;
import com.onclass.bootcamp.enums.ExceptionMessages;
import com.onclass.bootcamp.port.consumer.CapabilityConsumerPort;
import com.onclass.bootcamp.port.consumer.TechnologyConsumerPort;
import com.onclass.bootcamp.port.sqs.SqsSenderPort;
import com.onclass.bootcamp.usecase.utils.BootcampUtils;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

import static com.onclass.bootcamp.constants.BootcampConstants.MAX_CAPS;
import static com.onclass.bootcamp.constants.BootcampConstants.MIN_CAPS;
import static com.onclass.bootcamp.usecase.utils.BootcampUtils.*;

@RequiredArgsConstructor
public class BootcampUseCase {
    private final BootcampRepositoryPort bootcampRepositoryPort;
    private final CapabilityConsumerPort capabilityConsumerPort;
    private final TechnologyConsumerPort technologyConsumerPort;
    private final SqsSenderPort sqsSenderPort;

    public Mono<Bootcamp> saveBootcamp(Bootcamp bootcamp) {
        return Mono.just(bootcamp)
                .filter(bc -> isValidCapabilitiesCount(bc.getCapabilityIds(), MIN_CAPS, MAX_CAPS))
                .switchIfEmpty(Mono.error(new BootcampCapabilitiesCountException(
                        ExceptionMessages.BOOTCAMP_CAPABILITIES_COUNT_INVALID.format())))
                .filter(bc -> hasNoRepeatedCapabilities(bc.getCapabilityIds()))
                .switchIfEmpty(Mono.error(new BootcampCapabilitiesCountException(
                        ExceptionMessages.BOOTCAMP_CAPABILITIES_REPEATED.getMessage())))
                .map(BootcampUtils::enrichWithCapabilityCount)
                .flatMap(this::validateUniqueName)
                .flatMap(this::saveAndAssociateCapabilities);
    }

    private Mono<Bootcamp> validateUniqueName(Bootcamp bootcamp) {
        return bootcampRepositoryPort.findBootcampByName(bootcamp.getName())
                .flatMap(existing -> Mono.<Bootcamp>error(new BootcampAlreadyExistsException(
                        ExceptionMessages.BOOTCAMP_ALREADY_EXISTS.format(bootcamp.getName())))
                )
                .switchIfEmpty(Mono.just(bootcamp));
    }

    private Mono<Bootcamp> saveAndAssociateCapabilities(Bootcamp bootcamp) {
        var capabilityIds = bootcamp.getCapabilityIds();
        return bootcampRepositoryPort.saveBootcamp(bootcamp)
                .flatMap(savedBootcamp -> capabilityConsumerPort
                        .associateCapabilities(savedBootcamp.getId(), capabilityIds)
                        .doOnSuccess(unused -> notifyBootcampCreation(savedBootcamp))
                        .thenReturn(savedBootcamp)
                        .onErrorResume(e -> bootcampRepositoryPort.deleteBootcamp(savedBootcamp.getId())
                                .then(Mono.error(new SagaCompensationException(
                                        ExceptionMessages.SAGA_COMPENSATION_ASSOCIATION_FAILURE.getMessage()))))
                );
    }

    private void notifyBootcampCreation(Bootcamp bootcamp) {
        sqsSenderPort.sendBootcampReportMessage(buildBootcampMessage(bootcamp))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public Flux<BootcampWithCapabilities> getBootcampsWithCapabilities(int page, int size, String sortBy, String order) {
        return bootcampRepositoryPort.findBootcampsPagedAndSorted(page, size, sortBy, order)
                .flatMapSequential(bootcamp -> capabilityConsumerPort.getCapabilitiesByBootcampId(bootcamp.getId())
                        .flatMapSequential(capability -> technologyConsumerPort.getTechnologiesByCapabilityId(capability.getId())
                                .collectList()
                                .map(technologies -> buildCapabilitySummaryWithTechnologies(capability, technologies))
                        )
                        .collectList()
                        .map(capabilities -> buildBootcampWithCapabilities(bootcamp, capabilities))
                );
    }

    public Mono<Void> deleteBootcamp(Long bootcampId) {
        return bootcampRepositoryPort.findBootcampById(bootcampId)
                .switchIfEmpty(Mono.error(new NotFoundException(ExceptionMessages.BOOTCAMP_NOT_FOUND.format(bootcampId))))
                .flatMap(bootcamp -> capabilityConsumerPort.deleteAssociatedDataByBootcampId(bootcampId)
                        .then(bootcampRepositoryPort.deleteBootcamp(bootcampId)));
    }

    public Mono<Boolean> validateConflicts(Long newBootcampId, List<Long> enrolledBootcampIds) {
        return bootcampRepositoryPort.findBootcampById(newBootcampId)
                .switchIfEmpty(Mono.error(new NotFoundException(ExceptionMessages.BOOTCAMP_NOT_FOUND.format(newBootcampId))))
                .flatMap(candidateBootcamp ->
                        bootcampRepositoryPort.findAllByIds(enrolledBootcampIds)
                                .any(enrolledBootcamp -> hasScheduleConflict(candidateBootcamp, enrolledBootcamp))
                )
                .map(hasConflict -> !hasConflict);
    }
}
