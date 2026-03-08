package com.stock.stock.web;

import com.stock.stock.service.DemoReorganizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final DemoReorganizationService demoReorganizationService;

    public DemoController(DemoReorganizationService demoReorganizationService) {
        this.demoReorganizationService = demoReorganizationService;
    }

    /**
     * Încarcă un scenariu de stoc care necesită reorganizare:
     * stoc vechi (&gt;100 zile) în unele standuri, puțin în altele (același oraș).
     * După apel, poți rula raportul de redistribuire și vei vedea transferuri recomandate.
     */
    @PostMapping("/load-reorganization-scenario")
    public ResponseEntity<Map<String, String>> loadReorganizationScenario() {
        demoReorganizationService.loadReorganizationScenario();
        return ResponseEntity.ok(Map.of(
                "message",
                "Scenariu încărcat. Deschide /redistribution.html și apasă «Generează raport» pentru a vedea Înainte / Transferuri / După."
        ));
    }
}
