package com.onclass.bootcamp.usecase.bootcamp;

import com.onclass.bootcamp.exceptions.BootcampAlreadyExistsException;
import com.onclass.bootcamp.exceptions.BootcampCapabilitiesCountException;
import com.onclass.bootcamp.exceptions.SagaCompensationException;
import com.onclass.bootcamp.model.bootcamp.Bootcamp;
import com.onclass.bootcamp.model.bootcamp.BootcampWithCapabilities;
import com.onclass.bootcamp.model.bootcamp.gateways.BootcampRepositoryPort;
import com.onclass.bootcamp.port.consumer.BootcampCapabilityConsumerPort;
import com.onclass.bootcamp.port.consumer.CapabilityAssociationConsumerPort;
import com.onclass.bootcamp.enums.ExceptionMessages;
import com.onclass.bootcamp.port.consumer.CapabilityTechnologyConsumerPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.onclass.bootcamp.constants.BootcampConstants.MAX_CAPS;
import static com.onclass.bootcamp.constants.BootcampConstants.MIN_CAPS;
import static com.onclass.bootcamp.usecase.utils.BootcampUtils.buildBootcampWithCapabilities;
import static com.onclass.bootcamp.usecase.utils.BootcampUtils.buildCapabilitySummaryWithTechnologies;
import static com.onclass.bootcamp.usecase.utils.BootcampUtils.hasNoRepeatedCapabilities;
import static com.onclass.bootcamp.usecase.utils.BootcampUtils.isValidCapabilitiesCount;

@RequiredArgsConstructor
public class BootcampUseCase {
    private final BootcampRepositoryPort bootcampRepositoryPort;
    private final CapabilityAssociationConsumerPort capabilityAssociationConsumerPort;
    private final CapabilityTechnologyConsumerPort capabilityTechnologyConsumerPort;
    private final BootcampCapabilityConsumerPort bootcampCapabilityConsumerPort;

    public Mono<Bootcamp> saveBootcamp(Bootcamp bootcamp) {
        bootcamp.setCapabilityCount(bootcamp.getCapabilityIds().size());
        return Mono.just(bootcamp)
                .filter(bc -> isValidCapabilitiesCount(bc.getCapabilityIds(), MIN_CAPS, MAX_CAPS))
                .switchIfEmpty(Mono.error(new BootcampCapabilitiesCountException(
                        ExceptionMessages.BOOTCAMP_CAPABILITIES_COUNT_INVALID.format())))
                .filter(bc -> hasNoRepeatedCapabilities(bc.getCapabilityIds()))
                .switchIfEmpty(Mono.error(new BootcampCapabilitiesCountException(
                        ExceptionMessages.BOOTCAMP_CAPABILITIES_REPEATED.getMessage())))
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
                .flatMap(savedBootcamp -> capabilityAssociationConsumerPort
                        .associateCapabilities(savedBootcamp.getId(), capabilityIds)
                        .thenReturn(savedBootcamp)
                        .onErrorResume(e -> bootcampRepositoryPort.deleteBootcamp(savedBootcamp.getId())
                                .then(Mono.error(new SagaCompensationException(
                                        ExceptionMessages.SAGA_COMPENSATION_ASSOCIATION_FAILURE.getMessage()))
                                ))
                );
    }

    public Flux<BootcampWithCapabilities> getBootcampsWithCapabilities(int page, int size, String sortBy, String order) {
        return bootcampRepositoryPort.findBootcampsPagedAndSorted(page, size, sortBy, order)
                .flatMapSequential(bootcamp -> bootcampCapabilityConsumerPort.getCapabilitiesByBootcampId(bootcamp.getId())
                        .flatMapSequential(capability -> capabilityTechnologyConsumerPort.getTechnologiesByCapabilityId(capability.getId())
                                .collectList()
                                .map(technologies -> buildCapabilitySummaryWithTechnologies(capability, technologies))
                        )
                        .collectList()
                        .map(capabilities -> buildBootcampWithCapabilities(bootcamp, capabilities))
                );
    }
}
