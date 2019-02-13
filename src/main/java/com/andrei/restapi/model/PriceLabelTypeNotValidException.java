package com.andrei.restapi.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when price label type is not valid.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PriceLabelTypeNotValidException extends RuntimeException {

    public PriceLabelTypeNotValidException(final String priceLabelType) {
        super("Price label type " + priceLabelType + "is not valid");
    }
}
