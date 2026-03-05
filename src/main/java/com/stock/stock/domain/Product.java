package com.stock.stock.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku; // Stock Keeping Unit (ex: CASE-IPHONE-15-RED)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    private String name;
    private String brand;
    private String model;
    private String color;

    private Double price;
}