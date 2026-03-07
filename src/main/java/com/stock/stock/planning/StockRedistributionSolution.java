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
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Rădăcina modelului de planificare pentru Timefold.
 * Conține facts (produse, standuri, inventar) și entități de planificare (transferuri),
 * plus scorul rezultat.
 */
@PlanningSolution
@Data
@NoArgsConstructor
public class StockRedistributionSolution {

    /**
     * Listele de facts, citite din baza de date.
     */

    @ProblemFactCollectionProperty
    private List<Product> productList;

    @ProblemFactCollectionProperty
    private List<StoreStand> storeStandList;

    @ProblemFactCollectionProperty
    private List<InventoryItem> inventoryItemList;

    /**
     * Entitățile de planificare pe care solverul le modifică.
     */

    @PlanningEntityCollectionProperty
    private List<Transfer> transferList;

    /**
     * Parametru de configurare pentru domeniul de valori al cantității mutate.
     * De exemplu, 50 înseamnă că un singur transfer poate avea între 0 și 50 bucăți.
     */

    private int maxQuantityPerTransfer;

    /**
     * Score-ul multi-nivel (hard/soft) calculat de Timefold.
     */

    @PlanningScore
    private HardSoftScore score;

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

    @ValueRangeProvider(id = "quantityRange")
    public CountableValueRange<Integer> getQuantityRange() {
        // ValueRangeFactory folosește capăt superior exclusiv,
        // deci adăugăm 1 pentru a include maxQuantityPerTransfer.
        return ValueRangeFactory.createIntValueRange(0, maxQuantityPerTransfer + 1);
    }
}

