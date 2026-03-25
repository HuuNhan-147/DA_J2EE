package com.example.DACK_J2EE.dto;

import jakarta.persistence.ElementCollection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateDTO {
    private String name;
    private Double price;
    private String image;
    private Long categoryId;
    private String description;
    private Integer countInStock;
    private Double rating;
    @ElementCollection
    private List<ProductSpecificationDTO> specifications;
}
