package com.stock.stock.repository;

import com.stock.stock.domain.AppUser;
import com.stock.stock.domain.StoreStand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByStoreStand(StoreStand storeStand);
}

