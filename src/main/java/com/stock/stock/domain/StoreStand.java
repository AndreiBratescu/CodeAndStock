package com.stock.stock.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "store_stands")
@Data
public class StoreStand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private City city;

    private String mallName;

    private Integer storageCapacity;
}
