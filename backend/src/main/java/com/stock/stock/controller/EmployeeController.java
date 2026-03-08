package com.stock.stock.controller;

import com.stock.stock.domain.InventoryItem;
import com.stock.stock.domain.Sale;
import com.stock.stock.service.ProductService;
import com.stock.stock.service.SaleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/employee")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class EmployeeController {

    private final ProductService productService;
    private final SaleService saleService;

    public EmployeeController(ProductService productService, SaleService saleService) {
        this.productService = productService;
        this.saleService = saleService;
    }

    /**
     * Returnează inventarul complet pentru standul angajatului logat.
     * Folosit pentru Lista 1 din Frontend.
     */
    @GetMapping("/inventory")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InventoryItem>> getMyInventory() {
        try {
            log.info("Employee requesting inventory for their stand");
            List<InventoryItem> inventory = productService.getInventoryByUserStand();
            return ResponseEntity.ok(inventory);
        } catch (RuntimeException e) {
            log.error("Error fetching employee inventory: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Salvează o vânzare nouă și scade automat din stoc.
     * Folosit de butonul "Save Sale" din Frontend.
     */
    @PostMapping("/sale")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Sale> registerSale(@RequestBody Sale saleRequest) {
        try {
            log.info("Attempting to register sale for product: {}", saleRequest.getProduct().getSku());
            Sale savedSale = saleService.createSale(saleRequest);
            return ResponseEntity.ok(savedSale);
        } catch (RuntimeException e) {
            log.error("Sale registration failed: {}", e.getMessage());
            // Returnăm eroarea de business (ex: "Stoc insuficient") către front-end
            return ResponseEntity.badRequest().header("Error-Message", e.getMessage()).build();
        }
    }
}
