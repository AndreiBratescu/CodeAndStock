package com.stock.stock.service;

import com.stock.stock.domain.InventoryItem;
import com.stock.stock.repository.InventoryRepository;
import com.stock.stock.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsService {

    private final InventoryRepository inventoryRepository;
    private final SaleRepository saleRepository;

    public AnalyticsService(InventoryRepository inventoryRepository,
                            SaleRepository saleRepository) {
        this.inventoryRepository = inventoryRepository;
        this.saleRepository = saleRepository;
    }

    /**
     * Găsește toate item-urile de inventar care sunt în stoc de mai mult de {@code olderThanDays}.
     * Acestea sunt candidați buni pentru redistribuire sau lichidare.
     */
    public List<InventoryItem> findStaleInventory(int olderThanDays) {
        LocalDate limitDate = LocalDate.now().minusDays(olderThanDays);
        return inventoryRepository.findByArrivalDateBefore(limitDate);
    }

    /**
     * Calculează viteză medie de vânzare (bucăți/zi) pentru fiecare pereche (product, stand),
     * începând cu {@code sinceDate}.
     */
    public List<SalesVelocity> calculateSalesVelocity(LocalDate sinceDate) {
        long days = ChronoUnit.DAYS.between(sinceDate, LocalDate.now());
        if (days <= 0) {
            days = 1; // evităm împărțirea la zero
        }

        List<Object[]> rows = saleRepository.aggregateSalesByProductAndStand(sinceDate);
        List<SalesVelocity> result = new ArrayList<>();

        for (Object[] row : rows) {
            Long productId = (Long) row[0];
            Long standId = (Long) row[1];
            Number totalSoldNumber = (Number) row[2];
            int totalSold = totalSoldNumber.intValue();

            double dailyVelocity = totalSold / (double) days;
            result.add(new SalesVelocity(productId, standId, totalSold, dailyVelocity));
        }

        return result;
    }

    public record SalesVelocity(
            Long productId,
            Long standId,
            int totalSold,
            double dailyVelocity
    ) {
    }
}

