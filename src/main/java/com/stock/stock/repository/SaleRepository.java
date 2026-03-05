package com.stock.stock.repository;

import com.stock.stock.domain.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query("SELECT s.storeStand.id, SUM(s.quantitySold) as totalSold " +
            "FROM Sale s " +
            "WHERE s.product.id = :productId " +
            "AND s.saleDate >= :sinceDate " +
            "GROUP BY s.storeStand.id " +
            "ORDER BY totalSold DESC")
    List<Object[]> findBestSellingStandsForProduct(
            @Param("productId") Long productId,
            @Param("sinceDate") LocalDate sinceDate);
}
