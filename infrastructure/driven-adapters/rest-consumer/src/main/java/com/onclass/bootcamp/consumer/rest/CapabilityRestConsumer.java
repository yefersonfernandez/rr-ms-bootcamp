package com.onclass.bootcamp.consumer.rest;

import com.onclass.bootcamp.consumer.dto.response.CapabilityListResponseDto;
import com.onclass.bootcamp.enums.ExceptionMessages;
import com.onclass.bootcamp.exceptions.BootcampCapabilitiesCountException;
import com.onclass.bootcamp.exceptions.CapabilityMicroserviceException;
import com.onclass.bootcamp.exceptions.NotFoundException;
import com.onclass.bootcamp.exceptions.RepeatedCapabilitiesException;
import com.onclass.bootcamp.model.capability.CapabilitySummary;
import com.onclass.bootcamp.port.consumer.CapabilityConsumerPort;
import com.onclass.bootcamp.consumer.dto.request.AssociationRequestDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class CapabilityRestConsumer implements CapabilityConsumerPort {
    private static final String ASSOCIATE_CAPABILITIES_URL = "/capability/api/v1/capabilities/bootcamp-associations";
    private static final String GET_CAPABILITIES_URL = "/capability/api/v1/botcamps/{bootcampId}/capabilities";
    private static final String DELETE_BOOTCAMP_URL = "/capability/api/v1/bootcamps/{bootcampId}";

    private final WebClient capabilityWebClient;

    public CapabilityRestConsumer(@Qualifier("capabilityWebClient") WebClient capabilityWebClient) {
        this.capabilityWebClient = capabilityWebClient;
    }

    @CircuitBreaker(name = "associateCapabilitiesCB")
    public Mono<Void> associateCapabilities(Long bootcampId, List<Long> capabilityIds) {
        return capabilityWebClient.post()
                .uri(ASSOCIATE_CAPABILITIES_URL)
                .bodyValue(new AssociationRequestDto(bootcampId, capabilityIds))
                .retrieve()
                .onStatus(status -> status.value() == 400, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new BootcampCapabilitiesCountException(body)))
                )
                .onStatus(status -> status.value() == 409, response ->
                        response.bodyToMono(String.class).flatMap(body -> Mono.error(new RepeatedCapabilitiesException(body)))
                )
                .onStatus(status -> status.value() == 404, response ->
                        response.bodyToMono(String.class).flatMap(body -> Mono.error(new NotFoundException(body)))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class).flatMap(body -> Mono.error(new RuntimeException(body)))
                )
                .toBodilessEntity()
                .then();
    }

    @Override
    public Flux<CapabilitySummary> getCapabilitiesByBootcampId(Long bootcampId) {
        return capabilityWebClient.get()
                .uri(GET_CAPABILITIES_URL, bootcampId)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class).flatMap(body ->
                                Mono.error(new CapabilityMicroserviceException(
                                        ExceptionMessages.WEB_CLIENT_INTERNAL_SERVER_ERROR.format(body)
                                ))
                        )
                )
                .bodyToMono(CapabilityListResponseDto.class)
                .flatMapMany(response -> Flux.fromIterable(response.data() != null ? response.data() : List.of()));
    }

    @Override
    public Mono<Void> deleteAssociatedDataByBootcampId(Long bootcampId) {
        return capabilityWebClient.delete()
                .uri(DELETE_BOOTCAMP_URL, bootcampId)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class).flatMap(body ->
                                Mono.error(new CapabilityMicroserviceException(
                                        ExceptionMessages.WEB_CLIENT_INTERNAL_SERVER_ERROR.format(body)
                                ))
                        )
                )
                .toBodilessEntity()
                .then();
    }
}
