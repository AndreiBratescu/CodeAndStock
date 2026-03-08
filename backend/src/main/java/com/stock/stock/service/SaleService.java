package com.stock.stock.service;

import com.stock.stock.domain.AppUser;
import com.stock.stock.domain.InventoryItem;
import com.stock.stock.domain.Sale;
import com.stock.stock.repository.AppUserRepository;
import com.stock.stock.repository.InventoryRepository;
import com.stock.stock.repository.SaleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final AppUserRepository appUserRepository;
    private final InventoryRepository inventoryRepository;

    public SaleService(SaleRepository saleRepository, AppUserRepository appUserRepository, InventoryRepository inventoryRepository) {
        this.saleRepository = saleRepository;
        this.appUserRepository = appUserRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public Sale createSale(Sale sale) {
        // Get current authenticated user
        String username = getCurrentUsername();
        AppUser currentUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Verify user owns the store stand
        if (sale.getStoreStand() == null || !sale.getStoreStand().getId().equals(currentUser.getStoreStand().getId())) {
            throw new RuntimeException("User does not have permission to sell from this store stand");
        }

        // Verify sufficient inventory
        List<InventoryItem> inventoryItems = inventoryRepository.findByStoreStandId(sale.getStoreStand().getId());
        InventoryItem inventoryForProduct = inventoryItems.stream()
                .filter(item -> item.getProduct().getId().equals(sale.getProduct().getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found in store stand inventory"));

        if (inventoryForProduct.getQuantity() < sale.getQuantitySold()) {
            throw new RuntimeException("Insufficient inventory. Available: " + inventoryForProduct.getQuantity());
        }

        // Deduct from inventory
        inventoryForProduct.setQuantity(inventoryForProduct.getQuantity() - sale.getQuantitySold());
        inventoryRepository.save(inventoryForProduct);

        // Set user and date
        sale.setAppUser(currentUser);
        if (sale.getSaleDate() == null) {
            sale.setSaleDate(LocalDate.now());
        }

        Sale savedSale = saleRepository.save(sale);
        log.info("Sale created: {} units of product {} by user {}",
                sale.getQuantitySold(), sale.getProduct().getId(), username);
        return savedSale;
    }

    @Transactional(readOnly = true)
    public List<Sale> getUserSales() {
        String username = getCurrentUsername();
        AppUser currentUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        return saleRepository.findByAppUser(currentUser);
    }

    @Transactional(readOnly = true)
    public List<Sale> getUserSalesByStand() {
        String username = getCurrentUsername();
        AppUser currentUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (currentUser.getStoreStand() == null) {
            throw new RuntimeException("User has no assigned store stand");
        }

        return saleRepository.findByAppUserAndStoreStand(currentUser, currentUser.getStoreStand());
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }
        return authentication.getName();
    }
}

