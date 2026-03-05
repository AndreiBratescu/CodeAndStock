package com.stock.stock.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "inventory")
@Data
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "stand_id")
    private StoreStand storeStand;

    private Integer quantity;

    @Column(name = "arrival_date")
    private LocalDate arrivalDate;

    // Metodă utilitară pentru business logic
    public long getDaysInStock() {
        return java.time.temporal.ChronoUnit.DAYS.between(arrivalDate, LocalDate.now());
    }
}
