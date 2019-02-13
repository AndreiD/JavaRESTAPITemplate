package com.andrei.restapi.service;

import com.andrei.restapi.model.*;
import com.andrei.restapi.model.external.ExternalColorSwatch;
import com.andrei.restapi.model.external.ExternalPrice;
import com.andrei.restapi.model.external.ExternalProduct;
import com.andrei.restapi.model.external.ExternalProducts;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.andrei.restapi.model.external.ExternalPrice.NOW_PRICE_TO_FIELD_NAME;

/**
 * Product service.
 */
@Service
public class ProductService {

    private static final int DECIMAL_FORMAT_THRESHOLD = 10;

    private static final String WAS_NOW_FORMAT = "Was %s, now %s";

    private static final String WAS_THEN_NOW_FORMAT = "Was %s, then %s, now %s";

    private static final String PERCENT_DISCOUNT_FORMAT = "%s off - now %s";

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(".00");

    private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat();

    private final ExternalProductApiClient externalProductApiClient;


    public ProductService(final ExternalProductApiClient externalProductApiClient) {
        this.externalProductApiClient = externalProductApiClient;
    }

    /**
     * Get products that have a price reduction and show highest product with highest reduction first.
     *
     * @param priceLabel the price label type
     * @return {@link Products}
     * @throws {@link ProductServiceException} if unable to retrieve products from remote API
     */
    public Products getProducts(final Optional<PriceLabelType> priceLabel) {
        final ExternalProducts externalProducts;
        try {
            externalProducts = externalProductApiClient.getProducts();
        } catch (final Exception e) {
            throw new ProductServiceException("Unable to retrieve products from API", e);
        }

        final List<Product> products = externalProducts.getProducts().stream()
                .filter(externalProduct -> getPriceReduction(externalProduct.getPrice()) != 0)
                .sorted(this::comparePriceReduction)
                .map(externalProduct -> mapExternalProductToProduct(externalProduct, priceLabel))
                .collect(Collectors.toList());

        return Products.builder().products(products).build();
    }

    private int comparePriceReduction(final ExternalProduct externalProduct1, final ExternalProduct externalProduct2) {
        return (int) Math.floor(getPriceReduction(externalProduct2.getPrice()) - getPriceReduction(externalProduct1.getPrice()));
    }

    private double getPriceReduction(final ExternalPrice externalPrice) {
        if (!extractWasPrice(externalPrice).isPresent()) {
            return 0;
        }

        final double nowPrice = extractNowPrice(externalPrice);
        final double wasPrice = extractWasPrice(externalPrice).get();
        return wasPrice - nowPrice;
    }

    private Product mapExternalProductToProduct(final ExternalProduct externalProduct,
                                                final Optional<PriceLabelType> priceLabelType) {
        return Product.builder()
                .productId(externalProduct.getProductId())
                .title(externalProduct.getTitle())
                .colorSwatches(extractColorSwatches(externalProduct.getColorSwatches()))
                .nowPrice(extractFormattedNowPrice(externalProduct.getPrice()))
                .priceLabel(extractPriceLabel(priceLabelType, externalProduct.getPrice()))
                .build();
    }

    private List<ColorSwatch> extractColorSwatches(final List<ExternalColorSwatch> externalColorSwatches) {
        return externalColorSwatches.stream()
                .map(this::mapExternalColorSwatchToColorSwatch)
                .collect(Collectors.toList());
    }

    private ColorSwatch mapExternalColorSwatchToColorSwatch(final ExternalColorSwatch externalColorSwatch) {
        return ColorSwatch.builder()
                .color(externalColorSwatch.getColor())
                .rgbColor(BasicColor.parse(externalColorSwatch.getBasicColor()).getRgb())
                .skuid(externalColorSwatch.getSkuId())
                .build();
    }

    private String extractPriceLabel(final Optional<PriceLabelType> priceLabelType, final ExternalPrice externalPrice) {
        final PriceLabelType labelType = priceLabelType.orElse(PriceLabelType.SHOW_WAS_NOW);

        final String nowPrice = extractFormattedNowPrice(externalPrice);

        switch (labelType) {
            case SHOW_WAS_THEN_NOW:
                if (!(StringUtils.isBlank(externalPrice.getThen2()) && StringUtils.isBlank(externalPrice.getThen()))) {
                    return String.format(WAS_THEN_NOW_FORMAT, extractFormattedWasPrice(externalPrice),
                            extractFormattedThenPrice(externalPrice), nowPrice);

                }
            case SHOW_WAS_NOW:
                return String.format(WAS_NOW_FORMAT, extractFormattedWasPrice(externalPrice), nowPrice);

            case SHOW_PERC_DISCOUNT:
                return String.format(PERCENT_DISCOUNT_FORMAT, extractPercentageDiscount(externalPrice), nowPrice);

            default:
                throw new IllegalStateException("Label type " + labelType + " not recognised");
        }
    }

    private String extractFormattedNowPrice(final ExternalPrice externalPrice) {
        return formatPrice(extractNowPrice(externalPrice), Currency.valueOf(externalPrice.getCurrency()));
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

    private String extractFormattedWasPrice(final ExternalPrice externalPrice) {
        final Optional<Double> wasPriceOp = extractWasPrice(externalPrice);
        if (wasPriceOp.isPresent()) {
            return formatPrice(wasPriceOp.get(), Currency.valueOf(externalPrice.getCurrency()));
        }

        return "";
    }

    private Optional<Double> extractWasPrice(final ExternalPrice externalPrice) {
        final String wasPrice = externalPrice.getWas();
        if (StringUtils.isBlank(wasPrice)) {
            return Optional.empty();
        }
        return Optional.of(Double.parseDouble(wasPrice));
    }

    private String extractFormattedThenPrice(final ExternalPrice externalPrice) {
        if (!StringUtils.isBlank(externalPrice.getThen2())) {
            return formatPrice(Double.parseDouble(externalPrice.getThen2()), Currency.valueOf(externalPrice.getCurrency()));
        } else if (!StringUtils.isBlank(externalPrice.getThen())) {
            return formatPrice(Double.parseDouble(externalPrice.getThen()), Currency.valueOf(externalPrice.getCurrency()));
        }

        return "";
    }

    private String formatPrice(final double price, final Currency currency) {
        if (price >= DECIMAL_FORMAT_THRESHOLD && Math.floor(price) == price) {
            return currency.getSymbol() + INTEGER_FORMAT.format(price);
        }
        return currency.getSymbol() + DECIMAL_FORMAT.format(price);
    }

    private String extractPercentageDiscount(final ExternalPrice externalPrice) {
        if (!extractWasPrice(externalPrice).isPresent()) {
            return "";
        }

        final double nowPrice = extractNowPrice(externalPrice);
        final Double wasPrice = extractWasPrice(externalPrice).get();
        final int percentDiscount = (int) (((wasPrice - nowPrice) / wasPrice) * 100);
        return percentDiscount + "%";
    }
}
