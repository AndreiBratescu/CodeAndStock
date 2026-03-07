package com.stock.stock.planning;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import com.stock.stock.domain.InventoryItem;
import com.stock.stock.domain.StoreStand;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;

/**
 * Definim regulile de business pentru redistribuirea stocului.
 */
public class StockConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                transferOnlyWithinSameCity(constraintFactory),
                sourceStockMustNotBeExceeded(constraintFactory),
                preferMovingOldStock(constraintFactory)
        };
    }

    /**
     * Nu permitem transferuri între orașe diferite (pentru MVP).
     */
    private Constraint transferOnlyWithinSameCity(ConstraintFactory factory) {
        return factory.forEach(Transfer.class)
                .filter(transfer ->
                        transfer.getQuantityToMove() != null
                                && transfer.getQuantityToMove() > 0
                                && transfer.getSourceStand() != null
                                && transfer.getTargetStand() != null
                                && transfer.getSourceStand().getCity() != null
                                && transfer.getTargetStand().getCity() != null
                                && !transfer.getSourceStand().getCity().equals(transfer.getTargetStand().getCity())
                )
                .penalize(HardSoftScore.ONE_HARD,
                        Transfer::getQuantityToMove)
                .asConstraint("Transfer doar în același oraș");
    }

    /**
     * Nu permitem să mutăm mai mult stoc decât există în standul sursă
     * pentru un anumit produs.
     */
    private Constraint sourceStockMustNotBeExceeded(ConstraintFactory factory) {
        return factory.forEach(Transfer.class)
                .filter(transfer ->
                        transfer.getQuantityToMove() != null
                                && transfer.getQuantityToMove() > 0
                                && transfer.getProduct() != null
                                && transfer.getSourceStand() != null
                )
                .join(InventoryItem.class,
                        equal(Transfer::getProduct, InventoryItem::getProduct),
                        equal(Transfer::getSourceStand, InventoryItem::getStoreStand))
                .groupBy(
                        (transfer, inventoryItem) -> inventoryItem,
                        (transfer, inventoryItem) -> inventoryItem.getQuantity(),
                        ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sum(
                                (Transfer t, InventoryItem ignored) -> t.getQuantityToMove()
                        )
                )
                .filter((inventoryItem, availableQuantity, movedQuantity) ->
                        movedQuantity > availableQuantity
                )
                .penalize(HardSoftScore.ONE_HARD,
                        (inventoryItem, availableQuantity, movedQuantity) ->
                                movedQuantity - availableQuantity
                )
                .asConstraint("Nu se depășește stocul sursă");
    }

    /**
     * Încurajăm mutarea produselor care stau de mult timp în stoc
     * în standul sursă (de ex. > 100 zile).
     */
    private Constraint preferMovingOldStock(ConstraintFactory factory) {
        final long STALE_DAYS_THRESHOLD = 100;

        return factory.forEach(Transfer.class)
                .filter(transfer ->
                        transfer.getQuantityToMove() != null
                                && transfer.getQuantityToMove() > 0
                                && transfer.getProduct() != null
                                && transfer.getSourceStand() != null
                )
                .join(InventoryItem.class,
                        equal(Transfer::getProduct, InventoryItem::getProduct),
                        equal(Transfer::getSourceStand, InventoryItem::getStoreStand))
                .filter((transfer, inventoryItem) ->
                        inventoryItem.getArrivalDate() != null
                                && inventoryItem.getDaysInStock() >= STALE_DAYS_THRESHOLD
                )
                .reward(HardSoftScore.ONE_SOFT,
                        (transfer, inventoryItem) -> transfer.getQuantityToMove())
                .asConstraint("Preferă mutarea stocului vechi");
    }
}

