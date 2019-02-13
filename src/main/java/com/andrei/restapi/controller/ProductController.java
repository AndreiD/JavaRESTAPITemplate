package com.andrei.restapi.controller;

import com.andrei.restapi.model.PriceLabelType;
import com.andrei.restapi.model.Products;
import com.andrei.restapi.service.ProductService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Product controller.
 */
@RestController()
public class ProductController {

    static final String URL_PRODUCTS = "/products";

    static final String QUERY_PARAM_LABEL_TYPE = "labelType";

    private final ProductService productService;

    public ProductController(final ProductService productService) {
        this.productService = productService;
    }

    @RequestMapping(method = RequestMethod.GET, path = URL_PRODUCTS)
    public Products getProducts(@RequestParam(name = QUERY_PARAM_LABEL_TYPE, required = false) final Optional<String> priceLabelTypeOp) {
        if (!priceLabelTypeOp.isPresent()) {
            return productService.getProducts(Optional.empty());
        }

        final PriceLabelType priceLabelType = PriceLabelType.parse(priceLabelTypeOp.get());
        return productService.getProducts(Optional.of(priceLabelType));
    }
}
