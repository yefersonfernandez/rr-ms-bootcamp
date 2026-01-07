package com.onclass.bootcamp.exceptions;


import com.onclass.bootcamp.enums.ExceptionStatusCode;

public class RepeatedCapabilitiesException extends BusinessException {
    public RepeatedCapabilitiesException(String message) {
        super(ExceptionStatusCode.CONFLICT, message, 409);
    }
}
