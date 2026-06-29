package com.blockchain.vies;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class VatController {

    private final ViesService viesService;

    public VatController(ViesService viesService) {
        this.viesService = viesService;
    }

    /**
     * POST /api/verify-vat
     * Body: { "vatId": "DE123456789" }
     */
    @PostMapping("/verify-vat")
    public ResponseEntity<VatResponse> verifyVat(@RequestBody VatRequest request) {
        VatResponse response = viesService.verify(request.getVatId());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/health
     * Schneller Health-Check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"UP\",\"service\":\"vies-backend\"}");
    }
}
