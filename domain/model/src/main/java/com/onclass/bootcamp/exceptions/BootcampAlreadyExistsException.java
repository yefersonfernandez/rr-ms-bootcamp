package com.onclass.bootcamp.exceptions;

import com.onclass.bootcamp.enums.ExceptionStatusCode;

public class BootcampAlreadyExistsException extends BusinessException {
    public BootcampAlreadyExistsException(String message) {
        super(ExceptionStatusCode.CONFLICT, message, 409);
    }
}

