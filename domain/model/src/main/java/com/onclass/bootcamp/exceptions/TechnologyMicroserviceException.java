package com.onclass.bootcamp.exceptions;

import com.onclass.bootcamp.enums.ExceptionStatusCode;

public class TechnologyMicroserviceException extends BusinessException {
    public TechnologyMicroserviceException(String message) {
        super(ExceptionStatusCode.INTERNAL_SERVER_ERROR, message, 500);
    }
}

