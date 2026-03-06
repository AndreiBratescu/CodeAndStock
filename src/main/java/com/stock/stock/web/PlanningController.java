package com.stock.stock.web;

import com.stock.stock.domain.InventoryItem;
import com.stock.stock.domain.Product;
import com.stock.stock.domain.StoreStand;
import com.stock.stock.planning.StockRedistributionSolution;
import com.stock.stock.planning.Transfer;
import com.stock.stock.repository.InventoryRepository;
import com.stock.stock.service.StockPlanningService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/planning")
public class PlanningController {

    private final StockPlanningService stockPlanningService;
    private final InventoryRepository inventoryRepository;

    public PlanningController(StockPlanningService stockPlanningService,
                              InventoryRepository inventoryRepository) {
        this.stockPlanningService = stockPlanningService;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Endpoint simplu care rulează solverul și întoarce toate transferurile recomandate
     * (cu cantitate > 0) într-un format ușor de afișat într-un UI.
     */
    @GetMapping("/redistribution")
    public List<TransferView> getRedistributionPlan(
            @RequestParam(name = "staleDays", defaultValue = "100") int staleDays,
            @RequestParam(name = "maxQtyPerTransfer", defaultValue = "50") int maxQtyPerTransfer
    ) {
        StockRedistributionSolution solution =
                stockPlanningService.generatePlan(staleDays, maxQtyPerTransfer);

        List<Transfer> transfers = stockPlanningService.extractNonZeroTransfers(solution);

        // Precalculăm un index pentru a găsi ușor InventoryItem (de unde putem lua daysInStock).
        Map<String, InventoryItem> inventoryIndex = buildInventoryIndex(solution.getInventoryItemList());

        return transfers.stream()
                .map(transfer -> toView(transfer, inventoryIndex))
                .collect(Collectors.toList());
    }

    private Map<String, InventoryItem> buildInventoryIndex(List<InventoryItem> inventoryItems) {
        Map<String, InventoryItem> index = new HashMap<>();
        if (inventoryItems == null) {
            return index;
        }
        for (InventoryItem item : inventoryItems) {
            if (item.getProduct() == null || item.getStoreStand() == null) {
                continue;
            }
            String key = item.getProduct().getId() + "-" + item.getStoreStand().getId();
            index.put(key, item);
        }
        return index;
    }

    private TransferView toView(Transfer transfer, Map<String, InventoryItem> inventoryIndex) {
        Product product = transfer.getProduct();
        StoreStand source = transfer.getSourceStand();
        StoreStand target = transfer.getTargetStand();

        String key = product.getId() + "-" + source.getId();
        InventoryItem sourceItem = inventoryIndex.get(key);
        Long daysInStock = sourceItem != null ? sourceItem.getDaysInStock() : null;

        return new TransferView(
                product != null ? product.getId() : null,
                product != null ? product.getSku() : null,
                product != null ? product.getName() : null,
                source != null ? source.getId() : null,
                source != null ? source.getCity() : null,
                source != null ? source.getMallName() : null,
                target != null ? target.getId() : null,
                target != null ? target.getCity() : null,
                target != null ? target.getMallName() : null,
                transfer.getQuantityToMove(),
                daysInStock
        );
    }

    public record TransferView(
            Long productId,
            String productSku,
            String productName,
            Long sourceStandId,
            Object sourceCity,
            String sourceMall,
            Long targetStandId,
            Object targetCity,
            String targetMall,
            Integer quantityToMove,
            Long daysInStockAtSource
    ) {
    }
}

