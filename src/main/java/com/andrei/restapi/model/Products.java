package com.andrei.restapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Products model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Products {

    private List<Product> products;
}
