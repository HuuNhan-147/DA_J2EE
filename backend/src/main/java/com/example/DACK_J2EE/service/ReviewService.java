package com.example.DACK_J2EE.service;

import com.example.DACK_J2EE.dto.ReviewCreateDTO;
import com.example.DACK_J2EE.entity.Product;
import com.example.DACK_J2EE.entity.Review;
import com.example.DACK_J2EE.entity.User;
import com.example.DACK_J2EE.repository.ProductRepository;
import com.example.DACK_J2EE.repository.ReviewRepository;
import com.example.DACK_J2EE.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public Review createReview(Long userId, ReviewCreateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        Review review = Review.builder()
                .user(user)
                .product(product)
                .name(dto.getName())
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Update product rating
        updateProductRating(product.getId());

        return savedReview;
    }

    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found!"));
    }

    public void deleteReview(Long id) {
        Review review = getReviewById(id);
        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);
        
        // Update product rating after deletion
        updateProductRating(productId);
    }

    private void updateProductRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        List<Review> reviews = reviewRepository.findByProductId(productId);

        if (reviews.isEmpty()) {
            product.setRating(0.0);
            product.setNumReviews(0);
        } else {
            double averageRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            product.setRating(averageRating);
            product.setNumReviews(reviews.size());
        }

        productRepository.save(product);
    }
}
