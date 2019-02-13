package com.andrei.restapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Color swatch model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColorSwatch {

    private String color;

    private String rgbColor;

    private String skuid;
}
