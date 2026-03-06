package com.stock.stock.service;

import com.stock.stock.domain.*;
import com.stock.stock.repository.AppUserRepository;
import com.stock.stock.repository.InventoryRepository;
import com.stock.stock.repository.SaleRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private SaleService saleService;

    private AppUser testUser;
    private StoreStand testStoreStand;
    private Product testProduct;
    private InventoryItem testInventory;
    private Sale testSale;

    @BeforeEach
    void setUp() {
        // Ensure a clean security context before each test
        SecurityContextHolder.clearContext();

        // Setup store stand
        testStoreStand = new StoreStand();
        testStoreStand.setId(1L);
        testStoreStand.setCity(City.BUCURESTI);
        testStoreStand.setMallName("Test Mall");

        // Setup user
        testUser = AppUser.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .roles("ROLE_EMPLOYEE")
                .enabled(true)
                .storeStand(testStoreStand)
                .build();

        // Setup product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setSku("TEST-PRODUCT-001");
        testProduct.setName("Test Product");
        testProduct.setPrice(29.99);

        // Setup inventory
        testInventory = new InventoryItem();
        testInventory.setId(1L);
        testInventory.setProduct(testProduct);
        testInventory.setStoreStand(testStoreStand);
        testInventory.setQuantity(100);
        testInventory.setArrivalDate(LocalDate.now().minusDays(10));

        // Setup sale
        testSale = new Sale();
        testSale.setProduct(testProduct);
        testSale.setStoreStand(testStoreStand);
        testSale.setQuantitySold(5);
    }

    @AfterEach
    void tearDown() {
        // Clear context after each test to prevent pollution
        SecurityContextHolder.clearContext();
    }

    /**
     * Fixes the "UnnecessaryStubbing" and "User not authenticated" issues.
     * Uses a concrete SecurityContextImpl and a real Token.
     */
    private void setupSecurityContext() {
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null, Collections.emptyList());
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    @Test
    void testCreateSale_Success() {
        setupSecurityContext();
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(inventoryRepository.findByStoreStandId(1L)).thenReturn(Arrays.asList(testInventory));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(testInventory);
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        Sale result = saleService.createSale(testSale);

        assertNotNull(result);
        assertEquals(testUser, result.getAppUser());
        assertNotNull(result.getSaleDate());

        // Verify inventory was decremented (100 - 5 = 95)
        verify(inventoryRepository).save(argThat(inv -> inv.getQuantity() == 95));
        verify(saleRepository).save(any(Sale.class));
    }

    @Test
    void testCreateSale_UserNotFound() {
        setupSecurityContext();
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> saleService.createSale(testSale));
    }

    @Test
    void testCreateSale_WrongStoreStand() {
        setupSecurityContext();
        StoreStand differentStand = new StoreStand();
        differentStand.setId(2L);
        testSale.setStoreStand(differentStand);

        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> saleService.createSale(testSale));
        assertEquals("User does not have permission to sell from this store stand", exception.getMessage());
    }

    @Test
    void testCreateSale_ProductNotInInventory() {
        setupSecurityContext();
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(inventoryRepository.findByStoreStandId(1L)).thenReturn(Collections.emptyList());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> saleService.createSale(testSale));
        assertEquals("Product not found in store stand inventory", exception.getMessage());
    }

    @Test
    void testCreateSale_InsufficientInventory() {
        setupSecurityContext();
        testInventory.setQuantity(3); // Less than requested 5
        testSale.setQuantitySold(5);

        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(inventoryRepository.findByStoreStandId(1L)).thenReturn(Arrays.asList(testInventory));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> saleService.createSale(testSale));
        assertTrue(exception.getMessage().contains("Insufficient inventory"));
    }

    @Test
    void testGetUserSales() {
        setupSecurityContext();
        Sale sale1 = new Sale();
        sale1.setId(1L);
        Sale sale2 = new Sale();
        sale2.setId(2L);
        List<Sale> expectedSales = Arrays.asList(sale1, sale2);

        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(saleRepository.findByAppUser(testUser)).thenReturn(expectedSales);

        List<Sale> result = saleService.getUserSales();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(saleRepository).findByAppUser(testUser);
    }

    @Test
    void testGetUserSalesByStand() {
        setupSecurityContext();
        Sale sale1 = new Sale();
        sale1.setId(1L);
        List<Sale> expectedSales = Collections.singletonList(sale1);

        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(saleRepository.findByAppUserAndStoreStand(testUser, testStoreStand)).thenReturn(expectedSales);

        List<Sale> result = saleService.getUserSalesByStand();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(saleRepository).findByAppUserAndStoreStand(testUser, testStoreStand);
    }

    @Test
    void testGetUserSalesByStand_NoStoreStand() {
        setupSecurityContext();
        testUser.setStoreStand(null);

        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> saleService.getUserSalesByStand());
        assertEquals("User has no assigned store stand", exception.getMessage());
    }
}