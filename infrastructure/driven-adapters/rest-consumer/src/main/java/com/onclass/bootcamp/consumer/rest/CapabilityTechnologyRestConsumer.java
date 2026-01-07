package com.onclass.bootcamp.consumer.rest;

import com.onclass.bootcamp.consumer.dto.response.TechnologyListResponseDto;
import com.onclass.bootcamp.enums.ExceptionMessages;
import com.onclass.bootcamp.exceptions.TechnologyMicroserviceException;
import com.onclass.bootcamp.model.technology.TechnologySummary;
import com.onclass.bootcamp.port.consumer.CapabilityTechnologyConsumerPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class CapabilityTechnologyRestConsumer implements CapabilityTechnologyConsumerPort {
    private static final String GET_TECHNOLOGIES_URL = "/technology/api/v1/capabilities/{capabilityId}/technologies";

    private final WebClient technologyWebClient;

    public CapabilityTechnologyRestConsumer(@Qualifier("technologyWebClient") WebClient technologyWebClient) {
        this.technologyWebClient = technologyWebClient;
    }

    @Override
    public Flux<TechnologySummary> getTechnologiesByCapabilityId(Long capabilityId) {
        return technologyWebClient.get()
                .uri(GET_TECHNOLOGIES_URL, capabilityId)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class).flatMap(body ->
                                Mono.error(new TechnologyMicroserviceException(
                                        ExceptionMessages.WEB_CLIENT_INTERNAL_SERVER_ERROR.format(body)
                                ))
                        )
                )
                .bodyToMono(TechnologyListResponseDto.class)
                .flatMapMany(response -> Flux.fromIterable(response.data() != null ? response.data() : List.of()));
    }
}
