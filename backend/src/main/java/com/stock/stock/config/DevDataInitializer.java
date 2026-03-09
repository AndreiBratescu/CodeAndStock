package com.stock.stock.config;

import com.stock.stock.domain.*;
import com.stock.stock.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    private final StoreStandRepository storeStandRepository;
    private final AppUserRepository appUserRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final SaleRepository saleRepository;
    private final RegistrationRequestRepository registrationRequestRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataInitializer(StoreStandRepository storeStandRepository,
            AppUserRepository appUserRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            SaleRepository saleRepository,
            RegistrationRequestRepository registrationRequestRepository,
            PasswordEncoder passwordEncoder) {
        this.storeStandRepository = storeStandRepository;
        this.appUserRepository = appUserRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.saleRepository = saleRepository;
        this.registrationRequestRepository = registrationRequestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("=== DEV DataInitializer: populating H2 database ===");

        String hashedPassword = passwordEncoder.encode("password123");

        // Store Stands
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

        // Users
        AppUser empPlaza = AppUser.builder()
                .username("employee_plaza")
                .email("employee.plaza@example.com")
                .password(hashedPassword)
                .roles("ROLE_EMPLOYEE")
                .enabled(true)
                .storeStand(plaza)
                .build();
        appUserRepository.save(empPlaza);

        AppUser empAfi = AppUser.builder()
                .username("employee_afi")
                .email("employee.afi@example.com")
                .password(hashedPassword)
                .roles("ROLE_EMPLOYEE")
                .enabled(true)
                .storeStand(afi)
                .build();
        appUserRepository.save(empAfi);

        AppUser empConstanta = AppUser.builder()
                .username("employee_constanta")
                .email("employee.constanta@example.com")
                .password(hashedPassword)
                .roles("ROLE_EMPLOYEE")
                .enabled(true)
                .storeStand(constanta)
                .build();
        appUserRepository.save(empConstanta);

        AppUser admin = AppUser.builder()
                .username("admin_user")
                .email("admin@example.com")
                .password(hashedPassword)
                .roles("ROLE_ADMIN")
                .enabled(true)
                .storeStand(null)
                .build();
        appUserRepository.save(admin);

        // Products
        Product p1 = new Product();
        p1.setSku("CASE-APPLE-IPHONE15-RED");
        p1.setCategory(ProductCategory.CASE);
        p1.setName("iPhone 15 Red Case");
        p1.setBrand("APPLE");
        p1.setModel("IPHONE15");
        p1.setColor("RED");
        p1.setPrice(29.99);
        p1 = productRepository.save(p1);

        Product p2 = new Product();
        p2.setSku("CASE-APPLE-IPHONE15-BLACK");
        p2.setCategory(ProductCategory.CASE);
        p2.setName("iPhone 15 Black Case");
        p2.setBrand("APPLE");
        p2.setModel("IPHONE15");
        p2.setColor("BLACK");
        p2.setPrice(29.99);
        p2 = productRepository.save(p2);

        Product p3 = new Product();
        p3.setSku("SCREEN-APPLE-IPHONE15-CLEAR");
        p3.setCategory(ProductCategory.SCREEN_PROTECTOR);
        p3.setName("iPhone 15 Clear Screen Protector");
        p3.setBrand("APPLE");
        p3.setModel("IPHONE15");
        p3.setColor("CLEAR");
        p3.setPrice(9.99);
        p3 = productRepository.save(p3);

        // Inventory
        InventoryItem inv1 = new InventoryItem();
        inv1.setProduct(p1);
        inv1.setStoreStand(plaza);
        inv1.setQuantity(100);
        inv1.setArrivalDate(LocalDate.of(2026, 2, 1));
        inventoryRepository.save(inv1);

        InventoryItem inv2 = new InventoryItem();
        inv2.setProduct(p2);
        inv2.setStoreStand(afi);
        inv2.setQuantity(85);
        inv2.setArrivalDate(LocalDate.of(2026, 2, 5));
        inventoryRepository.save(inv2);

        // Sales
        Sale s1 = new Sale();
        s1.setProduct(p1);
        s1.setStoreStand(plaza);
        s1.setAppUser(empPlaza);
        s1.setQuantitySold(5);
        s1.setSaleDate(LocalDate.of(2026, 3, 1));
        saleRepository.save(s1);

        // Sample pending registration requests for admin testing
        RegistrationRequest req1 = RegistrationRequest.builder()
                .email("ion.popescu@gmail.com")
                .storeStand(plaza)
                .status(RegistrationRequest.RequestStatus.PENDING)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();
        registrationRequestRepository.save(req1);

        RegistrationRequest req2 = RegistrationRequest.builder()
                .email("maria.ionescu@yahoo.com")
                .storeStand(afi)
                .status(RegistrationRequest.RequestStatus.PENDING)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();
        registrationRequestRepository.save(req2);

        RegistrationRequest req3 = RegistrationRequest.builder()
                .email("andrei.vasile@outlook.com")
                .storeStand(constanta)
                .status(RegistrationRequest.RequestStatus.PENDING)
                .createdAt(LocalDateTime.now().minusMinutes(30))
                .build();
        registrationRequestRepository.save(req3);

        log.info("=== DEV DataInitializer: done (3 stores, 4 users, 3 products, 3 pending requests) ===");
    }
}
