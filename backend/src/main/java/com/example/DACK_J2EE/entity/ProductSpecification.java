package com.example.DACK_J2EE.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_specifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSpecification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "spec_key", nullable = false)
    private String specKey;

    @Column(name = "spec_value", nullable = false)
    private String specValue;

    @Column(length = 50)
    private String unit;

    @Column(name = "spec_group")
    private String specGroup;
}
