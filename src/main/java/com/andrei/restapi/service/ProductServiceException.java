package com.andrei.restapi.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is an error in the {@link ProductService}.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ProductServiceException extends RuntimeException {

    public ProductServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
