package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.dto.OrderRequestDTO;
import com.ecommerce.orderservice.dto.OrderResponseDTO;
import com.ecommerce.orderservice.dto.ProductDTO;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.exception.InsufficientStockException;
import com.ecommerce.orderservice.exception.OrderNotFoundException;
import com.ecommerce.orderservice.exception.ProductNotFoundException;
import com.ecommerce.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    public OrderResponseDTO placeOrder(OrderRequestDTO request) {

        // Step 1: Call Product Service to fetch product details
        ProductDTO product = productClient.getProductById(request.getProductId());

        // Step 2: Validate product exists
        if (product == null) {
            throw new ProductNotFoundException(request.getProductId());
        }

        // Step 3: Validate stock availability
        if (product.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    request.getProductId(), product.getQuantity(), request.getQuantity());
        }

        // Step 4: Calculate total price
        double totalPrice = product.getPrice() * request.getQuantity();

        // Step 5: Save the order
        Order order = new Order();
        order.setProductId(request.getProductId());
        order.setCustomerName(request.getCustomerName());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(totalPrice);
        Order savedOrder = orderRepository.save(order);

        // Step 6: Ask Product Service to reduce stock
        productClient.reduceStock(request.getProductId(), request.getQuantity());

        return toResponseDTO(savedOrder, product.getName());
    }

    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(order -> {
                    ProductDTO product = productClient.getProductById(order.getProductId());
                    String productName = (product != null) ? product.getName() : "Unknown / Deleted Product";
                    return toResponseDTO(order, productName);
                })
                .collect(Collectors.toList());
    }

    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        ProductDTO product = productClient.getProductById(order.getProductId());
        String productName = (product != null) ? product.getName() : "Unknown / Deleted Product";
        return toResponseDTO(order, productName);
    }

    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        orderRepository.delete(order);
        // Note: stock is intentionally not restored automatically here.
        // Add productClient.increaseStock(...) on the Product Service side if that behavior is desired.
    }

    private OrderResponseDTO toResponseDTO(Order order, String productName) {
        return new OrderResponseDTO(
                order.getId(),
                order.getProductId(),
                productName,
                order.getCustomerName(),
                order.getQuantity(),
                order.getTotalPrice()
        );
    }
}
