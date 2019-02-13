package com.andrei.restapi.service;

import com.andrei.restapi.model.external.ExternalProducts;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * External product API client. Fetches products from remote API.
 */
@FeignClient(name = "external-product-api-client", url = "${externalProductApiUrl}")
public interface ExternalProductApiClient {

    @RequestMapping(method = RequestMethod.GET)
    ExternalProducts getProducts();
}
