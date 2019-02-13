package com.andrei.restapi.controller;

import com.andrei.restapi.model.PriceLabelType;
import com.andrei.restapi.model.Product;
import com.andrei.restapi.model.Products;
import com.andrei.restapi.model.external.ExternalPrice;
import com.andrei.restapi.model.external.ExternalProduct;
import com.andrei.restapi.service.ExternalProductApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.andrei.restapi.controller.ProductController.QUERY_PARAM_LABEL_TYPE;
import static com.andrei.restapi.controller.ProductController.URL_PRODUCTS;
import static com.andrei.restapi.model.external.ExternalPrice.NOW_PRICE_TO_FIELD_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link ProductController}.
 * NOTE: Since we do not control the external API, minimal assumptions have been about the data to make the tests
 * robust. During testing the external API went down so integration tests started failing.
 * <p>
 * More comprehensive unit tests can be found at {@link com.andrei.restapi.service.ProductServiceTest}
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIT {

    static final String URL_WITH_LABEL_TYPE = URL_PRODUCTS + "?" + QUERY_PARAM_LABEL_TYPE + "={labelType}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExternalProductApiClient externalProductApiClient;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Get products should return products with ok status")
    void getProductsShouldReturnProductsWithOKStatus() throws Exception {
        final String responseText = this.mockMvc.perform(get(URL_PRODUCTS))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Products products = objectMapper.readValue(responseText, Products.class);

        assertNotNull(products, "Products should not be null");
    }

    @Test
    @DisplayName("Get products should only return reduced products")
    void getProductsShouldOnlyReturnReducedProducts() throws Exception {
        final String responseText = this.mockMvc.perform(get(URL_PRODUCTS))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Products products = objectMapper.readValue(responseText, Products.class);

        final Set<String> reducedIds = products.getProducts().stream().map(Product::getProductId).collect(Collectors.toSet());
        final Set<String> expectedReducedIds = getReducedIds();

        assertEquals(expectedReducedIds, reducedIds, "Ids should match");
    }

    @Test
    @DisplayName("Get products should return products with highest reduction first")
    void getProductsShouldReturnProductsWithHighestReductionFirst() throws Exception {
        final String responseText = this.mockMvc.perform(get(URL_PRODUCTS))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Products products = objectMapper.readValue(responseText, Products.class);

        final List<String> ids = products.getProducts().stream().map(Product::getProductId).collect(Collectors.toList());
        final List<String> idsWithHighestReductionFirst = getIdsWithHighestReductionFirst();

        assertEquals(idsWithHighestReductionFirst, ids, "Ids should match");
    }

    @Test
    @DisplayName("Get products when label type is invalid should return bad request status")
    void getProductsWhenLabelTypeIsInvalidShouldReturnBadRequestStatus() throws Exception {
        this.mockMvc.perform(get(URL_WITH_LABEL_TYPE, "invalidLabelType"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get products when label type is not specified should return products with was and now price")
    void getProductsWhenLabelTypeIsNotSpecifiedShouldReturnProductsWithWasAndNowPrice() throws Exception {
        final String responseText = this.mockMvc.perform(get(URL_PRODUCTS))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Products products = objectMapper.readValue(responseText, Products.class);

        products.getProducts().stream().forEach(product -> {
            assertTrue(product.getPriceLabel().matches("Was .*, now .*"));
        });
    }

    @Test
    @DisplayName("Get products when label type is ShowWasNow should return products with was and now price")
    void getProductsWhenLabelTypeIsShowWasNowShouldReturnProductsWithWasAndNowPrice() throws Exception {
        final String responseText = this.mockMvc.perform(get(URL_WITH_LABEL_TYPE, PriceLabelType.SHOW_WAS_NOW.getValue()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Products products = objectMapper.readValue(responseText, Products.class);

        products.getProducts().stream().forEach(product -> {
            assertTrue(product.getPriceLabel().matches("Was .*, now .*"), "Pattern should match");
        });
    }

    @Test
    @DisplayName("Get products when label type is ShowWasThenNow should return products with was, then and now price")
    void getProductsWhenLabelTypeIsShowWasThenNowShouldReturnProductsWithWas() throws Exception {
        final String responseText = this.mockMvc.perform(get(URL_WITH_LABEL_TYPE, PriceLabelType.SHOW_WAS_THEN_NOW.getValue()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Products products = objectMapper.readValue(responseText, Products.class);

        products.getProducts().stream().forEach(product -> {
            assertTrue(product.getPriceLabel().matches("Was .*,( then .*)? now .*"), "Pattern should match");
        });
    }

    @Test
    @DisplayName("Get products when label type is ShowPercDiscount should return products with percent discount")
    void getProductsWhenLabelTypeIsShowPercDiscountShouldReturnProductsWithPercentDiscount() throws Exception {
        final String responseText = this.mockMvc.perform(get(URL_WITH_LABEL_TYPE, PriceLabelType.SHOW_PERC_DISCOUNT.getValue()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final Products products = objectMapper.readValue(responseText, Products.class);

        products.getProducts().stream().forEach(product -> {
            assertTrue(product.getPriceLabel().matches(".* off - now .*"), "Pattern should match");
        });
    }

    private Set<String> getReducedIds() {
        return externalProductApiClient.getProducts().getProducts().stream()
                .filter(externalProduct -> StringUtils.isNotBlank(externalProduct.getPrice().getWas()))
                .map(ExternalProduct::getProductId)
                .collect(Collectors.toSet());
    }

    private List<String> getIdsWithHighestReductionFirst() {
        return externalProductApiClient.getProducts().getProducts().stream()
                .filter(externalProduct -> StringUtils.isNotBlank(externalProduct.getPrice().getWas()))
                .sorted(this::comparePriceReduction)
                .map(ExternalProduct::getProductId)
                .collect(Collectors.toList());
    }

    private int extractPriceReduction(final ExternalPrice price) {
        final double nowPrice = extractNowPrice(price);
        final double wasPrice = Double.parseDouble(price.getWas());
        return (int) Math.floor(wasPrice - nowPrice);
    }

    private double extractNowPrice(final ExternalPrice externalPrice) {
        String nowPriceText = "";

        final JsonNode nowNode = externalPrice.getNow();
        if (nowNode.isTextual()) {
            nowPriceText = externalPrice.getNow().asText();
        }
        if (nowNode.isObject()) {
            nowPriceText = nowNode.get(NOW_PRICE_TO_FIELD_NAME).asText();
        }

        return Double.parseDouble(nowPriceText);
    }

    private int comparePriceReduction(final ExternalProduct externalProduct1, final ExternalProduct externalProduct2) {
        return (int) Math.floor(extractPriceReduction(externalProduct2.getPrice()) - extractPriceReduction(externalProduct1.getPrice()));
    }
}
