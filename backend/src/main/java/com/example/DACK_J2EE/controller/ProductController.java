package com.example.DACK_J2EE.controller;

import com.example.DACK_J2EE.dto.ProductCreateDTO;
import com.example.DACK_J2EE.dto.ProductSpecificationDTO;
import com.example.DACK_J2EE.entity.Product;
import com.example.DACK_J2EE.service.ProductService;
import com.example.DACK_J2EE.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createProduct(
            @RequestParam("name") String name,
            @RequestParam("price") String priceStr,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("category") String categoryIdStr,
            @RequestParam(value = "countInStock", required = false, defaultValue = "0") String countInStockStr,
            @RequestParam(value = "rating", required = false, defaultValue = "0.0") String ratingStr,
            @RequestParam(value = "specifications", required = false) String specifications,
            @RequestParam("image") MultipartFile imageFile) {
        try {
            Double price = (priceStr != null && !priceStr.isEmpty() && !priceStr.equals("null") && !priceStr.equals("undefined")) ? Double.parseDouble(priceStr) : 0.0;
            Long categoryId = (categoryIdStr != null && !categoryIdStr.isEmpty() && !categoryIdStr.equals("null") && !categoryIdStr.equals("undefined")) ? Long.parseLong(categoryIdStr) : null;
            Integer countInStock = (countInStockStr != null && !countInStockStr.isEmpty() && !countInStockStr.equals("null") && !countInStockStr.equals("undefined")) ? Integer.parseInt(countInStockStr) : 0;
            Double rating = (ratingStr != null && !ratingStr.isEmpty() && !ratingStr.equals("null") && !ratingStr.equals("undefined")) ? Double.parseDouble(ratingStr) : 0.0;

            String imageUrl = cloudinaryService.uploadImage(imageFile, "products");
            
            ProductCreateDTO dto = ProductCreateDTO.builder()
                .name(name)
                .price(price)
                .description(description)
                .categoryId(categoryId)
                .countInStock(countInStock)
                .rating(rating)
                .image(imageUrl)
                .build();
            Product product = productService.createProduct(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product created successfully!");
            response.put("product", product);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
public ResponseEntity<?> updateProduct(
        @PathVariable Long id,
        @RequestParam(value = "name", required = false) String name,
        @RequestParam(value = "price", required = false) String priceStr,
        @RequestParam(value = "description", required = false) String description,
        @RequestParam(value = "category", required = false) String categoryIdStr,
        @RequestParam(value = "countInStock", required = false) String countInStockStr,
        @RequestParam(value = "rating", required = false) String ratingStr,
        @RequestParam(value = "specifications", required = false) String specifications,
        @RequestParam(value = "image", required = false) MultipartFile imageFile) {
    try {
        System.out.println("=== UPDATE PRODUCT DEBUG ===");
        System.out.println("ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("PriceStr: " + priceStr);
        System.out.println("CategoryStr: " + categoryIdStr);
        System.out.println("CountInStockStr: " + countInStockStr);
        System.out.println("RatingStr: " + ratingStr);
        System.out.println("Specifications: " + specifications);
        
        Double price = (priceStr != null && !priceStr.isEmpty() && !priceStr.equals("null") && !priceStr.equals("undefined"))
                ? Double.parseDouble(priceStr) : null;
        System.out.println("Parsed Price: " + price);

        Long categoryId = (categoryIdStr != null && !categoryIdStr.isEmpty() && !categoryIdStr.equals("null") && !categoryIdStr.equals("undefined"))
                ? Long.parseLong(categoryIdStr) : null;

        Integer countInStock = (countInStockStr != null && !countInStockStr.isEmpty() && !countInStockStr.equals("null") && !countInStockStr.equals("undefined"))
                ? Integer.parseInt(countInStockStr) : null;

        Double rating = (ratingStr != null && !ratingStr.isEmpty() && !ratingStr.equals("null") && !ratingStr.equals("undefined"))
                ? Double.parseDouble(ratingStr) : null;

        String imageUrl = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(imageFile, "products");
        }
        ObjectMapper objectMapper = new ObjectMapper();

        List<ProductSpecificationDTO> specList = null;

        if (specifications != null && !specifications.isEmpty()) {
            specList = objectMapper.readValue(
                specifications,
                new TypeReference<List<ProductSpecificationDTO>>() {}
            );
        }
        ProductCreateDTO dto = ProductCreateDTO.builder()
                .name(name)
                .price(price)
                .description(description)
                .categoryId(categoryId)
                .countInStock(countInStock)
                .rating(rating)
                .specifications(specList)
                .image(imageUrl) // ⚠️ có thể null (service sẽ xử lý)
                .build();

        System.out.println("Saving DTO...");

        Product product = productService.updateProduct(id, dto);
        System.out.println("Successfully updated product from DB.");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Product updated successfully!");
        response.put("product", product);

        return ResponseEntity.ok(response);

    } catch (Exception e) {
        System.out.println("=== EXCEPTION in updateProduct ===");
        e.printStackTrace(); // 👈 thêm để debug server
        Map<String, String> error = new HashMap<>();
        error.put("message", "Error: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Product deleted successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double rating) {
        try {
            List<Product> products = productService.searchProducts(keyword, category, minPrice, maxPrice, rating);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
