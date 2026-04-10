package com.example.DACK_J2EE.service;

import com.example.DACK_J2EE.dto.CartDTO;
import com.example.DACK_J2EE.dto.CartItemDTO;
import com.example.DACK_J2EE.entity.Cart;
import com.example.DACK_J2EE.entity.CartItem;
import com.example.DACK_J2EE.entity.Product;
import com.example.DACK_J2EE.entity.User;
import com.example.DACK_J2EE.repository.CartRepository;
import com.example.DACK_J2EE.repository.ProductRepository;
import com.example.DACK_J2EE.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // ================= GET CART =================
    public CartDTO getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        return convertToDTO(cart);
    }

    // ================= ADD =================
    public CartDTO addToCart(Long userId, CartItemDTO itemDTO) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        Product product = productRepository.findById(itemDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + itemDTO.getQuantity()),
                        () -> {
                            CartItem newItem = CartItem.builder()
                                    .product(product)
                                    .name(product.getName())
                                    .quantity(itemDTO.getQuantity())
                                    .price(product.getPrice())
                                    .image(product.getImage())
                                    .cart(cart)
                                    .build();
                            cart.getItems().add(newItem);
                        }
                );

        return convertToDTO(cartRepository.save(cart));
    }

    // ================= REMOVE =================
    public CartDTO removeFromCart(Long userId, Long productId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems() != null) {
            cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        }

        return convertToDTO(cartRepository.save(cart));
    }

    // ================= UPDATE =================
    public CartDTO updateCartItem(Long userId, Long productId, Integer quantity) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems() != null) {
            cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst()
                    .ifPresent(item -> item.setQuantity(quantity));
        }

        return convertToDTO(cartRepository.save(cart));
    }

    // ================= COUNT =================
    public int getCartItemCount(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        if (cart.getItems() == null) {
            return 0;
        }

        return cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    // ================= CREATE NEW CART =================
    private Cart createNewCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = Cart.builder()
                .user(user)
                .items(new ArrayList<>()) // ✅ FIX quan trọng
                .build();

        return cartRepository.save(cart);
    }

    private CartDTO convertToDTO(Cart cart) {
        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }
        
        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .cartItems(
                        cart.getItems().stream()
                                .map(item -> CartItemDTO.builder()
                                        .productId(item.getProduct().getId())
                                        .name(item.getName())
                                        .quantity(item.getQuantity())
                                        .price(item.getPrice())
                                        .image(item.getImage())
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }
}