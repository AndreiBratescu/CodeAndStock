package com.stock.stock.service;

import com.stock.stock.domain.*;
import com.stock.stock.repository.AppUserRepository;
import com.stock.stock.repository.InventoryRepository;
import com.stock.stock.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private ProductService productService;

    private AppUser testUser;
    private StoreStand testStoreStand;
    private Product testProduct1;
    private Product testProduct2;
    private InventoryItem testInventory1;
    private InventoryItem testInventory2;

    @BeforeEach
    void setUp() {
        // Reset Security Context before each test to ensure a clean slate
        SecurityContextHolder.clearContext();

        testStoreStand = new StoreStand();
        testStoreStand.setId(1L);
        testStoreStand.setCity(City.BUCURESTI);

        testUser = AppUser.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .roles("ROLE_EMPLOYEE")
                .enabled(true)
                .storeStand(testStoreStand)
                .build();

        testProduct1 = new Product();
        testProduct1.setId(1L);
        testProduct1.setSku("PRODUCT-001");
        testProduct1.setName("Product 1");

        testProduct2 = new Product();
        testProduct2.setId(2L);
        testProduct2.setSku("PRODUCT-002");
        testProduct2.setName("Product 2");

        testInventory1 = new InventoryItem();
        testInventory1.setId(1L);
        testInventory1.setProduct(testProduct1);
        testInventory1.setStoreStand(testStoreStand);
        testInventory1.setQuantity(50);
        testInventory1.setArrivalDate(LocalDate.now());

        testInventory2 = new InventoryItem();
        testInventory2.setId(2L);
        testInventory2.setProduct(testProduct2);
        testInventory2.setStoreStand(testStoreStand);
        testInventory2.setQuantity(75);
        testInventory2.setArrivalDate(LocalDate.now());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setupSecurityContext() {
        // Create a real Authentication object instead of a mock to ensure getName() works
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null, Collections.emptyList());

        // Use the actual Implementation of SecurityContext
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    @Test
    void testCreateProduct_Success() {
        setupSecurityContext();
        Product newProduct = new Product();
        newProduct.setBrand("Apple");
        newProduct.setModel("iPhone 15");
        newProduct.setColor("Red");
        newProduct.setCategory(ProductCategory.CASE);

        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(newProduct);

        Product result = productService.createProduct(newProduct);

        assertNotNull(result);
        assertEquals("APPLE", result.getBrand());
        assertEquals("IPHONE15", result.getModel());
    }

    @Test
    void testCreateProduct_DuplicateSku() {
        setupSecurityContext();
        Product newProduct = new Product();
        newProduct.setBrand("Apple");
        newProduct.setModel("iPhone 15");
        newProduct.setColor("Red");
        newProduct.setCategory(ProductCategory.CASE);

        when(productRepository.existsBySku(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> productService.createProduct(newProduct));
    }

    @Test
    void testGetProductsByUserStand_Success() {
        setupSecurityContext();
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(inventoryRepository.findByStoreStandId(1L))
                .thenReturn(Arrays.asList(testInventory1, testInventory2));

        List<Product> result = productService.getProductsByUserStand();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testProduct1));
    }

    @Test
    void testGetProductsByUserStand_UserNotFound() {
        setupSecurityContext();
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.getProductsByUserStand());
    }

    @Test
    void testGetProductsByUserStand_NoStoreStand() {
        setupSecurityContext();
        testUser.setStoreStand(null);
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.getProductsByUserStand());
        assertEquals("User has no assigned store stand", exception.getMessage());
    }

    @Test
    void testGetInventoryByUserStand_Success() {
        setupSecurityContext();
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(inventoryRepository.findByStoreStandId(1L))
                .thenReturn(Arrays.asList(testInventory1, testInventory2));

        List<InventoryItem> result = productService.getInventoryByUserStand();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetInventoryByUserStand_EmptyInventory() {
        setupSecurityContext();
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(inventoryRepository.findByStoreStandId(1L)).thenReturn(Collections.emptyList());

        List<InventoryItem> result = productService.getInventoryByUserStand();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetProductsByUserStand_DuplicateProductsInInventory() {
        setupSecurityContext();
        InventoryItem testInventory3 = new InventoryItem();
        testInventory3.setId(3L);
        testInventory3.setProduct(testProduct1);
        testInventory3.setStoreStand(testStoreStand);
        testInventory3.setQuantity(25);

        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(inventoryRepository.findByStoreStandId(1L))
                .thenReturn(Arrays.asList(testInventory1, testInventory2, testInventory3));

        List<Product> result = productService.getProductsByUserStand();

        assertNotNull(result);
        assertEquals(2, result.size());
    }
}