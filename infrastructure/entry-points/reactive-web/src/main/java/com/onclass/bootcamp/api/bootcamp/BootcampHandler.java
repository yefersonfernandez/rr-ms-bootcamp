package com.onclass.bootcamp.api.bootcamp;

import com.onclass.bootcamp.api.dto.request.BootcampRequestDto;
import com.onclass.bootcamp.api.dto.response.ApiResponseDto;
import com.onclass.bootcamp.api.mapper.BootcampMapper;
import com.onclass.bootcamp.api.utils.HandlersResponseUtil;
import com.onclass.bootcamp.api.utils.ValidatorUtil;
import com.onclass.bootcamp.enums.ExceptionStatusCode;
import com.onclass.bootcamp.model.bootcamp.Bootcamp;
import com.onclass.bootcamp.usecase.bootcamp.BootcampUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class BootcampHandler {
    private final BootcampUseCase bootcampUseCase;
    private final BootcampMapper bootcampMapper;
    private final ValidatorUtil validatorUtil;

    public Mono<ServerResponse> listenSaveBootcamp(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(BootcampRequestDto.class)
                .flatMap(validatorUtil::validate)
                .map(bootcampMapper::toModel)
                .flatMap(bootcampUseCase::saveBootcamp)
                .map(bootcampMapper::toBootcampResponseDto)
                .flatMap(savedBootcamp -> ServerResponse.created(URI.create(""))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(HandlersResponseUtil.buildBodySuccessResponse(ExceptionStatusCode.CREATED.status(), savedBootcamp))

                );
    }
}
