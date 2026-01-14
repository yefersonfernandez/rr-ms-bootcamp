package com.onclass.bootcamp.port.sqs;

import com.onclass.bootcamp.port.sqs.model.BootcampMessage;
import reactor.core.publisher.Mono;

public interface SqsSenderPort {
    Mono<Void> sendBootcampReportMessage(BootcampMessage message);
}
