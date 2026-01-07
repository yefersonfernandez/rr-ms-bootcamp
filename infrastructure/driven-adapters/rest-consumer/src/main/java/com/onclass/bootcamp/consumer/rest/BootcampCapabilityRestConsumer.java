package com.onclass.bootcamp.consumer.rest;

import com.onclass.bootcamp.consumer.dto.response.CapabilityListResponseDto;
import com.onclass.bootcamp.enums.ExceptionMessages;
import com.onclass.bootcamp.exceptions.CapabilityMicroserviceException;
import com.onclass.bootcamp.model.capability.CapabilitySummary;
import com.onclass.bootcamp.port.consumer.BootcampCapabilityConsumerPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class BootcampCapabilityRestConsumer implements BootcampCapabilityConsumerPort {
    private static final String GET_CAPABILITIES_URL = "/capability/api/v1/botcamps/{bootcampId}/capabilities";

    private final WebClient capabilityWebClient;

    public BootcampCapabilityRestConsumer(@Qualifier("capabilityWebClient") WebClient capabilityWebClient) {
        this.capabilityWebClient = capabilityWebClient;
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
}
