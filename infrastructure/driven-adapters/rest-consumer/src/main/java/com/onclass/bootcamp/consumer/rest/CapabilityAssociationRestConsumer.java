package com.onclass.bootcamp.consumer.rest;

import com.onclass.bootcamp.exceptions.BootcampCapabilitiesCountException;
import com.onclass.bootcamp.exceptions.CapabilityNotFoundException;
import com.onclass.bootcamp.exceptions.RepeatedCapabilitiesException;
import com.onclass.bootcamp.port.consumer.CapabilityAssociationConsumerPort;
import com.onclass.bootcamp.consumer.dto.request.AssociationRequestDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CapabilityAssociationRestConsumer implements CapabilityAssociationConsumerPort {
    private static final String ASSOCIATE_CAPABILITIES_URL = "/capability/api/v1/capabilities/bootcamp-associations";

    private final WebClient client;

    @CircuitBreaker(name = "associateCapabilitiesCB")
    public Mono<Void> associateCapabilities(Long bootcampId, List<Long> capabilityIds) {
        return client.post()
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
                        response.bodyToMono(String.class).flatMap(body -> Mono.error(new CapabilityNotFoundException(body)))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class).flatMap(body -> Mono.error(new RuntimeException(body)))
                )
                .toBodilessEntity()
                .then();
    }

}
