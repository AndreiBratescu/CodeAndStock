package com.stock.stock.controller;

import com.stock.stock.domain.Sale;
import com.stock.stock.service.SaleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Sale> createSale(@RequestBody Sale sale) {
        try {
            log.info("Creating sale: {} units of product {}", sale.getQuantitySold(), sale.getProduct().getId());
            Sale createdSale = saleService.createSale(sale);
            return ResponseEntity.ok(createdSale);
        } catch (RuntimeException e) {
            log.error("Error creating sale: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-sales")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Sale>> getMyUserSales() {
        try {
            List<Sale> sales = saleService.getUserSales();
            return ResponseEntity.ok(sales);
        } catch (RuntimeException e) {
            log.error("Error fetching user sales: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-stand-sales")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Sale>> getMyStandSales() {
        try {
            List<Sale> sales = saleService.getUserSalesByStand();
            return ResponseEntity.ok(sales);
        } catch (RuntimeException e) {
            log.error("Error fetching stand sales: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

