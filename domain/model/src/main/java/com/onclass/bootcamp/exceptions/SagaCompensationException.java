package com.onclass.bootcamp.exceptions;


import com.onclass.bootcamp.enums.ExceptionStatusCode;

public class SagaCompensationException extends BusinessException {
    public SagaCompensationException(String message) {
        super(ExceptionStatusCode.INTERNAL_SERVER_ERROR, message, 500);
    }
}
