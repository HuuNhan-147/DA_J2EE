package com.example.DACK_J2EE.service;

import com.example.DACK_J2EE.dto.OrderCreateDTO;
import com.example.DACK_J2EE.entity.*;
import com.example.DACK_J2EE.repository.OrderRepository;
import com.example.DACK_J2EE.repository.ProductRepository;
import com.example.DACK_J2EE.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private String generateOrderCode() {
        return "DH" + System.currentTimeMillis() % 1000000 + "-" + (int)(100 + Math.random() * 900);
    }

    public Order createOrder(Long userId, OrderCreateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        Order order = Order.builder()
                .orderCode(generateOrderCode())
                .user(user)
                .paymentMethod(dto.getPaymentMethod())
                .itemsPrice(dto.getItemsPrice())
                .shippingPrice(dto.getShippingPrice())
                .taxPrice(dto.getTaxPrice())
                .totalPrice(dto.getTotalPrice())
                .isPaid(false)
                .paymentStatus(Order.PaymentStatus.pending)
                .isDelivered(false)
                .build();

        if (dto.getShippingAddress() != null) {
            OrderShippingAddress address = OrderShippingAddress.builder()
                    .fullname(dto.getShippingAddress().getFullname())
                    .phone(dto.getShippingAddress().getPhone())
                    .address(dto.getShippingAddress().getAddress())
                    .city(dto.getShippingAddress().getCity())
                    .order(order)
                    .build();
            order.setShippingAddress(address);
        }

        if (dto.getOrderItems() != null) {
            List<OrderItem> items = dto.getOrderItems().stream()
                    .map(itemDto -> {
                        Product product = productRepository.findById(itemDto.getProductId())
                                .orElseThrow(() -> new RuntimeException("Product not found!"));
                        return OrderItem.builder()
                                .name(itemDto.getName())
                                .quantity(itemDto.getQuantity())
                                .image(itemDto.getImage())
                                .price(itemDto.getPrice())
                                .product(product)
                                .order(order)
                                .build();
                    })
                    .collect(Collectors.toList());
            order.setItems(items);
        }

        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found!"));
    }

    public Order getOrderByCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Order not found!"));
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order updateOrderPaymentStatus(Long orderId, String paymentStatus) {
        Order order = getOrderById(orderId);
        
        try {
            Order.PaymentStatus status = Order.PaymentStatus.valueOf(paymentStatus);
            order.setPaymentStatus(status);

            if (status == Order.PaymentStatus.paid) {
                order.setIsPaid(true);
                order.setPaidAt(LocalDateTime.now());
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid payment status: " + paymentStatus);
        }

        return orderRepository.save(order);
    }

    public Order updateOrderDeliveryStatus(Long orderId, Boolean isDelivered) {
        Order order = getOrderById(orderId);
        order.setIsDelivered(isDelivered);

        if (isDelivered) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        return orderRepository.save(order);
    }

    public Order updateVnpayTransactionId(Long orderId, String transactionId) {
        Order order = getOrderById(orderId);
        order.setVnpayTransactionId(transactionId);
        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order processVnpayReturn(Long orderId, String transactionId, String responseCode) {
        Order order = getOrderById(orderId);
        if ("00".equals(responseCode)) {
            order.setIsPaid(true);
            order.setPaidAt(LocalDateTime.now());
            order.setPaymentStatus(Order.PaymentStatus.paid);
            order.setVnpayTransactionId(transactionId);
        } else {
            order.setPaymentStatus(Order.PaymentStatus.failed);
        }
        return orderRepository.save(order);
    }

    public void deleteOrder(Long orderId) {
        Order order = getOrderById(orderId);
        orderRepository.delete(order);
    }
}
