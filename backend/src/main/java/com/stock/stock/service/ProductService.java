package com.stock.stock.service;

import com.stock.stock.domain.AppUser;
import com.stock.stock.domain.InventoryItem;
import com.stock.stock.domain.Product;
import com.stock.stock.repository.AppUserRepository;
import com.stock.stock.repository.InventoryRepository;
import com.stock.stock.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final AppUserRepository appUserRepository;

    public ProductService(ProductRepository productRepository, InventoryRepository inventoryRepository, AppUserRepository appUserRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public Product createProduct(Product product) {
        String brand = product.getBrand().trim().toUpperCase();
        String model = product.getModel().trim().toUpperCase().replace(" ", "");
        String color = product.getColor().trim().toUpperCase();

        // 2. Generare SKU automată: CASE-IPHONE-15-RED
        String generatedSku = String.format("%s-%s-%s-%s",
                product.getCategory(), brand, model, color);

        if (productRepository.existsBySku(generatedSku)) {
            throw new RuntimeException("Acest produs (SKU: " + generatedSku + ") există deja în sistem!");
        }

        product.setSku(generatedSku);
        product.setBrand(brand);
        product.setModel(model);
        product.setColor(color);

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product updated) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produsul cu id " + id + " nu există."));

        String brand = updated.getBrand().trim().toUpperCase();
        String model = updated.getModel().trim().toUpperCase().replace(" ", "");
        String color = updated.getColor().trim().toUpperCase();

        String generatedSku = String.format("%s-%s-%s-%s",
                updated.getCategory(), brand, model, color);

        productRepository.findBySku(generatedSku)
                .filter(p -> !p.getId().equals(id))
                .ifPresent(p -> {
                    throw new RuntimeException("Alt produs cu același SKU (" + generatedSku + ") există deja.");
                });

        existing.setCategory(updated.getCategory());
        existing.setName(updated.getName());
        existing.setBrand(brand);
        existing.setModel(model);
        existing.setColor(color);
        existing.setPrice(updated.getPrice());
        existing.setSku(generatedSku);

        return productRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByUserStand() {
        String username = getCurrentUsername();
        AppUser currentUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (currentUser.getStoreStand() == null) {
            throw new RuntimeException("User has no assigned store stand");
        }

        // Get all inventory items for user's store stand
        List<InventoryItem> inventoryItems = inventoryRepository.findByStoreStandId(currentUser.getStoreStand().getId());

        // Extract products from inventory items
        return inventoryItems.stream()
                .map(InventoryItem::getProduct)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> getInventoryByUserStand() {
        String username = getCurrentUsername();
        AppUser currentUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (currentUser.getStoreStand() == null) {
            throw new RuntimeException("User has no assigned store stand");
        }

        return inventoryRepository.findByStoreStandId(currentUser.getStoreStand().getId());
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }
        return authentication.getName();
    }
}
