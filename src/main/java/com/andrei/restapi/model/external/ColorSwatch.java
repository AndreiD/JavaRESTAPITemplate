package com.andrei.restapi.model.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * External color swatch model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalColorSwatch {

    private String color;

    private String basicColor;

    private String skuId;

}


