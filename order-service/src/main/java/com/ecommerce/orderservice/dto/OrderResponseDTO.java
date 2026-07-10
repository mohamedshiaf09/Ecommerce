package com.ecommerce.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// This is what is returned to the client after an order is placed / fetched
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String customerName;
    private Integer quantity;
    private Double totalPrice;
}
