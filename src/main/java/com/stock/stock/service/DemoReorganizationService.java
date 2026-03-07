package com.stock.stock.service;

import com.stock.stock.domain.*;
import com.stock.stock.repository.InventoryRepository;
import com.stock.stock.repository.ProductRepository;
import com.stock.stock.repository.StoreStandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Încarcă un scenariu de stoc care necesită clar reorganizare:
 * stoc vechi (&gt;100 zile) într-un stand, puțin sau zero în alt stand din același oraș.
 */
@Service
public class DemoReorganizationService {

    private static final int STALE_DAYS = 110;
    private static final int RECENT_DAYS = 10;

    private final ProductRepository productRepository;
    private final StoreStandRepository storeStandRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductService productService;

    public DemoReorganizationService(ProductRepository productRepository,
                                     StoreStandRepository storeStandRepository,
                                     InventoryRepository inventoryRepository,
                                     ProductService productService) {
        this.productRepository = productRepository;
        this.storeStandRepository = storeStandRepository;
        this.inventoryRepository = inventoryRepository;
        this.productService = productService;
    }

    /**
     * Șterge tot inventarul și încarcă un scenariu care necesită reorganizare.
     * Asigură 2 standuri în București și 2 în Cluj, produse demo, și stoc „blocat” în unele standuri.
     */
    @Transactional
    public void loadReorganizationScenario() {
        inventoryRepository.deleteAll();

        List<StoreStand> bucuresti = storeStandRepository.findByCity(City.BUCURESTI);
        List<StoreStand> cluj = storeStandRepository.findByCity(City.CLUJ_NAPOCA);

        if (bucuresti.size() < 2) {
            addStandIfMissing(City.BUCURESTI, "Mega Mall", 50);
            addStandIfMissing(City.BUCURESTI, "AFI Palace", 40);
            bucuresti = storeStandRepository.findByCity(City.BUCURESTI);
        }
        if (cluj.size() < 2) {
            addStandIfMissing(City.CLUJ_NAPOCA, "Iulius Mall", 45);
            addStandIfMissing(City.CLUJ_NAPOCA, "Vivo Cluj", 40);
            cluj = storeStandRepository.findByCity(City.CLUJ_NAPOCA);
        }

        Product p1 = getOrCreateProduct("Husă demo reorganizare A", "REORG", "DEMO-A", "Albastru", 49.99);
        Product p2 = getOrCreateProduct("Husă demo reorganizare B", "REORG", "DEMO-B", "Negru", 59.99);
        Product p3 = getOrCreateProduct("Încărcător demo reorganizare", "REORG", "DEMO-C", "Alb", 79.99);

        LocalDate staleDate = LocalDate.now().minusDays(STALE_DAYS);
        LocalDate recentDate = LocalDate.now().minusDays(RECENT_DAYS);

        // București: stand 1 = mult stoc vechi, stand 2 = puțin stoc recent → mutăm din 1 în 2
        if (bucuresti.size() >= 2) {
            addInventory(p1, bucuresti.get(0), 70, staleDate);
            addInventory(p1, bucuresti.get(1), 2, recentDate);
            addInventory(p2, bucuresti.get(0), 45, staleDate);
            addInventory(p2, bucuresti.get(1), 0, recentDate);
            addInventory(p3, bucuresti.get(0), 50, staleDate);
            addInventory(p3, bucuresti.get(1), 3, recentDate);
        }

        // Cluj: stand 1 = mult stoc vechi, stand 2 = puțin → mutăm din 1 în 2
        if (cluj.size() >= 2) {
            addInventory(p1, cluj.get(0), 40, staleDate);
            addInventory(p1, cluj.get(1), 5, recentDate);
            addInventory(p2, cluj.get(0), 35, staleDate);
            addInventory(p2, cluj.get(1), 0, recentDate);
        }
    }

    private void addStandIfMissing(City city, String mallName, int capacity) {
        List<StoreStand> list = storeStandRepository.findByCity(city);
        boolean exists = list.stream().anyMatch(s -> mallName.equals(s.getMallName()));
        if (!exists) {
            StoreStand s = new StoreStand();
            s.setCity(city);
            s.setMallName(mallName);
            s.setStorageCapacity(capacity);
            storeStandRepository.save(s);
        }
    }

    private Product getOrCreateProduct(String name, String brand, String model, String color, double price) {
        String sku = ProductCategory.CASE + "-" + brand.toUpperCase() + "-" + model.toUpperCase().replace(" ", "") + "-" + color.toUpperCase();
        if (model.contains("C")) {
            sku = ProductCategory.CHARGER + "-" + brand.toUpperCase() + "-" + model.toUpperCase().replace(" ", "") + "-" + color.toUpperCase();
        }
        Optional<Product> existing = productRepository.findBySku(sku);
        if (existing.isPresent()) {
            return existing.get();
        }
        Product p = new Product();
        p.setCategory(model.contains("C") ? ProductCategory.CHARGER : ProductCategory.CASE);
        p.setName(name);
        p.setBrand(brand);
        p.setModel(model);
        p.setColor(color);
        p.setPrice(price);
        return productService.createProduct(p);
    }

    private void addInventory(Product product, StoreStand stand, int quantity, LocalDate arrivalDate) {
        if (quantity <= 0) return;
        InventoryItem item = new InventoryItem();
        item.setProduct(product);
        item.setStoreStand(stand);
        item.setQuantity(quantity);
        item.setArrivalDate(arrivalDate);
        inventoryRepository.save(item);
    }
}
