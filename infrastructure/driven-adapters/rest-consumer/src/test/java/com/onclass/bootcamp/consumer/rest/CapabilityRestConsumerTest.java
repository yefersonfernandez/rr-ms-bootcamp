package com.onclass.bootcamp.consumer.rest;

import com.onclass.bootcamp.enums.ExceptionMessages;
import com.onclass.bootcamp.exceptions.BootcampCapabilitiesCountException;
import com.onclass.bootcamp.exceptions.CapabilityMicroserviceException;
import com.onclass.bootcamp.exceptions.NotFoundException;
import com.onclass.bootcamp.exceptions.RepeatedCapabilitiesException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;

class CapabilityRestConsumerTest {
    private static CapabilityRestConsumer consumer;
    private static MockWebServer mockBackEnd;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        var webClient = WebClient.builder().baseUrl(mockBackEnd.url("/").toString()).build();
        consumer = new CapabilityRestConsumer(webClient);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    @DisplayName("Successful association returns completed Mono<Void>")
    void associateCapabilities_success() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value()));
        var result = consumer.associateCapabilities(1L, List.of(1L, 2L, 3L));
        StepVerifier.create(result).verifyComplete();
    }

    @Test
    @DisplayName("Invalid count returns BootcampCapabilitiesCountException")
    void associateCapabilities_invalidCount() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody("Invalid count"));
        var result = consumer.associateCapabilities(1L, List.of(1L));
        StepVerifier.create(result)
                .expectErrorSatisfies(e -> {
                    assert e instanceof BootcampCapabilitiesCountException;
                    assert e.getMessage().contains("Invalid count");
                })
                .verify();
    }

    @Test
    @DisplayName("Repeated capabilities returns RepeatedCapabilitiesException")
    void associateCapabilities_repeated() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.CONFLICT.value())
                .setBody("Repeated capabilities"));
        var result = consumer.associateCapabilities(1L, List.of(1L, 1L, 2L));
        StepVerifier.create(result)
                .expectErrorSatisfies(e -> {
                    assert e instanceof RepeatedCapabilitiesException;
                    assert e.getMessage().contains("Repeated capabilities");
                })
                .verify();
    }

    @Test
    @DisplayName("Capabilities not found returns NotFoundException")
    void associateCapabilities_notFound() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value())
                .setBody("Not found"));
        var result = consumer.associateCapabilities(1L, List.of(99L));
        StepVerifier.create(result)
                .expectErrorSatisfies(e -> {
                    assert e instanceof NotFoundException;
                    assert e.getMessage().contains("Not found");
                })
                .verify();
    }

    @Test
    @DisplayName("Internal server error returns RuntimeException")
    void associateCapabilities_serverError() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setBody("Server error"));
        var result = consumer.associateCapabilities(1L, List.of(1L, 2L, 3L));
        StepVerifier.create(result)
                .expectErrorSatisfies(e -> {
                    assert e instanceof RuntimeException;
                    assert e.getMessage().contains("Server error");
                })
                .verify();
    }

    @Test
    @DisplayName("Successful capability query returns CapabilitySummary list")
    void getCapabilitiesByBootcampId_success() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"code\":200,\"data\":[{\"id\":1,\"name\":\"Java\"},{\"id\":2,\"name\":\"Spring Boot\"}]}")
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(consumer.getCapabilitiesByBootcampId(123L))
                .expectNextMatches(t -> t.getId().equals(1L) && t.getName().equals("Java"))
                .expectNextMatches(t -> t.getId().equals(2L) && t.getName().equals("Spring Boot"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Empty capability list returns empty Flux")
    void getCapabilitiesByBootcampId_empty() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"code\":200,\"data\":[]}")
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(consumer.getCapabilitiesByBootcampId(999L))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Internal server error returns CapabilityMicroserviceException")
    void getCapabilitiesByBootcampId_serverError() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setBody("Server error")
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(consumer.getCapabilitiesByBootcampId(123L))
                .expectErrorSatisfies(e -> {
                    assert e instanceof CapabilityMicroserviceException;
                    assert e.getMessage().contains(ExceptionMessages.WEB_CLIENT_INTERNAL_SERVER_ERROR.getMessage());
                })
                .verify();
    }

    @Test
    @DisplayName("Successful delete returns completed Mono<Void>")
    void deleteAssociatedDataByBootcampId_success() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value()));
        var result = consumer.deleteAssociatedDataByBootcampId(1L);
        StepVerifier.create(result).verifyComplete();
    }

    @Test
    @DisplayName("Delete with server error returns CapabilityMicroserviceException")
    void deleteAssociatedDataByBootcampId_serverError() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setBody("Server error"));
        var result = consumer.deleteAssociatedDataByBootcampId(1L);
        StepVerifier.create(result)
                .expectErrorSatisfies(e -> {
                    assert e instanceof CapabilityMicroserviceException;
                    assert e.getMessage().contains(ExceptionMessages.WEB_CLIENT_INTERNAL_SERVER_ERROR.getMessage());
                })
                .verify();
    }
}

