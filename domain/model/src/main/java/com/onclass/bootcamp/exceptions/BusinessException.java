package com.onclass.bootcamp.exceptions;


import com.onclass.bootcamp.enums.ExceptionStatusCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ExceptionStatusCode statusCode;
    private final int status;

    public BusinessException(ExceptionStatusCode statusCode, String message, int status) {
        super(message);
        this.statusCode = statusCode;
        this.status = status;
    }
}