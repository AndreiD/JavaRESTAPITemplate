package com.andrei.restapi.model;

import lombok.Getter;

import java.util.Arrays;

/**
 * Basic color enum with RGB mapping.
 */
public enum BasicColor {
    RED("FF0000"),
    PINK("FFC0CB"),
    ORANGE("FFA500"),
    YELLOW("FFFF00"),
    PURPLE("800080"),
    GREEN("008000"),
    BLUE("0000FF"),
    BROWN("A52A2A"),
    WHITE("FFFFFF"),
    GREY("808080"),
    BLACK("000000"),
    // Multi is one of the basic color values in the API
    MULTI("");

    @Getter
    private final String rgb;

    BasicColor(final String rgb) {
        this.rgb = rgb;
    }

    public static BasicColor parse(final String color) {
        return Arrays.stream(values())
                .filter(basicColor -> basicColor.toString().equals(color.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Color " + color + " is not valid"));
    }
}
