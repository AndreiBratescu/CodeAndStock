package com.stock.stock.service;

import com.stock.stock.domain.*;
import com.stock.stock.dto.*;
import com.stock.stock.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service layer for admin operations:
 * - Dashboard statistics
 * - User & role management
 * - Store stand CRUD
 * - Product CRUD
 * - Inventory management
 */
@Slf4j
@Service
public class AdminService {

    private static final Set<String> VALID_ROLES = Set.of(
            "ROLE_EMPLOYEE", "ROLE_ADMIN", "ROLE_MANAGER");

    private final AppUserRepository appUserRepository;
    private final StoreStandRepository storeStandRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final SaleRepository saleRepository;

    public AdminService(AppUserRepository appUserRepository,
            StoreStandRepository storeStandRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            SaleRepository saleRepository) {
        this.appUserRepository = appUserRepository;
        this.storeStandRepository = storeStandRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.saleRepository = saleRepository;
    }

    // ==================== DASHBOARD ====================

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        long totalStores = storeStandRepository.count();
        long totalProducts = productRepository.count();
        long totalUsers = appUserRepository.count();
        long totalInventoryItems = inventoryRepository.count();
        long totalSales = saleRepository.count();

        int totalStockQuantity = inventoryRepository.findAll().stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();

        int totalSoldQuantity = saleRepository.findAll().stream()
                .mapToInt(sale -> sale.getQuantitySold() != null ? sale.getQuantitySold() : 0)
                .sum();

        return DashboardResponse.builder()
                .totalStores(totalStores)
                .totalProducts(totalProducts)
                .totalUsers(totalUsers)
                .totalInventoryItems(totalInventoryItems)
                .totalSales(totalSales)
                .totalStockQuantity(totalStockQuantity)
                .totalSoldQuantity(totalSoldQuantity)
                .build();
    }

    // ==================== USER MANAGEMENT ====================

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return appUserRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return toUserResponse(user);
    }

    @Transactional
    public UserResponse updateUserRole(Long userId, RoleUpdateRequest request) {
        String newRole = request.getRole();
        if (!VALID_ROLES.contains(newRole)) {
            throw new RuntimeException("Invalid role: " + newRole
                    + ". Valid roles: " + VALID_ROLES);
        }

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        String oldRole = user.getRoles();
        user.setRoles(newRole);
        appUserRepository.save(user);

        log.info("Role updated for user '{}': {} -> {}", user.getUsername(), oldRole, newRole);
        return toUserResponse(user);
    }

    @Transactional
    public UserResponse toggleUserEnabled(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setEnabled(!user.getEnabled());
        appUserRepository.save(user);

        log.info("User '{}' {} by admin", user.getUsername(),
                user.getEnabled() ? "enabled" : "disabled");
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(AppUser user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .enabled(user.getEnabled())
                .storeStandId(user.getStoreStand() != null ? user.getStoreStand().getId() : null)
                .storeStandMall(user.getStoreStand() != null ? user.getStoreStand().getMallName() : null)
                .storeStandCity(user.getStoreStand() != null ? user.getStoreStand().getCity().name() : null)
                .build();
    }

    // ==================== STORE STAND MANAGEMENT ====================

    @Transactional(readOnly = true)
    public List<StoreStand> getAllStores() {
        return storeStandRepository.findAll();
    }

    @Transactional(readOnly = true)
    public StoreStand getStoreById(Long id) {
        return storeStandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + id));
    }

    @Transactional
    public StoreStand createStore(StoreStandRequest request) {
        StoreStand store = new StoreStand();
        store.setCity(request.getCity());
        store.setMallName(request.getMallName());
        store.setStorageCapacity(request.getStorageCapacity());

        StoreStand saved = storeStandRepository.save(store);
        log.info("Created store: {} in {} (capacity: {})",
                saved.getMallName(), saved.getCity(), saved.getStorageCapacity());
        return saved;
    }

    @Transactional
    public StoreStand updateStore(Long id, StoreStandRequest request) {
        StoreStand store = storeStandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + id));

        if (request.getCity() != null) {
            store.setCity(request.getCity());
        }
        if (request.getMallName() != null) {
            store.setMallName(request.getMallName());
        }
        if (request.getStorageCapacity() != null) {
            store.setStorageCapacity(request.getStorageCapacity());
        }

        StoreStand saved = storeStandRepository.save(store);
        log.info("Updated store id={}: {} in {}", id, saved.getMallName(), saved.getCity());
        return saved;
    }

    @Transactional
    public void deleteStore(Long id) {
        if (!storeStandRepository.existsById(id)) {
            throw new RuntimeException("Store not found with id: " + id);
        }
        storeStandRepository.deleteById(id);
        log.info("Deleted store id={}", id);
    }

    // ==================== PRODUCT MANAGEMENT ====================

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Transactional
    public Product createProduct(Product product) {
        String brand = product.getBrand().trim().toUpperCase();
        String model = product.getModel().trim().toUpperCase().replace(" ", "");
        String color = product.getColor().trim().toUpperCase();

        String generatedSku = String.format("%s-%s-%s-%s",
                product.getCategory(), brand, model, color);

        if (productRepository.existsBySku(generatedSku)) {
            throw new RuntimeException("Product with SKU '" + generatedSku + "' already exists!");
        }

        product.setSku(generatedSku);
        product.setBrand(brand);
        product.setModel(model);
        product.setColor(color);

        Product saved = productRepository.save(product);
        log.info("Created product: {} (SKU: {})", saved.getName(), saved.getSku());
        return saved;
    }

    @Transactional
    public Product updateProduct(Long id, Product productUpdate) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (productUpdate.getName() != null) {
            existing.setName(productUpdate.getName());
        }
        if (productUpdate.getPrice() != null) {
            existing.setPrice(productUpdate.getPrice());
        }
        if (productUpdate.getCategory() != null) {
            existing.setCategory(productUpdate.getCategory());
        }
        if (productUpdate.getBrand() != null) {
            existing.setBrand(productUpdate.getBrand().trim().toUpperCase());
        }
        if (productUpdate.getModel() != null) {
            existing.setModel(productUpdate.getModel().trim().toUpperCase().replace(" ", ""));
        }
        if (productUpdate.getColor() != null) {
            existing.setColor(productUpdate.getColor().trim().toUpperCase());
        }

        Product saved = productRepository.save(existing);
        log.info("Updated product id={}: {}", id, saved.getName());
        return saved;
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        log.info("Deleted product id={}", id);
    }

    // ==================== INVENTORY MANAGEMENT ====================

    @Transactional(readOnly = true)
    public List<InventoryItem> getAllInventory() {
        return inventoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> getInventoryByStore(Long storeId) {
        return inventoryRepository.findByStoreStandId(storeId);
    }

    @Transactional
    public InventoryItem addInventory(InventoryRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));

        StoreStand store = storeStandRepository.findById(request.getStoreStandId())
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + request.getStoreStandId()));

        InventoryItem item = new InventoryItem();
        item.setProduct(product);
        item.setStoreStand(store);
        item.setQuantity(request.getQuantity());
        item.setArrivalDate(LocalDate.now());

        InventoryItem saved = inventoryRepository.save(item);
        log.info("Added inventory: {} units of '{}' to store '{}'",
                request.getQuantity(), product.getName(), store.getMallName());
        return saved;
    }
}
