package com.stock.stock.controller;

import com.stock.stock.domain.InventoryItem;
import com.stock.stock.domain.Product;
import com.stock.stock.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:8080"})
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/my-stand")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Product>> getProductsByMyStand() {
        try {
            List<Product> products = productService.getProductsByUserStand();
            return ResponseEntity.ok(products);
        } catch (RuntimeException e) {
            log.error("Error fetching products for user stand: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/inventory/my-stand")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InventoryItem>> getInventoryByMyStand() {
        try {
            List<InventoryItem> inventory = productService.getInventoryByUserStand();
            return ResponseEntity.ok(inventory);
        } catch (RuntimeException e) {
            log.error("Error fetching inventory for user stand: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

