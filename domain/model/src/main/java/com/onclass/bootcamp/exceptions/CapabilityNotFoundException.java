package com.onclass.bootcamp.exceptions;


import com.onclass.bootcamp.enums.ExceptionStatusCode;

public class CapabilityNotFoundException extends BusinessException {
    public CapabilityNotFoundException(String message) {
        super(ExceptionStatusCode.NOT_FOUND, message, 404);
    }
}
