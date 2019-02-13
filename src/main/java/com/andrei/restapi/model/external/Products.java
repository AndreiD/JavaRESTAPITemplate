package com.andrei.restapi.model.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * External products model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalProducts {

    private List<ExternalProduct> products;
}
