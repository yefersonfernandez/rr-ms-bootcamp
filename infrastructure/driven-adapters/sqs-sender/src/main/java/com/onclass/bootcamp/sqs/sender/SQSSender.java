package com.onclass.bootcamp.sqs.sender;

import tools.jackson.databind.ObjectMapper;
import com.onclass.bootcamp.port.sqs.SqsSenderPort;
import com.onclass.bootcamp.port.sqs.model.BootcampMessage;
import com.onclass.bootcamp.sqs.sender.config.SQSSenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements SqsSenderPort {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> sendBootcampReportMessage(BootcampMessage message) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(message))
                .map(rawMessage -> buildRequest(rawMessage, properties.bootcampReportQueueUrl()))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnSuccess(response -> log.info(
                        "SQS_PUBLISH_SUCCESS | Bootcamp: {} (ID: {})",
                        message.getName(),
                        message.getBootcampId()
                ))
                .doOnError(error -> log.error(
                        "SQS_PUBLISH_ERROR | Bootcamp: {} (ID: {})",
                        message.getName(),
                        message.getBootcampId()
                ))
                .then();
    }

    private SendMessageRequest buildRequest(String message, String queueUrl) {
        return SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .build();
    }
}
