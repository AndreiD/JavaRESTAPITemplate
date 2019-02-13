package com.andrei.restapi.model;

import lombok.Getter;

/**
 * Currency enum with symbol mapping.
 */
public enum Currency {
    GBP("£");

    @Getter
    private final String symbol;

    Currency(final String symbol) {
        this.symbol = symbol;
    }
}
