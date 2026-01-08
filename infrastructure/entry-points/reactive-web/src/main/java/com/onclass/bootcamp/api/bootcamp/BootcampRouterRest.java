package com.onclass.bootcamp.api.bootcamp;

import com.onclass.bootcamp.api.config.BootcampPath;
import com.onclass.bootcamp.api.openapi.BootcampOpenApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

@Configuration
@RequiredArgsConstructor
public class BootcampRouterRest {
    private final BootcampPath bootcampPath;

    @Bean
    public RouterFunction<ServerResponse> routerFunction(BootcampHandler handler) {
        return route()
                .POST(bootcampPath.getBootcamps(), handler::listenSaveBootcamp, BootcampOpenApi::saveBootcamp)
                .GET(bootcampPath.getBootcampsList(), handler::listenListBootcamps, BootcampOpenApi::listBootcamps)
                .DELETE(bootcampPath.getDeleteBootcampById(), handler::listenDeleteBootcamp, BootcampOpenApi::deleteBootcamp)
                .build();
    }
}
