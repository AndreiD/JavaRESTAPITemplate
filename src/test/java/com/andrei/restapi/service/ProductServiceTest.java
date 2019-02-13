package com.andrei.restapi.service;

import com.andrei.restapi.model.BasicColor;
import com.andrei.restapi.model.PriceLabelType;
import com.andrei.restapi.model.Product;
import com.andrei.restapi.model.Products;
import com.andrei.restapi.model.external.ExternalColorSwatch;
import com.andrei.restapi.model.external.ExternalPrice;
import com.andrei.restapi.model.external.ExternalProduct;
import com.andrei.restapi.model.external.ExternalProducts;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProductService}.
 * NOTE: A unit test has been written for each behaviour in the test specification.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private ProductService productService;

    @Mock
    private ExternalProductApiClient externalProductApiClient;

    @BeforeEach
    void setUp() {
        productService = new ProductService(externalProductApiClient);
    }

    @Test
    @DisplayName("Get products when client throws exception should throw product service exception")
    void getProductsWhenClientThrowsExceptionShouldThrowProductServiceException() {
        when(externalProductApiClient.getProducts()).thenThrow(RuntimeException.class);

        assertThrows(ProductServiceException.class, () -> {
            productService.getProducts(Optional.empty());
        });
    }

    @Test
    @DisplayName("Get products should only return products with a price reduction")
    void getProductsShouldOnlyReturnProductsWithAPriceReduction() {
        final String withReductionId = "withReduction";
        final ExternalProduct withReduction = ExternalProduct.builder()
                .productId(withReductionId)
                .price(ExternalPrice.builder()
                        .now(new TextNode("20.00"))
                        .was("30.00")
                        .currency("GBP")
                        .build())
                .colorSwatches(Collections.emptyList())
                .build();

        final ExternalProduct withoutReduction = ExternalProduct.builder()
                .productId("withoutReduction")
                .price(ExternalPrice.builder()
                        .now(new TextNode("20.00"))
                        .build())
                .build();

        final ExternalProducts externalProducts = ExternalProducts.builder().products(List.of(withReduction, withoutReduction)).build();

        when(externalProductApiClient.getProducts()).thenReturn(externalProducts);

        final Products products = productService.getProducts(Optional.empty());

        assertEquals(1, products.getProducts().size(), "Size should match");
        assertEquals(withReductionId, products.getProducts().get(0).getProductId(), "Id should match");
    }

    @Test
    @DisplayName("Get products should sort products to show highest reduction first")
    void getProductsShouldSortProductsToShowHighestReductionFirst() {
        final String thirdId = "thirdId";
        final ExternalProduct third = ExternalProduct.builder()
                .productId(thirdId)
                .price(ExternalPrice.builder()
                        .now(new TextNode("1.00"))
                        .was("2.00")
                        .currency("GBP")
                        .build())
                .colorSwatches(Collections.emptyList())
                .build();

        final String firstId = "firstId";
        final ExternalProduct first = ExternalProduct.builder()
                .productId(firstId)
                .price(ExternalPrice.builder()
                        .now(new TextNode("1.00"))
                        .was("4.00")
                        .currency("GBP")
                        .build())
                .colorSwatches(Collections.emptyList())
                .build();

        final String secondId = "secondId";
        final ExternalProduct second = ExternalProduct.builder()
                .productId(secondId)
                .price(ExternalPrice.builder()
                        .now(new TextNode("1.00"))
                        .was("3.00")
                        .currency("GBP")
                        .build())
                .colorSwatches(Collections.emptyList())
                .build();


        final ExternalProducts externalProducts = ExternalProducts.builder().products(List.of(third, first, second)).build();

        when(externalProductApiClient.getProducts()).thenReturn(externalProducts);

        final Products products = productService.getProducts(Optional.empty());

        assertEquals(firstId, products.getProducts().get(0).getProductId(), "Id should match");
        assertEquals(secondId, products.getProducts().get(1).getProductId(), "Id should match");
        assertEquals(thirdId, products.getProducts().get(2).getProductId(), "Id should match");
    }

    @Test
    @DisplayName("Get products should return products with basic color mapped to RGB color")
    void getProductsShouldReturnProductsWithBasicColorMappedToRgbColor() {
        final ExternalProduct greenProduct = ExternalProduct.builder()
                .productId("id")
                .price(ExternalPrice.builder()
                        .now(new TextNode("1.00"))
                        .was("2.00")
                        .currency("GBP")
                        .build())
                .colorSwatches(List.of(ExternalColorSwatch.builder().basicColor("Green").build()))
                .build();

        final ExternalProducts externalProducts = ExternalProducts.builder().products(List.of(greenProduct)).build();

        when(externalProductApiClient.getProducts()).thenReturn(externalProducts);

        final Products products = productService.getProducts(Optional.empty());

        assertEquals(BasicColor.GREEN.getRgb(), products.getProducts().get(0).getColorSwatches().get(0).getRgbColor(), "RGB should match");
    }

    @Test
    @DisplayName("Get products when now price is less than 10 should return now price in decimal format")
    void getProductsWhenNowPriceIsLessThan10ShouldReturnNowPriceInDecimalFormat() {
        final ExternalProduct externalProduct = ExternalProduct.builder()
                .productId("id")
                .price(ExternalPrice.builder()
                        .now(new TextNode("1.00"))
                        .was("2.00")
                        .currency("GBP")
                        .build())
                .colorSwatches(Collections.emptyList())
                .build();

        final ExternalProducts externalProducts = ExternalProducts.builder().products(List.of(externalProduct)).build();

        when(externalProductApiClient.getProducts()).thenReturn(externalProducts);

        final Products products = productService.getProducts(Optional.empty());

        assertEquals("£1.00", products.getProducts().get(0).getNowPrice(), "Price should match");
    }

    @Test
    @DisplayName("Get products when now price is greater than or equal to 10 should return now price as integer")
    void getProductsWhenNowPriceIsGreaterThanOrEqualTo10ShouldReturnNowPriceAsInteger() {
        final ExternalProduct ten = ExternalProduct.builder()
                .productId("id")
                .price(ExternalPrice.builder()
                        .now(new TextNode("10.00"))
                        .was("11.00")
                        .currency("GBP")
                        .build())
                .colorSwatches(Collections.emptyList())
                .build();

        final ExternalProduct moreThanTen = ExternalProduct.builder()
                .productId("id2")
                .price(ExternalPrice.builder()
                        .now(new TextNode("11.00"))
                        .was("12.00")
                        .currency("GBP")
                        .build())
                .colorSwatches(Collections.emptyList())
                .build();

        final ExternalProducts externalProducts = ExternalProducts.builder().products(List.of(ten, moreThanTen)).build();

        when(externalProductApiClient.getProducts()).thenReturn(externalProducts);

        final Products products = productService.getProducts(Optional.empty());

        assertEquals("£10", products.getProducts().get(0).getNowPrice(), "Price should match");
        assertEquals("£11", products.getProducts().get(1).getNowPrice(), "Price should match");
    }

    @Test
    @DisplayName("Get products when label type is unspecified should return price label with was and now price")
    void getProductsWhenLabelTypeIsUnspecifiedShouldReturnPriceLabelWithWasAndNowPrice() {
        final ExternalProduct externalProduct = ExternalProduct.builder()
                .productId("id")
                .price(ExternalPrice.builder()
                        .now(new TextNode("9.00"))
                        .was("11.00")
                        .currency("GBP")
                        .build())
                .colorSwatches(Collections.emptyList())
                .build();

        final ExternalProducts externalProducts = ExternalProducts.builder().products(List.of(externalProduct)).build();

        when(externalProductApiClient.getProducts()).thenReturn(externalProducts);

        final Products products = productService.getProducts(Optional.empty());

        assertEquals("Was £11, now £9.00", products.getProducts().get(0).getPriceLabel(), "Price label should match");
    }

    @Test
    @DisplayName("Get products when label type is ShowWasNow should return price label with was and now price")
    void getProductsWhenLabelTypeIsShowWasNowShouldReturnPriceLabelWithWasAndNowPrice() {
        final ExternalProduct externalProduct = ExternalProduct.builder()
                .productId("id")
                .price(ExternalPrice.builder()
                        .now(new TextNode("9.00"))
                        .was("11.00")
                        .currency("GBP")
                        .build())
                .colorSwatches(Collections.emptyList())
                .build();

        final ExternalProducts externalProducts = ExternalProducts.builder().products(List.of(externalProduct)).build();

        when(externalProductApiClient.getProducts()).thenReturn(externalProducts);

        final Products products = productService.getProducts(Optional.of(PriceLabelType.SHOW_WAS_NOW));

        assertEquals("Was £11, now £9.00", products.getProducts().get(0).getPriceLabel(), "Price label should match");
    }

    @Test
    @DisplayName("Get products when label type is ShowWasThenNow and then2 exists should return price label with was, then2 and now price")
    void getProductsWhenLabelTypeIsShowWasThenNowAndThen2ExistsShouldReturnPriceLabelWithWasThen2AndNowPrice() {
        final ExternalProduct externalProduct = ExternalProduct.builder()
                .productId("id")
                .price(ExternalPrice.builder()
                        .now(new TextNode("9.00"))
                        .was("11.00")
                        .then2("10.00")
                        .currency("GBP")
                        .build())
                .colorSwatches(Collections.emptyList())
                .build();

        final ExternalProducts externalProducts = ExternalProducts.builder().products(List.of(externalProduct)).build();

        when(externalProductApiClient.getProducts()).thenReturn(externalProducts);

        final Products products = productService.getProducts(Optional.of(PriceLabelType.SHOW_WAS_THEN_NOW));

        assertEquals("Was £11, then £10, now £9.00", products.getProducts().get(0).getPriceLabel(), "Price label should match");
    }

    @Test
    @DisplayName("Get products when label type is ShowWasThenNow and then2 does not exist but then exists should return price label with was, then and now price")
    void getProductsWhenLabelTypeIsShowWasThenNowAndThen2DoesNotExistButThenExistsShouldReturnPriceLabelWithWasThenAndNowPrice() {
        final ExternalProduct externalProduct = ExternalProduct.builder()
                .productId("id")
                .price(ExternalPrice.builder()
                        .now(new TextNode("9.00"))
                        .was("11.00")
                        .then("10.00")
                        .currency("GBP")
                        .build())
                .colorSwatches(Collections.emptyList())
                .build();

        final ExternalProducts externalProducts = ExternalProducts.builder().products(List.of(externalProduct)).build();

        when(externalProductApiClient.getProducts()).thenReturn(externalProducts);

        final Products products = productService.getProducts(Optional.of(PriceLabelType.SHOW_WAS_THEN_NOW));

        assertEquals("Was £11, then £10, now £9.00", products.getProducts().get(0).getPriceLabel(), "Price label should match");
    }

    @Test
    @DisplayName("Get products when label type is ShowWasThenNow and both then and then2 do not exist should return price label with was and now price")
    void getProductsWhenLabelTypeIsShowWasThenNowAndBothThenAndThen2DoNotExistShouldReturnPriceLabelWithWasAndNowPrice() {
        final ExternalProduct externalProduct = ExternalProduct.builder()
                .productId("id")
                .price(ExternalPrice.builder()
                        .now(new TextNode("9.00"))
                        .was("11.00")
                        .currency("GBP")
                        .build())
                .colorSwatches(Collections.emptyList())
                .build();

        final ExternalProducts externalProducts = ExternalProducts.builder().products(List.of(externalProduct)).build();

        when(externalProductApiClient.getProducts()).thenReturn(externalProducts);

        final Products products = productService.getProducts(Optional.of(PriceLabelType.SHOW_WAS_THEN_NOW));

        assertEquals("Was £11, now £9.00", products.getProducts().get(0).getPriceLabel(), "Price label should match");
    }

    @Test
    @DisplayName("Get products when label type is ShowPercDiscount should return price label with percent off and now price")
    void getProductsWhenLabelTypeIsShowPercDiscountShouldReturnPriceLabelWithPercentOffAndNowPrice() {
        final ExternalProduct externalProduct = ExternalProduct.builder()
                .productId("id")
                .price(ExternalPrice.builder()
                        .now(new TextNode("5.00"))
                        .was("10.00")
                        .currency("GBP")
                        .build())
                .colorSwatches(Collections.emptyList())
                .build();

        final ExternalProducts externalProducts = ExternalProducts.builder().products(List.of(externalProduct)).build();

        when(externalProductApiClient.getProducts()).thenReturn(externalProducts);

        final Products products = productService.getProducts(Optional.of(PriceLabelType.SHOW_PERC_DISCOUNT));

        assertEquals("50% off - now £5.00", products.getProducts().get(0).getPriceLabel(), "Price label should match");
    }

    @Test
    @DisplayName("Get products should return products with fields mapped correctly from external products")
    void getProductsShouldReturnProductsWithFieldsMappedCorrectlyFromExternalProducts() {
        final String productId = "productId";
        final String productTitle = "productTitle";
        final String skuId = "123";
        final String color = "Black/White";

        final ExternalProduct externalProduct = ExternalProduct.builder()
                .productId(productId)
                .title(productTitle)
                .price(ExternalPrice.builder()
                        .now(new TextNode("5.00"))
                        .was("10.00")
                        .currency("GBP")
                        .build())
                .colorSwatches(List.of(ExternalColorSwatch.builder()
                        .color(color)
                        .basicColor("Black")
                        .skuId(skuId)
                        .build()))
                .build();

        final ExternalProducts externalProducts = ExternalProducts.builder().products(List.of(externalProduct)).build();

        when(externalProductApiClient.getProducts()).thenReturn(externalProducts);

        final Products products = productService.getProducts(Optional.empty());

        final Product actualProduct = products.getProducts().get(0);

        assertEquals(productId, actualProduct.getProductId(), "Product ID should match");
        assertEquals(productTitle, actualProduct.getTitle(), "Title should match");
        assertEquals(color, actualProduct.getColorSwatches().get(0).getColor(), "Color should match");
        assertEquals("000000", actualProduct.getColorSwatches().get(0).getRgbColor(), "RGB Color should match");
        assertEquals(skuId, actualProduct.getColorSwatches().get(0).getSkuid(), "Skuid should match");
        assertEquals("£5.00", actualProduct.getNowPrice(), "Now price should match");
        assertEquals("Was £10, now £5.00", actualProduct.getPriceLabel(), "Price label should match");
    }
}