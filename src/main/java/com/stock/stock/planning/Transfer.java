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

    private Product product;
    private StoreStand sourceStand;
    private StoreStand targetStand;

    @PlanningVariable(valueRangeProviderRefs = "quantityRange")
    private Integer quantityToMove;

    public Transfer(Product product, StoreStand sourceStand, StoreStand targetStand) {
        this.product = product;
        this.sourceStand = sourceStand;
        this.targetStand = targetStand;
        this.quantityToMove = 0;
    }

}

