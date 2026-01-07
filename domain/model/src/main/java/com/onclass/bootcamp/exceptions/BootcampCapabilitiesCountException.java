package com.onclass.bootcamp.exceptions;


import com.onclass.bootcamp.enums.ExceptionStatusCode;

public class BootcampCapabilitiesCountException extends BusinessException {
    public BootcampCapabilitiesCountException(String message) {
        super(ExceptionStatusCode.BAD_REQUEST, message, 400);
    }
}
