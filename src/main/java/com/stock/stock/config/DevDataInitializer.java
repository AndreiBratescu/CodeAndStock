package com.stock.stock.config;

import com.stock.stock.domain.*;
import com.stock.stock.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

/**
 * Initializes sample data for the dev profile using the app's own
 * PasswordEncoder.
 * This ensures BCrypt hashes are always valid.
 * Only runs when the "dev" profile is active.
 */
@Slf4j
@Configuration
@Profile("dev")
public class DevDataInitializer {

    @Bean
    public CommandLineRunner initDevData(
            StoreStandRepository storeStandRepository,
            AppUserRepository appUserRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            SaleRepository saleRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // Only initialize if database is empty
            if (storeStandRepository.count() > 0) {
                log.info("Dev data already exists, skipping initialization.");
                return;
            }

            log.info("Initializing dev sample data...");

            // ---- Store Stands ----
            StoreStand plaza = new StoreStand();
            plaza.setCity(City.BUCURESTI);
            plaza.setMallName("Plaza Mall Downtown");
            plaza.setStorageCapacity(5000);
            plaza = storeStandRepository.save(plaza);

            StoreStand afi = new StoreStand();
            afi.setCity(City.BUCURESTI);
            afi.setMallName("AFI Cotroceni");
            afi.setStorageCapacity(4500);
            afi = storeStandRepository.save(afi);

            StoreStand constanta = new StoreStand();
            constanta.setCity(City.CONSTANTA);
            constanta.setMallName("Constanta City Center");
            constanta.setStorageCapacity(3000);
            constanta = storeStandRepository.save(constanta);

            log.info("Created {} store stands", storeStandRepository.count());

            // ---- Users (password: password123) ----
            String encodedPassword = passwordEncoder.encode("password123");

            AppUser empPlaza = AppUser.builder()
                    .username("employee_plaza")
                    .email("employee.plaza@example.com")
                    .password(encodedPassword)
                    .roles("ROLE_EMPLOYEE")
                    .enabled(true)
                    .storeStand(plaza)
                    .build();
            appUserRepository.save(empPlaza);

            AppUser empAfi = AppUser.builder()
                    .username("employee_afi")
                    .email("employee.afi@example.com")
                    .password(encodedPassword)
                    .roles("ROLE_EMPLOYEE")
                    .enabled(true)
                    .storeStand(afi)
                    .build();
            appUserRepository.save(empAfi);

            AppUser empConstanta = AppUser.builder()
                    .username("employee_constanta")
                    .email("employee.constanta@example.com")
                    .password(encodedPassword)
                    .roles("ROLE_EMPLOYEE")
                    .enabled(true)
                    .storeStand(constanta)
                    .build();
            appUserRepository.save(empConstanta);

            AppUser adminUser = AppUser.builder()
                    .username("admin_user")
                    .email("admin@example.com")
                    .password(encodedPassword)
                    .roles("ROLE_ADMIN")
                    .enabled(true)
                    .storeStand(null) // Admin doesn't need a store assignment
                    .build();
            appUserRepository.save(adminUser);

            log.info("Created {} users", appUserRepository.count());

            // ---- Products ----
            Product case1 = new Product();
            case1.setSku("CASE-APPLE-IPHONE15-RED");
            case1.setCategory(ProductCategory.CASE);
            case1.setName("iPhone 15 Red Case");
            case1.setBrand("APPLE");
            case1.setModel("IPHONE15");
            case1.setColor("RED");
            case1.setPrice(29.99);
            case1 = productRepository.save(case1);

            Product case2 = new Product();
            case2.setSku("CASE-APPLE-IPHONE15-BLACK");
            case2.setCategory(ProductCategory.CASE);
            case2.setName("iPhone 15 Black Case");
            case2.setBrand("APPLE");
            case2.setModel("IPHONE15");
            case2.setColor("BLACK");
            case2.setPrice(29.99);
            case2 = productRepository.save(case2);

            Product screen1 = new Product();
            screen1.setSku("SCREEN_PROTECTOR-APPLE-IPHONE15-CLEAR");
            screen1.setCategory(ProductCategory.SCREEN_PROTECTOR);
            screen1.setName("iPhone 15 Clear Screen Protector");
            screen1.setBrand("APPLE");
            screen1.setModel("IPHONE15");
            screen1.setColor("CLEAR");
            screen1.setPrice(9.99);
            screen1 = productRepository.save(screen1);

            Product charger1 = new Product();
            charger1.setSku("CHARGER-APPLE-IPHONE-WHITE");
            charger1.setCategory(ProductCategory.CHARGER);
            charger1.setName("Apple USB-C Charger");
            charger1.setBrand("APPLE");
            charger1.setModel("GENERIC");
            charger1.setColor("WHITE");
            charger1.setPrice(19.99);
            charger1 = productRepository.save(charger1);

            Product cable1 = new Product();
            cable1.setSku("CABLE-APPLE-IPHONE-WHITE");
            cable1.setCategory(ProductCategory.CABLE);
            cable1.setName("Apple USB-C Cable");
            cable1.setBrand("APPLE");
            cable1.setModel("GENERIC");
            cable1.setColor("WHITE");
            cable1.setPrice(14.99);
            cable1 = productRepository.save(cable1);

            log.info("Created {} products", productRepository.count());

            // ---- Inventory: Plaza Mall ----
            createInventory(inventoryRepository, case1, plaza, 100, LocalDate.of(2026, 2, 1));
            createInventory(inventoryRepository, case2, plaza, 85, LocalDate.of(2026, 2, 5));
            createInventory(inventoryRepository, screen1, plaza, 150, LocalDate.of(2026, 2, 10));
            createInventory(inventoryRepository, charger1, plaza, 60, LocalDate.of(2026, 2, 15));
            createInventory(inventoryRepository, cable1, plaza, 75, LocalDate.of(2026, 2, 20));

            // ---- Inventory: AFI Cotroceni ----
            createInventory(inventoryRepository, case1, afi, 120, LocalDate.of(2026, 2, 1));
            createInventory(inventoryRepository, case2, afi, 95, LocalDate.of(2026, 2, 5));
            createInventory(inventoryRepository, screen1, afi, 160, LocalDate.of(2026, 2, 10));
            createInventory(inventoryRepository, charger1, afi, 70, LocalDate.of(2026, 2, 15));
            createInventory(inventoryRepository, cable1, afi, 85, LocalDate.of(2026, 2, 20));

            // ---- Inventory: Constanta ----
            createInventory(inventoryRepository, case1, constanta, 80, LocalDate.of(2026, 2, 1));
            createInventory(inventoryRepository, case2, constanta, 65, LocalDate.of(2026, 2, 5));
            createInventory(inventoryRepository, screen1, constanta, 110, LocalDate.of(2026, 2, 10));
            createInventory(inventoryRepository, charger1, constanta, 45, LocalDate.of(2026, 2, 15));
            createInventory(inventoryRepository, cable1, constanta, 55, LocalDate.of(2026, 2, 20));

            log.info("Created {} inventory items", inventoryRepository.count());

            // ---- Sample Sales ----
            createSale(saleRepository, case1, plaza, empPlaza, 5, LocalDate.of(2026, 3, 1));
            createSale(saleRepository, case2, plaza, empPlaza, 3, LocalDate.of(2026, 3, 2));
            createSale(saleRepository, charger1, plaza, empPlaza, 2, LocalDate.of(2026, 3, 3));
            createSale(saleRepository, case1, afi, empAfi, 4, LocalDate.of(2026, 3, 1));
            createSale(saleRepository, screen1, afi, empAfi, 10, LocalDate.of(2026, 3, 4));
            createSale(saleRepository, cable1, constanta, empConstanta, 6, LocalDate.of(2026, 3, 2));

            log.info("Created {} sales", saleRepository.count());
            log.info("Dev data initialization complete!");
        };
    }

    private void createInventory(InventoryRepository repo, Product product, StoreStand store,
            int quantity, LocalDate arrivalDate) {
        InventoryItem item = new InventoryItem();
        item.setProduct(product);
        item.setStoreStand(store);
        item.setQuantity(quantity);
        item.setArrivalDate(arrivalDate);
        repo.save(item);
    }

    private void createSale(SaleRepository repo, Product product, StoreStand store,
            AppUser user, int qty, LocalDate date) {
        Sale sale = new Sale();
        sale.setProduct(product);
        sale.setStoreStand(store);
        sale.setAppUser(user);
        sale.setQuantitySold(qty);
        sale.setSaleDate(date);
        repo.save(sale);
    }
}
