package com.stock.stock.planning;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.CountableValueRange;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeFactory;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.stock.stock.domain.InventoryItem;
import com.stock.stock.domain.Product;
import com.stock.stock.domain.StoreStand;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Rădăcina modelului de planificare pentru Timefold.
 * Conține facts (produse, standuri, inventar) și entități de planificare (transferuri),
 * plus scorul rezultat.
 */
@PlanningSolution
@NoArgsConstructor
public class StockRedistributionSolution {

    @ProblemFactCollectionProperty
    private List<Product> productList;

    @ProblemFactCollectionProperty
    private List<StoreStand> storeStandList;

    @ProblemFactCollectionProperty
    private List<InventoryItem> inventoryItemList;

    @PlanningEntityCollectionProperty
    private List<Transfer> transferList;

    private int maxQuantityPerTransfer;

    @PlanningScore
    private HardSoftScore score;

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }

    public List<StoreStand> getStoreStandList() {
        return storeStandList;
    }

    public void setStoreStandList(List<StoreStand> storeStandList) {
        this.storeStandList = storeStandList;
    }

    public List<InventoryItem> getInventoryItemList() {
        return inventoryItemList;
    }

    public void setInventoryItemList(List<InventoryItem> inventoryItemList) {
        this.inventoryItemList = inventoryItemList;
    }

    public List<Transfer> getTransferList() {
        return transferList;
    }

    public void setTransferList(List<Transfer> transferList) {
        this.transferList = transferList;
    }

    public int getMaxQuantityPerTransfer() {
        return maxQuantityPerTransfer;
    }

    public void setMaxQuantityPerTransfer(int maxQuantityPerTransfer) {
        this.maxQuantityPerTransfer = maxQuantityPerTransfer;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public StockRedistributionSolution(List<Product> productList,
                                       List<StoreStand> storeStandList,
                                       List<InventoryItem> inventoryItemList,
                                       List<Transfer> transferList,
                                       int maxQuantityPerTransfer) {
        this.productList = productList;
        this.storeStandList = storeStandList;
        this.inventoryItemList = inventoryItemList;
        this.transferList = transferList;
        this.maxQuantityPerTransfer = maxQuantityPerTransfer;
    }

    /**
     * Domeniul de valori pentru {@link Transfer#quantityToMove}.
     * Definim 0..maxQuantityPerTransfer, inclusiv.
     */
    @ValueRangeProvider(id = "quantityRange")
    public CountableValueRange<Integer> getQuantityRange() {
        // ValueRangeFactory folosește capăt superior exclusiv,
        // deci adăugăm 1 pentru a include maxQuantityPerTransfer.
        return ValueRangeFactory.createIntValueRange(0, maxQuantityPerTransfer + 1);
    }
}

