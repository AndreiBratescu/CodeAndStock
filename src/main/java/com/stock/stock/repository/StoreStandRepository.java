package com.stock.stock.repository;

import com.stock.stock.domain.City;
import com.stock.stock.domain.StoreStand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreStandRepository extends JpaRepository<StoreStand, Long> {

    List<StoreStand> findByCity(City city);
}

