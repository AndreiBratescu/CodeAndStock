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
}
