package com.stock.stock.planning;

import com.stock.stock.domain.Product;
import com.stock.stock.domain.StoreStand;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

/**
 * Reprezintă o decizie potențială de transfer de stoc
 * pentru un anumit produs, dintr-un stand sursă
 * către un stand destinație.
 */
@PlanningEntity
public class Transfer {

    private Product product;
    private StoreStand sourceStand;
    private StoreStand targetStand;

    @PlanningVariable(valueRangeProviderRefs = "quantityRange")
    private Integer quantityToMove;

    /** Constructor fără argumente cerut de Timefold pentru clonare. */
    public Transfer() {
    }

    public Transfer(Product product, StoreStand sourceStand, StoreStand targetStand) {
        this.product = product;
        this.sourceStand = sourceStand;
        this.targetStand = targetStand;
        this.quantityToMove = 0;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public StoreStand getSourceStand() {
        return sourceStand;
    }

    public void setSourceStand(StoreStand sourceStand) {
        this.sourceStand = sourceStand;
    }

    public StoreStand getTargetStand() {
        return targetStand;
    }

    public void setTargetStand(StoreStand targetStand) {
        this.targetStand = targetStand;
    }

    public Integer getQuantityToMove() {
        return quantityToMove;
    }

    public void setQuantityToMove(Integer quantityToMove) {
        this.quantityToMove = quantityToMove;
    }
}

