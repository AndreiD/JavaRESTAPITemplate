package com.andrei.restapi.controller;

import com.andrei.restapi.model.PriceLabelType;
import com.andrei.restapi.model.Product;
import com.andrei.restapi.model.Products;
import com.andrei.restapi.service.ProductService;
import com.andrei.restapi.service.ProductServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static com.andrei.restapi.controller.ProductController.QUERY_PARAM_LABEL_TYPE;
import static com.andrei.restapi.controller.ProductController.URL_PRODUCTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link ProductController}.
 * NOTE: More comprehensive unit tests can be found at {@link com.andrei.restapi.service.ProductServiceTest}.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    static final String URL_WITH_LABEL_TYPE = URL_PRODUCTS + "?" + QUERY_PARAM_LABEL_TYPE + "={labelType}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Get products when label type is not specified should return products with default setting")
    void getProductsWhenLabelTypeIsNotSpecifiedShouldReturnProductsWithDefaultSetting() throws Exception {
        final Products products = Products.builder().products(List.of(Product.builder().productId("id").build())).build();
        when(productService.getProducts(Optional.empty())).thenReturn(products);

        final String responseText = this.mockMvc.perform(get(URL_PRODUCTS))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Products productsActual = objectMapper.readValue(responseText, Products.class);

        assertEquals(products, productsActual, "Products should match");
    }

    @Test
    @DisplayName("Get products when label type is specified should return products with label type")
    void getProductsWhenLabelTypeIsSpecifiedShouldReturnProductsWithLabelType() throws Exception {
        final Products products = Products.builder().products(List.of(Product.builder().productId("id").build())).build();

        when(productService.getProducts(Optional.of(PriceLabelType.SHOW_WAS_THEN_NOW))).thenReturn(products);

        final String responseText = this.mockMvc.perform(get(URL_WITH_LABEL_TYPE, PriceLabelType.SHOW_WAS_THEN_NOW.getValue()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Products productsActual = objectMapper.readValue(responseText, Products.class);

        assertEquals(products, productsActual, "Products should match");
    }

    @Test
    @DisplayName("Get products when label type is invalid should return bad request status")
    void getProductsWhenLabelTypeIsInvalidShouldReturnBadReqeustStatus() throws Exception {
        this.mockMvc.perform(get(URL_WITH_LABEL_TYPE, "invalidLabel"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get products when product service throws exception should return internal server error status")
    void getProductsWhenProductServiceThrowsExceptionShouldReturnInternalServerErrorStatus() throws Exception {
        when(productService.getProducts(Optional.empty())).thenThrow(ProductServiceException.class);

        this.mockMvc.perform(get(URL_PRODUCTS)).andExpect(status().isInternalServerError());
    }
}
