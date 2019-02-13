package com.andrei.restapi.model.external;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * External price model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalPrice {

    public static final String NOW_PRICE_TO_FIELD_NAME = "to";

    private String was;

    private String then;

    private String then2;

    private String currency;

    /**
     * The value of this field can be a String or an object. Using JsonNode to allow custom parsing.
     */
    private JsonNode now;
}
