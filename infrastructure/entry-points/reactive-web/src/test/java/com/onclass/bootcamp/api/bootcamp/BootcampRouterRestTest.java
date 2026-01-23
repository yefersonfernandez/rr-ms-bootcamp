package com.onclass.bootcamp.api.bootcamp;

import com.onclass.bootcamp.api.config.BootcampPath;
import com.onclass.bootcamp.api.dto.request.BootcampRequestDto;
import com.onclass.bootcamp.api.dto.response.BootcampResponseDto;
import com.onclass.bootcamp.api.dto.response.BootcampWithCapabilitiesResponseDto;
import com.onclass.bootcamp.api.mapper.BootcampMapper;
import com.onclass.bootcamp.api.utils.ValidatorUtil;
import com.onclass.bootcamp.enums.ExceptionStatusCode;
import com.onclass.bootcamp.model.bootcamp.Bootcamp;
import com.onclass.bootcamp.model.bootcamp.BootcampWithCapabilities;
import com.onclass.bootcamp.usecase.bootcamp.BootcampUseCase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@TestPropertySource(properties = {
        "routes.paths.bootcamps=/bootcamp/api/v1/bootcamps",
        "routes.paths.bootcampsList=/bootcamp/api/v1/bootcamps/list",
        "routes.paths.delete-bootcamp-by-id=/bootcamp/api/v1/bootcamps/{bootcampId}",
        "routes.paths.validate-conflicts=/bootcamp/api/v1/bootcamps/{id}/conflicts"
})
@ContextConfiguration(classes = {BootcampRouterRest.class, BootcampHandler.class, BootcampPath.class, ValidatorUtil.class})
@WebFluxTest
class BootcampRouterRestTest {
    private static final String BOOTCAMPS_PATH = "/bootcamp/api/v1/bootcamps";
    private static final String BOOTCAMPS_LIST_PATH = "/bootcamp/api/v1/bootcamps/list";
    private static final String DELETE_BOOTCAMP_PATH = "/bootcamp/api/v1/bootcamps/1";
    private static final String BOOTCAMP_NAME = "Java Bootcamp";
    private static final String BOOTCAMP_DESCRIPTION = "Bootcamp for Java";
    private static final int BOOTCAMP_DURATION = 30;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private BootcampPath bootcampPath;

    @MockitoBean
    private BootcampUseCase bootcampUseCase;
    @MockitoBean
    private BootcampMapper bootcampMapper;
    @MockitoBean
    private ValidatorUtil validatorUtil;

    private BootcampRequestDto validRequestDto;
    private BootcampResponseDto bootcampResponseDto;
    private BootcampWithCapabilitiesResponseDto bootcampWithCapabilitiesResponseDto;
    private Bootcamp bootcampModel;

    @BeforeEach
    void setUp() {
        validRequestDto = new BootcampRequestDto(BOOTCAMP_NAME, BOOTCAMP_DESCRIPTION, null, BOOTCAMP_DURATION, null);
        bootcampResponseDto = new BootcampResponseDto(1L, BOOTCAMP_NAME, BOOTCAMP_DESCRIPTION, null, BOOTCAMP_DURATION, 2);
        bootcampWithCapabilitiesResponseDto = new BootcampWithCapabilitiesResponseDto(1L, BOOTCAMP_NAME, BOOTCAMP_DESCRIPTION, null, BOOTCAMP_DURATION, null);

        bootcampModel = new Bootcamp(1L, BOOTCAMP_NAME, BOOTCAMP_DESCRIPTION, null, BOOTCAMP_DURATION, null, null);
    }

    @Test
    @DisplayName("Should load path property from BootcampPath")
    void shouldLoadBootcampPathProperty() {
        Assertions.assertThat(bootcampPath.getBootcamps()).isEqualTo(BOOTCAMPS_PATH);
    }

    @Test
    @DisplayName("POST /bootcamps - listenSaveBootcamp: should return 201 when bootcamp is created")
    void post_saveBootcamp_shouldReturnCreated() {
        when(validatorUtil.validate(any())).thenReturn(Mono.just(validRequestDto));
        when(bootcampMapper.toModel(any())).thenReturn(bootcampModel);
        when(bootcampUseCase.saveBootcamp(any())).thenReturn(Mono.just(bootcampModel));
        when(bootcampMapper.toBootcampResponseDto(any())).thenReturn(bootcampResponseDto);

        webTestClient.post()
                .uri(BOOTCAMPS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.code").isEqualTo(ExceptionStatusCode.CREATED.status())
                .jsonPath("$.data.name").isEqualTo(BOOTCAMP_NAME)
                .jsonPath("$.data.description").isEqualTo(BOOTCAMP_DESCRIPTION);
    }

    @Test
    @DisplayName("GET /bootcamps/list - listenListBootcamps: should return 200 when bootcamps are listed")
    void get_listBootcamps_shouldReturnOk() {
        when(bootcampUseCase.getBootcampsWithCapabilities(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(Flux.just(new BootcampWithCapabilities()));

        when(bootcampMapper.toBootcampWithCapabilitiesResponseDto(any()))
                .thenReturn(bootcampWithCapabilitiesResponseDto);

        webTestClient.get()
                .uri(BOOTCAMPS_LIST_PATH + "?page=0&size=10&sortBy=name&order=asc")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(ExceptionStatusCode.OK.status())
                .jsonPath("$.data.content[0].name").isEqualTo(BOOTCAMP_NAME);
    }

    @Test
    @DisplayName("DELETE /bootcamps/{id} - listenDeleteBootcamp: should return 204 when bootcamp is deleted")
    void delete_bootcamp_shouldReturnNoContent() {
        when(bootcampUseCase.deleteBootcamp(anyLong())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(DELETE_BOOTCAMP_PATH)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("GET /bootcamps/{id}/conflicts - should return 200")
    void get_validateConflicts_shouldReturnOk() {
        when(bootcampUseCase.validateConflicts(anyLong(), anyList()))
                .thenReturn(Mono.just(true));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/bootcamp/api/v1/bootcamps/1/conflicts")
                        .queryParam("ids", "2")
                        .queryParam("ids", "3")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }
}