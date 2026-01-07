package com.onclass.bootcamp.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Bootcamp Microservice",
                description = "Manages bootcamps and their association with capabilities."
        )
)
public class SwaggerConfig {}
