package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ProductClient {

    private final RestTemplate restTemplate;

    @Value("${product.service.url}")
    private String productServiceUrl;

    // Calls GET http://localhost:8080/products/{id} on Product Service
    public ProductDTO getProductById(Long productId) {
        try {
            return restTemplate.getForObject(productServiceUrl + "/" + productId, ProductDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            return null; // Product does not exist
        } catch (RestClientException ex) {
            throw new RuntimeException("Product Service is unavailable: " + ex.getMessage());
        }
    }

    // Calls PUT http://localhost:8080/products/{id}/reduce-stock?quantity=X on Product Service
    public void reduceStock(Long productId, Integer quantity) {
        String url = productServiceUrl + "/" + productId + "/reduce-stock?quantity=" + quantity;
        restTemplate.exchange(url, HttpMethod.PUT, null, ProductDTO.class);
    }
}
