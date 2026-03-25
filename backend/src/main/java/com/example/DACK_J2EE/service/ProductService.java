package com.example.DACK_J2EE.service;

import com.example.DACK_J2EE.dto.ProductCreateDTO;
import com.example.DACK_J2EE.entity.Category;
import com.example.DACK_J2EE.entity.Product;
import com.example.DACK_J2EE.entity.ProductSpecification;
import com.example.DACK_J2EE.repository.CategoryRepository;
import com.example.DACK_J2EE.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found!"));
    }

    public Product createProduct(ProductCreateDTO dto) {
        if (productRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Product name already exists!");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found!"));

        if (dto.getImage() == null || dto.getImage().isEmpty()) {
            throw new RuntimeException("Image upload failed!");
        }

        Product product = Product.builder()
                .name(dto.getName().trim())
                .price(dto.getPrice())
                .image(dto.getImage())
                .category(category)
                .description(dto.getDescription() != null ? dto.getDescription().trim() : "")
                .countInStock(dto.getCountInStock() != null ? dto.getCountInStock() : 0)
                .rating(dto.getRating() != null ? dto.getRating() : 0.0)
                .build();

        Product savedProduct = productRepository.save(product);
        return savedProduct;
    }

    public Product updateProduct(Long id, ProductCreateDTO dto) {
        Product product = getProductById(id);

        if (dto.getName() != null) {
            product.setName(dto.getName());
        }
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }
        if (dto.getRating() != null) {
            product.setRating(dto.getRating());
        }
        if (dto.getCountInStock() != null) {
            product.setCountInStock(dto.getCountInStock());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found!"));
            product.setCategory(category);
        }
        if (dto.getImage() != null) {
            product.setImage(dto.getImage());
        }

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    public List<Product> searchProducts(String keyword, Long categoryId, Double minPrice, 
                                       Double maxPrice, Double rating) {
        return productRepository.searchProducts(keyword, categoryId, minPrice, maxPrice, rating);
    }
}

