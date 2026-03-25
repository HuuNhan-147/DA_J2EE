package com.example.DACK_J2EE.service;

import com.example.DACK_J2EE.entity.Order;
import com.example.DACK_J2EE.entity.User;
import com.example.DACK_J2EE.repository.OrderRepository;
import com.example.DACK_J2EE.repository.ProductRepository;
import com.example.DACK_J2EE.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> getDashboardStats() {
        long totalProducts = productRepository.count();
        long totalUsers = userRepository.count();
        long totalOrders = orderRepository.count();
        Double totalRevenue = orderRepository.getTotalRevenue();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", totalProducts);
        stats.put("totalUsers", totalUsers);
        stats.put("totalOrders", totalOrders);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0);

        return stats;
    }

    public List<Map<String, Object>> getMonthlyRevenue() {
        List<Object[]> rawData = orderRepository.getMonthlyRevenue();
        return rawData.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("_id", row[0]);
            map.put("totalRevenue", row[1]);
            return map;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getTopSellingProducts() {
        List<Object[]> rawData = orderRepository.getTopSellingProducts();
        return rawData.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("productId", row[0]);
            map.put("name", row[1]);
            map.put("image", row[2]);
            map.put("totalSold", row[3]);
            return map;
        }).collect(Collectors.toList());
    }

    public List<Order> getLatestOrders() {
        return orderRepository.findTop5ByOrderByCreatedAtDesc();
    }

    public List<User> getLatestUsers() {
        return userRepository.findTop5ByOrderByCreatedAtDesc();
    }

    public Map<String, Object> getOrderStatusStats() {
        List<Object[]> rawData = orderRepository.getOrderStatusStats();
        Map<String, Object> map = new HashMap<>();
        if (!rawData.isEmpty() && rawData.get(0) != null) {
            Object[] row = rawData.get(0);
            map.put("paid", row[0]);
            map.put("delivered", row[1]);
            map.put("unpaid", row[2]);
            map.put("undelivered", row[3]);
            map.put("total", row[4]);
        }
        return map;
    }
}
