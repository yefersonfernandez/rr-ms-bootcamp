package com.onclass.bootcamp.exceptions;

import com.onclass.bootcamp.enums.ExceptionStatusCode;

public class CapabilityMicroserviceException extends BusinessException {
    public CapabilityMicroserviceException(String message) {
        super(ExceptionStatusCode.INTERNAL_SERVER_ERROR, message, 500);
    }
}

