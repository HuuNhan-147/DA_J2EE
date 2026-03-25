package com.example.DACK_J2EE.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "http://localhost:5173")
public class MainController {

    @GetMapping
    public ResponseEntity<?> welcome() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to DA E-Commerce API!");
        response.put("version", "1.0.0");
        response.put("description", "Spring Boot REST API for E-Commerce Platform");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api")
    public ResponseEntity<?> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to DA E-Commerce API!");
        response.put("baseUrl", "http://localhost:8080/api");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("users", "/users - User authentication and profile management");
        endpoints.put("products", "/products - Product management");
        endpoints.put("categories", "/categories - Category management");
        endpoints.put("cart", "/cart - Shopping cart management");
        endpoints.put("orders", "/orders - Order management");
        endpoints.put("reviews", "/reviews - Product reviews");
        
        response.put("endpoints", endpoints);
        return ResponseEntity.ok(response);
    }
}
