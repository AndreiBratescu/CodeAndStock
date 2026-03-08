package com.stock.stock.service;

import com.stock.stock.domain.AppUser;
import com.stock.stock.domain.InventoryItem;
import com.stock.stock.repository.AppUserRepository;
import com.stock.stock.repository.InventoryRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final AppUserRepository appUserRepository;

    public InventoryService(InventoryRepository inventoryRepository, AppUserRepository appUserRepository) {
        this.inventoryRepository = inventoryRepository;
        this.appUserRepository = appUserRepository;
    }

    public List<InventoryItem> getInventoryForCurrentUserStand() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser currentUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return inventoryRepository.findByStoreStandId(currentUser.getStoreStand().getId());
    }
}
