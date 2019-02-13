package com.andrei.restapi.model;

import lombok.Getter;

import java.util.Arrays;

/**
 * Price label type enum.
 */
public enum PriceLabelType {
    SHOW_WAS_NOW("ShowWasNow"),
    SHOW_WAS_THEN_NOW("ShowWasThenNow"),
    SHOW_PERC_DISCOUNT("ShowPercDiscount");

    @Getter
    private final String value;

    PriceLabelType(final String value) {
        this.value = value;
    }

    public static PriceLabelType parse(final String value) {
        return Arrays.stream(values())
                .filter(priceLabelType -> priceLabelType.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new PriceLabelTypeNotValidException(value));
    }
}
