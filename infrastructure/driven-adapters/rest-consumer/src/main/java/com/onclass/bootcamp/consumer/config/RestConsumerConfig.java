package com.onclass.bootcamp.consumer.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Configuration
public class  RestConsumerConfig {

    private final RestConsumerProperties properties;

    public RestConsumerConfig(RestConsumerProperties properties) {
        this.properties = properties;
    }

    @Bean
    public WebClient technologyWebClient() {
        return WebClient.builder()
                .baseUrl(properties.getTechnologyUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(getClientHttpConnector())
                .build();
    }

    @Bean
    public WebClient capabilityWebClient() {
        return WebClient.builder()
                .baseUrl(properties.getCapabilityUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(getClientHttpConnector())
                .build();
    }

    private ClientHttpConnector getClientHttpConnector() {
        int timeout = properties.getTimeout();
        return new ReactorClientHttpConnector(HttpClient.create()
                .compress(true)
                .keepAlive(true)
                .option(CONNECT_TIMEOUT_MILLIS, timeout)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(timeout, MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(timeout, MILLISECONDS));
                }));
    }
}
