package com.stock.stock.web;

import com.stock.stock.domain.InventoryItem;
import com.stock.stock.domain.Product;
import com.stock.stock.domain.StoreStand;
import com.stock.stock.planning.StockRedistributionSolution;
import com.stock.stock.planning.Transfer;
import com.stock.stock.repository.InventoryRepository;
import com.stock.stock.service.StockPlanningService;
import com.stock.stock.web.dto.RedistributionReportDto;
import com.stock.stock.web.dto.StockAtLocationDto;
import com.stock.stock.web.dto.TransferWithExplanationDto;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/planning")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:8080"})
public class PlanningController {

    private final StockPlanningService stockPlanningService;
    private final InventoryRepository inventoryRepository;

    public PlanningController(StockPlanningService stockPlanningService,
                              InventoryRepository inventoryRepository) {
        this.stockPlanningService = stockPlanningService;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Raport complet: stoc ÎNAINTE, transferuri cu explicații, stoc DUPĂ.
     * Ideal pentru afișare clară a redistribuirii.
     */
    @GetMapping("/redistribution-report")
    public RedistributionReportDto getRedistributionReport(
            @RequestParam(name = "staleDays", defaultValue = "100") int staleDays,
            @RequestParam(name = "maxQtyPerTransfer", defaultValue = "50") int maxQtyPerTransfer
    ) {
        List<InventoryItem> allInventory = inventoryRepository.findAll();
        List<StockAtLocationDto> before = toStockAtLocationList(allInventory);

        StockRedistributionSolution solution =
                stockPlanningService.generatePlan(staleDays, maxQtyPerTransfer);
        List<Transfer> transfers = stockPlanningService.extractNonZeroTransfers(solution);
        Map<String, InventoryItem> inventoryIndex = buildInventoryIndex(solution.getInventoryItemList() != null
                ? solution.getInventoryItemList() : allInventory);

        List<TransferWithExplanationDto> transfersWithExplanation = transfers.stream()
                .map(t -> toTransferWithExplanation(t, inventoryIndex))
                .collect(Collectors.toList());

        List<StockAtLocationDto> after = computeAfterState(before, transfersWithExplanation);

        String summary = buildSummaryExplanation(before, transfersWithExplanation, after);

        return new RedistributionReportDto(summary, before, transfersWithExplanation, after);
    }

    private List<StockAtLocationDto> toStockAtLocationList(List<InventoryItem> items) {
        if (items == null) return List.of();
        List<StockAtLocationDto> list = new ArrayList<>();
        for (InventoryItem item : items) {
            Product p = item.getProduct();
            StoreStand s = item.getStoreStand();
            if (p == null || s == null) continue;
            list.add(new StockAtLocationDto(
                    p.getId(),
                    p.getSku(),
                    p.getName(),
                    s.getId(),
                    s.getCity() != null ? s.getCity().name() : "",
                    s.getMallName() != null ? s.getMallName() : "",
                    item.getQuantity() != null ? item.getQuantity() : 0,
                    item.getDaysInStock()
            ));
        }
        return list;
    }

    private TransferWithExplanationDto toTransferWithExplanation(Transfer t, Map<String, InventoryItem> inventoryIndex) {
        Product p = t.getProduct();
        StoreStand src = t.getSourceStand();
        StoreStand tgt = t.getTargetStand();
        String key = p.getId() + "-" + src.getId();
        InventoryItem srcItem = inventoryIndex.get(key);
        long days = srcItem != null ? srcItem.getDaysInStock() : 0;
        int qty = t.getQuantityToMove() != null ? t.getQuantityToMove() : 0;

        String explanation = String.format(
                "Stoc în sursă de %d zile, neînvândut. Mutăm %d buc către standul din %s (%s) pentru a echilibra stocul.",
                days, qty,
                tgt != null ? tgt.getMallName() : "?",
                tgt != null && tgt.getCity() != null ? tgt.getCity().name() : "?"
        );

        return new TransferWithExplanationDto(
                p != null ? p.getId() : null,
                p != null ? p.getSku() : null,
                p != null ? p.getName() : null,
                src != null ? src.getId() : null,
                src != null && src.getCity() != null ? src.getCity().name() : "",
                src != null ? src.getMallName() : "",
                tgt != null ? tgt.getId() : null,
                tgt != null && tgt.getCity() != null ? tgt.getCity().name() : "",
                tgt != null ? tgt.getMallName() : "",
                qty,
                days,
                explanation
        );
    }

    private List<StockAtLocationDto> computeAfterState(List<StockAtLocationDto> before,
                                                      List<TransferWithExplanationDto> transfers) {
        Map<String, Integer> qtyByKey = new HashMap<>();
        Map<String, StockAtLocationDto> infoByKey = new HashMap<>();
        for (StockAtLocationDto s : before) {
            String key = s.productId() + "-" + s.standId();
            qtyByKey.put(key, qtyByKey.getOrDefault(key, 0) + s.quantity());
            infoByKey.put(key, s);
        }
        for (TransferWithExplanationDto t : transfers) {
            String srcKey = t.productId() + "-" + t.sourceStandId();
            String tgtKey = t.productId() + "-" + t.targetStandId();
            qtyByKey.put(srcKey, qtyByKey.getOrDefault(srcKey, 0) - t.quantityToMove());
            qtyByKey.put(tgtKey, qtyByKey.getOrDefault(tgtKey, 0) + t.quantityToMove());
            if (!infoByKey.containsKey(tgtKey)) {
                infoByKey.put(tgtKey, new StockAtLocationDto(
                        t.productId(), t.productSku(), t.productName(),
                        t.targetStandId(), t.targetCity(), t.targetMall(), 0, 0L));
            }
        }

        List<StockAtLocationDto> after = new ArrayList<>();
        for (Map.Entry<String, Integer> e : qtyByKey.entrySet()) {
            int newQty = Math.max(0, e.getValue());
            if (newQty == 0) continue;
            StockAtLocationDto info = infoByKey.get(e.getKey());
            if (info == null) continue;
            after.add(new StockAtLocationDto(
                    info.productId(), info.productSku(), info.productName(),
                    info.standId(), info.city(), info.mallName(),
                    newQty, info.daysInStock()
            ));
        }
        return after;
    }

    private String buildSummaryExplanation(List<StockAtLocationDto> before,
                                          List<TransferWithExplanationDto> transfers,
                                          List<StockAtLocationDto> after) {
        if (transfers.isEmpty()) {
            return "Nu există transferuri recomandate. Stocul curent nu are produse blocate (peste 100 zile în același stand) sau nu există standuri alternative în același oraș.";
        }
        int totalMoved = transfers.stream().mapToInt(TransferWithExplanationDto::quantityToMove).sum();
        return String.format(
                "Redistribuire recomandată: %d transferuri, în total %d bucăți mutate din standuri cu stoc vechi (nevândut) către standuri din același oraș.",
                transfers.size(), totalMoved
        );
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

