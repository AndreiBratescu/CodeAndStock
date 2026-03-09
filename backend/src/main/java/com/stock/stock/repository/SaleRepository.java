package com.stock.stock.repository;

import com.stock.stock.domain.AppUser;
import com.stock.stock.domain.Sale;
import com.stock.stock.domain.StoreStand;
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

    @Query("SELECT s.product.id, s.storeStand.id, SUM(s.quantitySold) as totalSold " +
            "FROM Sale s " +
            "WHERE s.saleDate >= :sinceDate " +
            "GROUP BY s.product.id, s.storeStand.id")
    List<Object[]> aggregateSalesByProductAndStand(
            @Param("sinceDate") LocalDate sinceDate);

    List<Sale> findByAppUser(AppUser appUser);

    // Toate vânzările pentru un anumit stand (indiferent de utilizator)
    List<Sale> findByStoreStand(StoreStand storeStand);

    List<Sale> findByAppUserAndStoreStand(AppUser appUser, StoreStand storeStand);

}
