package com.stock.stock.repository;

import com.stock.stock.domain.Product;
import com.stock.stock.domain.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    Optional<Product> findBySku(String sku);

    List<Product> findByCategory(ProductCategory category);

    List<Product> findByBrandIgnoreCase(String brand);

    List<Product> findByModelContainingIgnoreCase(String model);
}