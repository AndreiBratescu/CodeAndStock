package com.stock.stock.service;

import com.stock.stock.domain.Product;
import com.stock.stock.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product createProduct(Product product) {
        String brand = product.getBrand().trim().toUpperCase();
        String model = product.getModel().trim().toUpperCase().replace(" ", "");
        String color = product.getColor().trim().toUpperCase();

        // 2. Generare SKU automată: CASE-IPHONE-15-RED
        String generatedSku = String.format("%s-%s-%s-%s",
                product.getCategory(), brand, model, color);

        if (productRepository.existsBySku(generatedSku)) {
            throw new RuntimeException("Acest produs (SKU: " + generatedSku + ") există deja în sistem!");
        }

        product.setSku(generatedSku);
        product.setBrand(brand);
        product.setModel(model);
        product.setColor(color);

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product updated) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produsul cu id " + id + " nu există."));

        String brand = updated.getBrand().trim().toUpperCase();
        String model = updated.getModel().trim().toUpperCase().replace(" ", "");
        String color = updated.getColor().trim().toUpperCase();

        String generatedSku = String.format("%s-%s-%s-%s",
                updated.getCategory(), brand, model, color);

        productRepository.findBySku(generatedSku)
                .filter(p -> !p.getId().equals(id))
                .ifPresent(p -> {
                    throw new RuntimeException("Alt produs cu același SKU (" + generatedSku + ") există deja.");
                });

        existing.setCategory(updated.getCategory());
        existing.setName(updated.getName());
        existing.setBrand(brand);
        existing.setModel(model);
        existing.setColor(color);
        existing.setPrice(updated.getPrice());
        existing.setSku(generatedSku);

        return productRepository.save(existing);
    }
}
