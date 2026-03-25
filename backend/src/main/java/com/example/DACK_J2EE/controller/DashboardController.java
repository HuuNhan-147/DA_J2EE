package com.example.DACK_J2EE.controller;

import com.example.DACK_J2EE.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            return ResponseEntity.ok(dashboardService.getDashboardStats());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error: " + e.getMessage());
        }
    }

    @GetMapping("/monthly-revenue")
    public ResponseEntity<?> getMonthlyRevenue() {
        try {
            return ResponseEntity.ok(dashboardService.getMonthlyRevenue());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error: " + e.getMessage());
        }
    }

    @GetMapping("/top-products")
    public ResponseEntity<?> getTopSellingProducts() {
        try {
            return ResponseEntity.ok(dashboardService.getTopSellingProducts());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error: " + e.getMessage());
        }
    }

    @GetMapping("/latest-orders")
    public ResponseEntity<?> getLatestOrders() {
        try {
            return ResponseEntity.ok(dashboardService.getLatestOrders());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error: " + e.getMessage());
        }
    }

    @GetMapping("/latest-users")
    public ResponseEntity<?> getLatestUsers() {
        try {
            return ResponseEntity.ok(dashboardService.getLatestUsers());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error: " + e.getMessage());
        }
    }

    @GetMapping("/order-status")
    public ResponseEntity<?> getOrderStatusStats() {
        try {
            return ResponseEntity.ok(dashboardService.getOrderStatusStats());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error: " + e.getMessage());
        }
    }
}
