package com.example.DACK_J2EE.controller;

import com.example.DACK_J2EE.dto.CartDTO;
import com.example.DACK_J2EE.dto.CartItemDTO;
import com.example.DACK_J2EE.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173")
public class CartController {

    @Autowired
    private CartService cartService;

    // ===== LẤY USER ID TỪ JWT =====
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getName());
    }

    // ================= GET CART =================
    @GetMapping
    public ResponseEntity<?> getCart() {
        try {
            Long userId = getCurrentUserId();
            CartDTO cart = cartService.getCart(userId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    // ================= ADD =================
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartItemDTO itemDTO) {
        try {
            Long userId = getCurrentUserId();
            CartDTO cart = cartService.addToCart(userId, itemDTO);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    // ================= REMOVE =================
    @DeleteMapping("/{productId}") // ✅ khớp frontend
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId) {
        try {
            Long userId = getCurrentUserId();
            CartDTO cart = cartService.removeFromCart(userId, productId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    // ================= UPDATE =================
    @PutMapping("/update") // ✅ nhận body giống frontend
    public ResponseEntity<?> updateCartItem(@RequestBody CartItemDTO itemDTO) {
        try {
            Long userId = getCurrentUserId();
            CartDTO cart = cartService.updateCartItem(
                    userId,
                    itemDTO.getProductId(),
                    itemDTO.getQuantity()
            );
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    // ================= COUNT =================
    @GetMapping("/count")
    public ResponseEntity<?> getCartCount() {
        try {
            Long userId = getCurrentUserId();
            int count = cartService.getCartItemCount(userId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }
}