package com.stock.stock.planning;

import com.stock.stock.domain.Product;
import com.stock.stock.domain.StoreStand;
import lombok.Data;
import lombok.NoArgsConstructor;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

/**
 * Reprezintă o decizie potențială de transfer de stoc
 * pentru un anumit produs, dintr-un stand sursă
 * către un stand destinație.
 */
@PlanningEntity
@Data
@NoArgsConstructor
public class Transfer {

    /**
     * Produsul care urmează să fie mutat.
     * Este un fact de problemă, nu o variabilă de planificare.
     */
    private Product product;

    /**
     * Standul din care pleacă marfa.
     */
    private StoreStand sourceStand;

    /**
     * Standul în care ajunge marfa.
     */
    private StoreStand targetStand;

    /**
     * Cantitatea pe care solverul o decide pentru acest transfer.
     * Domeniul de valori este definit la nivel de soluție (0..maxPerTransfer).
     */
    @PlanningVariable(valueRangeProviderRefs = "quantityRange")
    private Integer quantityToMove;

    public Transfer(Product product, StoreStand sourceStand, StoreStand targetStand) {
        this.product = product;
        this.sourceStand = sourceStand;
        this.targetStand = targetStand;
        this.quantityToMove = 0;
    }
}

