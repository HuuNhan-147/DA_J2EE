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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public CartDTO getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found!"));

        return convertToDTO(cart);
    }

    public CartDTO addToCart(Long userId, CartItemDTO itemDTO) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found!"));
                    return Cart.builder()
                            .user(user)
                            .items(List.of())
                            .build();
                });

        Product product = productRepository.findById(itemDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(itemDTO.getProductId()))
                .findFirst()
                .ifPresentOrElse(
                    item -> item.setQuantity(item.getQuantity() + itemDTO.getQuantity()),
                    () -> {
                        CartItem newItem = CartItem.builder()
                                .product(product)
                                .name(itemDTO.getName())
                                .quantity(itemDTO.getQuantity())
                                .price(itemDTO.getPrice())
                                .image(itemDTO.getImage())
                                .cart(cart)
                                .build();
                        cart.getItems().add(newItem);
                    }
                );

        Cart savedCart = cartRepository.save(cart);
        return convertToDTO(savedCart);
    }

    public CartDTO removeFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found!"));

        cart.setItems(cart.getItems().stream()
                .filter(item -> !item.getProduct().getId().equals(productId))
                .collect(Collectors.toList()));

        Cart savedCart = cartRepository.save(cart);
        return convertToDTO(savedCart);
    }

    public CartDTO clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found!"));

        cart.setItems(List.of());
        Cart savedCart = cartRepository.save(cart);
        return convertToDTO(savedCart);
    }

    public CartDTO updateCartItem(Long userId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found!"));

        cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));

        Cart savedCart = cartRepository.save(cart);
        return convertToDTO(savedCart);
    }

    private CartDTO convertToDTO(Cart cart) {
        List<CartItemDTO> items = cart.getItems().stream()
                .map(item -> CartItemDTO.builder()
                        .productId(item.getProduct().getId())
                        .name(item.getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .image(item.getImage())
                        .build())
                .collect(Collectors.toList());

        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .cartItems(items)
                .build();
    }
}
