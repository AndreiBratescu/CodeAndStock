package com.stock.stock.controller;

import com.stock.stock.domain.InventoryItem;
import com.stock.stock.domain.Product;
import com.stock.stock.domain.StoreStand;
import com.stock.stock.dto.*;
import com.stock.stock.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only REST controller.
 * All endpoints require ROLE_ADMIN authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:8080" })
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        log.info("Admin dashboard requested");
        DashboardResponse dashboard = adminService.getDashboard();
        return ResponseEntity.ok(dashboard);
    }

    // ==================== USER MANAGEMENT ====================

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Admin listing all users");
        List<UserResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = adminService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.error("Error fetching user: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long id,
            @RequestBody RoleUpdateRequest request) {
        try {
            log.info("Admin updating role for user id={} to {}", id, request.getRole());
            UserResponse user = adminService.updateUserRole(id, request);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.error("Error updating user role: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<UserResponse> toggleUserEnabled(@PathVariable Long id) {
        try {
            log.info("Admin toggling enabled status for user id={}", id);
            UserResponse user = adminService.toggleUserEnabled(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.error("Error toggling user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== STORE MANAGEMENT ====================

    @GetMapping("/stores")
    public ResponseEntity<List<StoreStand>> getAllStores() {
        log.info("Admin listing all stores");
        List<StoreStand> stores = adminService.getAllStores();
        return ResponseEntity.ok(stores);
    }

    @GetMapping("/stores/{id}")
    public ResponseEntity<StoreStand> getStoreById(@PathVariable Long id) {
        try {
            StoreStand store = adminService.getStoreById(id);
            return ResponseEntity.ok(store);
        } catch (RuntimeException e) {
            log.error("Error fetching store: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/stores")
    public ResponseEntity<StoreStand> createStore(@RequestBody StoreStandRequest request) {
        try {
            log.info("Admin creating store: {} in {}", request.getMallName(), request.getCity());
            StoreStand store = adminService.createStore(request);
            return ResponseEntity.ok(store);
        } catch (RuntimeException e) {
            log.error("Error creating store: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/stores/{id}")
    public ResponseEntity<StoreStand> updateStore(
            @PathVariable Long id,
            @RequestBody StoreStandRequest request) {
        try {
            log.info("Admin updating store id={}", id);
            StoreStand store = adminService.updateStore(id, request);
            return ResponseEntity.ok(store);
        } catch (RuntimeException e) {
            log.error("Error updating store: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/stores/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        try {
            log.info("Admin deleting store id={}", id);
            adminService.deleteStore(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting store: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== PRODUCT MANAGEMENT ====================

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("Admin listing all products");
        List<Product> products = adminService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            Product product = adminService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            log.error("Error fetching product: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        try {
            log.info("Admin creating product: {}", product.getName());
            Product created = adminService.createProduct(product);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            log.error("Error creating product: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {
        try {
            log.info("Admin updating product id={}", id);
            Product updated = adminService.updateProduct(id, product);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Error updating product: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            log.info("Admin deleting product id={}", id);
            adminService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting product: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== INVENTORY MANAGEMENT ====================

    @GetMapping("/inventory")
    public ResponseEntity<List<InventoryItem>> getAllInventory() {
        log.info("Admin listing all inventory");
        List<InventoryItem> inventory = adminService.getAllInventory();
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/inventory/store/{storeId}")
    public ResponseEntity<List<InventoryItem>> getInventoryByStore(@PathVariable Long storeId) {
        log.info("Admin listing inventory for store id={}", storeId);
        List<InventoryItem> inventory = adminService.getInventoryByStore(storeId);
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/inventory")
    public ResponseEntity<InventoryItem> addInventory(@RequestBody InventoryRequest request) {
        try {
            log.info("Admin adding inventory: {} units of product {} to store {}",
                    request.getQuantity(), request.getProductId(), request.getStoreStandId());
            InventoryItem item = adminService.addInventory(request);
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            log.error("Error adding inventory: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
