package com.stock.stock.service;

import ai.timefold.solver.spring.boot.autoconfigure.manager.SolverManager;
import ai.timefold.solver.core.api.solver.SolverJob;
import com.stock.stock.domain.InventoryItem;
import com.stock.stock.domain.Product;
import com.stock.stock.domain.StoreStand;
import com.stock.stock.planning.StockRedistributionSolution;
import com.stock.stock.planning.Transfer;
import com.stock.stock.repository.InventoryRepository;
import com.stock.stock.repository.ProductRepository;
import com.stock.stock.repository.StoreStandRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Serviciu care construiește problema pentru Timefold și rulează solverul
 * pentru a obține un plan de redistribuire a stocului.
 */
@Service
public class StockPlanningService {

    private final ProductRepository productRepository;
    private final StoreStandRepository storeStandRepository;
    private final InventoryRepository inventoryRepository;
    private final AnalyticsService analyticsService;
    private final SolverManager<StockRedistributionSolution, Long> solverManager;

    public StockPlanningService(ProductRepository productRepository,
                                StoreStandRepository storeStandRepository,
                                InventoryRepository inventoryRepository,
                                AnalyticsService analyticsService,
                                SolverManager<StockRedistributionSolution, Long> solverManager) {
        this.productRepository = productRepository;
        this.storeStandRepository = storeStandRepository;
        this.inventoryRepository = inventoryRepository;
        this.analyticsService = analyticsService;
        this.solverManager = solverManager;
    }

    /**
     * Generează un plan de redistribuire, sincron (blokant),
     * pornind de la produsele cu stoc vechi.
     *
     * @param staleDays              pragul de vechime în zile (ex: 100).
     * @param maxQuantityPerTransfer cantitatea maximă mutată într-un singur transfer (ex: 50).
     */
    public StockRedistributionSolution generatePlan(int staleDays, int maxQuantityPerTransfer) {
        List<Product> products = productRepository.findAll();
        List<StoreStand> stands = storeStandRepository.findAll();
        List<InventoryItem> inventoryItems = inventoryRepository.findAll();

        // 1. Identificăm inventarul vechi (candidați pentru mutare).
        List<InventoryItem> staleInventory = analyticsService.findStaleInventory(staleDays);

        // 2. Construim toate combinațiile posibile de transfer
        //    din standurile cu stoc vechi către alte standuri din același oraș.
        List<Transfer> transfers = new ArrayList<>();
        for (InventoryItem sourceItem : staleInventory) {
            StoreStand sourceStand = sourceItem.getStoreStand();
            if (sourceStand == null || sourceStand.getCity() == null) {
                continue;
            }
            for (StoreStand targetStand : stands) {
                if (targetStand.getId().equals(sourceStand.getId())) {
                    continue; // nu are sens să mutăm în același stand
                }
                if (!sourceStand.getCity().equals(targetStand.getCity())) {
                    continue; // restricție MVP: doar în același oraș
                }
                transfers.add(new Transfer(sourceItem.getProduct(), sourceStand, targetStand));
            }
        }

        StockRedistributionSolution problem = new StockRedistributionSolution(
                products,
                stands,
                inventoryItems,
                transfers,
                maxQuantityPerTransfer
        );

        long problemId = System.currentTimeMillis();
        SolverJob<StockRedistributionSolution, Long> job =
                solverManager.solve(problemId, id -> problem);

        try {
            return job.getFinalBestSolution();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Job de planificare întrerupt.", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Eroare la rularea solverului Timefold.", e.getCause());
        }
    }

    /**
     * Conveniență: rulează planificarea cu valori implicite (100 zile, 50 buc/transfer).
     */
    public StockRedistributionSolution generateDefaultPlan() {
        return generatePlan(100, 50);
    }

    /**
     * Extrage doar transferurile cu cantitate > 0 dintr-o soluție.
     */
    public List<Transfer> extractNonZeroTransfers(StockRedistributionSolution solution) {
        List<Transfer> result = new ArrayList<>();
        if (solution == null || solution.getTransferList() == null) {
            return result;
        }
        for (Transfer transfer : solution.getTransferList()) {
            if (transfer.getQuantityToMove() != null && transfer.getQuantityToMove() > 0) {
                result.add(transfer);
            }
        }
        return result;
    }
}

