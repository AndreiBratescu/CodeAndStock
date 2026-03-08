package com.stock.stock.config;

import com.stock.stock.domain.*;
import com.stock.stock.repository.InventoryRepository;
import com.stock.stock.repository.ProductRepository;
import com.stock.stock.repository.StoreStandRepository;
import com.stock.stock.service.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Încarcă la pornire produse de exemplu și stoc în mai multe standuri.
 * Rulează doar dacă nu există încă produse în DB (evită duplicate la restart).
 */
@Component
@Profile("!test")
public class SampleDataLoader implements CommandLineRunner {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final StoreStandRepository storeStandRepository;
    private final InventoryRepository inventoryRepository;

    public SampleDataLoader(ProductService productService,
                           ProductRepository productRepository,
                           StoreStandRepository storeStandRepository,
                           InventoryRepository inventoryRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.storeStandRepository = storeStandRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return; // date deja existente
        }
        loadStands();
        loadProductsAndInventory();
    }

    private void loadStands() {
        if (storeStandRepository.count() > 0) return;

        List<StoreStand> stands = List.of(
                createStand(City.BUCURESTI, "Mega Mall", 50),
                createStand(City.BUCURESTI, "AFI Palace", 40),
                createStand(City.CLUJ_NAPOCA, "Iulius Mall", 45),
                createStand(City.TIMISOARA, "Shopping City", 35),
                createStand(City.IASI, "Palas Mall", 40)
        );
        stands.forEach(storeStandRepository::save);
    }

    private StoreStand createStand(City city, String mallName, int capacity) {
        StoreStand s = new StoreStand();
        s.setCity(city);
        s.setMallName(mallName);
        s.setStorageCapacity(capacity);
        return s;
    }

    private void loadProductsAndInventory() {
        Product p1 = saveProduct(ProductCategory.CASE, "Husă silicon iPhone 15", "Apple", "iPhone 15", "Midnight", 89.99);
        Product p2 = saveProduct(ProductCategory.CASE, "Husă MagSafe iPhone 15 Pro", "Apple", "iPhone 15 Pro", "Blue", 149.99);
        Product p3 = saveProduct(ProductCategory.SCREEN_PROTECTOR, "Sticlă protectoare Samsung S24", "Samsung", "Galaxy S24", "Transparent", 59.99);
        Product p4 = saveProduct(ProductCategory.CHARGER, "Încărcător rapid 25W", "Samsung", "25W", "White", 79.99);
        Product p5 = saveProduct(ProductCategory.CABLE, "Cablu USB-C to Lightning 1m", "Apple", "USB-C Lightning", "White", 129.99);
        Product p6 = saveProduct(ProductCategory.AUDIO, "Căști wireless Bluetooth", "JBL", "Tune 520BT", "Black", 199.99);
        Product p7 = saveProduct(ProductCategory.GADGET, "Suport auto ventuză", "Baseus", "Car Mount", "Black", 49.99);
        Product p8 = saveProduct(ProductCategory.CASE, "Husă bumper Xiaomi 14", "Xiaomi", "14", "Blue", 69.99);

        List<StoreStand> stands = storeStandRepository.findAll();
        if (stands.size() < 2) return;

        LocalDate oldDate = LocalDate.now().minusDays(120);
        LocalDate recentDate = LocalDate.now().minusDays(15);

        addInventory(p1, stands.get(0), 25, oldDate);
        addInventory(p1, stands.get(1), 10, recentDate);
        addInventory(p2, stands.get(0), 15, recentDate);
        addInventory(p2, stands.get(2), 30, oldDate);
        addInventory(p3, stands.get(1), 40, oldDate);
        addInventory(p3, stands.get(3), 20, recentDate);
        addInventory(p4, stands.get(0), 18, recentDate);
        addInventory(p4, stands.get(2), 12, oldDate);
        addInventory(p5, stands.get(1), 35, oldDate);
        addInventory(p5, stands.get(4), 8, recentDate);
        addInventory(p6, stands.get(2), 22, recentDate);
        addInventory(p6, stands.get(3), 14, oldDate);
        addInventory(p7, stands.get(0), 50, oldDate);
        addInventory(p7, stands.get(4), 5, recentDate);
        addInventory(p8, stands.get(3), 28, recentDate);
        addInventory(p8, stands.get(1), 11, oldDate);
    }

    private Product saveProduct(ProductCategory category, String name, String brand, String model, String color, double price) {
        Product p = new Product();
        p.setCategory(category);
        p.setName(name);
        p.setBrand(brand);
        p.setModel(model);
        p.setColor(color);
        p.setPrice(price);
        return productService.createProduct(p);
    }

    private void addInventory(Product product, StoreStand stand, int quantity, LocalDate arrivalDate) {
        InventoryItem item = new InventoryItem();
        item.setProduct(product);
        item.setStoreStand(stand);
        item.setQuantity(quantity);
        item.setArrivalDate(arrivalDate);
        inventoryRepository.save(item);
    }
}
