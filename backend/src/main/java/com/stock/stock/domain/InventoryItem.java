package com.stock.stock.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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

    public long getDaysInStock() {
        return arrivalDate != null ? ChronoUnit.DAYS.between(arrivalDate, LocalDate.now()) : 0L;
    }
}
