package com.andrei.restapi.model.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * External product model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalProduct {

    private String productId;

    private String title;

    private ExternalPrice price;

    private List<ExternalColorSwatch> colorSwatches;
}

