package com.example.DACK_J2EE.controller;

import com.example.DACK_J2EE.dto.CartDTO;
import com.example.DACK_J2EE.dto.CartItemDTO;
import com.example.DACK_J2EE.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173")
public class CartController {

    @Autowired
    private CartService cartService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }

    @GetMapping
    public ResponseEntity<?> getCart() {
        try {
            Long userId = getCurrentUserId();
            CartDTO cart = cartService.getCart(userId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartItemDTO itemDTO) {
        try {
            Long userId = getCurrentUserId();
            CartDTO cart = cartService.addToCart(userId, itemDTO);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId) {
        try {
            Long userId = getCurrentUserId();
            CartDTO cart = cartService.removeFromCart(userId, productId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart() {
        try {
            Long userId = getCurrentUserId();
            CartDTO cart = cartService.clearCart(userId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<?> updateCartItem(@PathVariable Long productId, 
            @RequestParam Integer quantity) {
        try {
            Long userId = getCurrentUserId();
            CartDTO cart = cartService.updateCartItem(userId, productId, quantity);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
