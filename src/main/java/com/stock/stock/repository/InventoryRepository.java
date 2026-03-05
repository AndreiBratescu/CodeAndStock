package com.stock.stock.repository;

import com.stock.stock.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByArrivalDateBefore(LocalDate date);

    List<InventoryItem> findByStoreStandId(Long standId);
}