package com.onclass.bootcamp.api.bootcamp;

import com.onclass.bootcamp.api.dto.request.BootcampRequestDto;
import com.onclass.bootcamp.api.dto.request.ValidateConflictRequestDto;
import com.onclass.bootcamp.api.dto.response.ApiResponseDto;
import com.onclass.bootcamp.api.mapper.BootcampMapper;
import com.onclass.bootcamp.api.utils.HandlersResponseUtil;
import com.onclass.bootcamp.api.utils.ValidatorUtil;
import com.onclass.bootcamp.enums.ExceptionStatusCode;
import com.onclass.bootcamp.model.bootcamp.Bootcamp;
import com.onclass.bootcamp.usecase.bootcamp.BootcampUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;

import static com.onclass.bootcamp.api.constants.BootcampHandlerLogMessages.*;
import static com.onclass.bootcamp.api.utils.HandlersResponseUtil.buildBodySuccessResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class BootcampHandler {
    private final BootcampUseCase bootcampUseCase;
    private final BootcampMapper bootcampMapper;
    private final ValidatorUtil validatorUtil;

    public Mono<ServerResponse> listenSaveBootcamp(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(BootcampRequestDto.class)
                .doOnNext(bootcampRequestDto -> log.info(BOOTCAMP_REQUEST_RECEIVED, bootcampRequestDto))
                .flatMap(validatorUtil::validate)
                .map(bootcampMapper::toModel)
                .flatMap(bootcampUseCase::saveBootcamp)
                .map(bootcampMapper::toBootcampResponseDto)
                .flatMap(savedBootcamp -> ServerResponse.created(URI.create(""))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(buildBodySuccessResponse(ExceptionStatusCode.CREATED.status(), savedBootcamp))
                );
    }

    public Mono<ServerResponse> listenListBootcamps(ServerRequest request) {
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        String sortBy = request.queryParam("sortBy").orElse("name");
        String order = request.queryParam("order").orElse("asc");

        log.info(BOOTCAMP_LIST_REQUEST, page, size, sortBy, order);

        return bootcampUseCase.getBootcampsWithCapabilities(page, size, sortBy, order)
                .map(bootcampMapper::toBootcampWithCapabilitiesResponseDto)
                .collectList()
                .doOnNext(dtoList -> log.info(BOOTCAMP_LIST_MAPPED, dtoList))
                .map(dtoList -> new PageImpl<>(dtoList, PageRequest.of(page, size), dtoList.size()))
                .flatMap(pageResult -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(buildBodySuccessResponse(ExceptionStatusCode.OK.status(), pageResult))
                );
    }

    public Mono<ServerResponse> listenDeleteBootcamp(ServerRequest request) {
        Long bootcampId = Long.valueOf(request.pathVariable("bootcampId"));
        log.info(BOOTCAMP_DELETE_REQUEST, bootcampId);
        return bootcampUseCase.deleteBootcamp(bootcampId)
                .then(ServerResponse.noContent().build())
                .doOnSuccess(resp -> log.info("[HANDLER] Bootcamp {} deleted successfully (cascade)", bootcampId))
                .doOnError(e -> log.error("[HANDLER] Error deleting bootcamp {}: {}", bootcampId, e.getMessage()));
    }

    public Mono<ServerResponse> listenValidateConflicts(ServerRequest request) {
        Long newBootcampId = Long.valueOf(request.pathVariable("id"));

        return Mono.justOrEmpty(request.queryParams().get("ids"))
                .map(ids -> ids.stream()
                        .filter(StringUtils::hasText)
                        .map(Long::valueOf)
                        .toList())
                .defaultIfEmpty(Collections.emptyList())
                .flatMap(enrolledIds ->
                        bootcampUseCase.validateConflicts(newBootcampId, enrolledIds)
                                .flatMap(isValid -> ServerResponse.ok().bodyValue(isValid))
                );
    }
}
