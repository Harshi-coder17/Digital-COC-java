package com.chainguard.controller;

import com.chainguard.dto.TransferCustodyRequest;
import com.chainguard.model.AuditViewRecord;
import com.chainguard.model.CourtViewRecord;
import com.chainguard.model.CustodyLogEntry;
import com.chainguard.service.CustodyService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/custody")
public class CustodyController {

    private final CustodyService custodyService;

    public CustodyController(CustodyService custodyService) {
        this.custodyService = custodyService;
    }

    @GetMapping("/evidence/{id}")
    public List<CustodyLogEntry> getCustodyHistory(@PathVariable int id) {
        return custodyService.getCustodyHistory(id);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    public Map<String, String> transferCustody(@Valid @RequestBody TransferCustodyRequest request) {
        custodyService.transferCustody(request);
        return Map.of("message", "Custody transferred successfully.");
    }

    @DeleteMapping("/{logId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> deleteCustodyLog(@PathVariable int logId) {
        custodyService.deleteCustodyLog(logId);
        return Map.of("message", "Deleted.");
    }

    @GetMapping("/court-view")
    @PreAuthorize("hasAnyRole('ADMIN', 'JUDICIAL')")
    public List<CourtViewRecord> getCourtView() {
        return custodyService.getCourtView();
    }

    @GetMapping("/audit-view")
    @PreAuthorize("hasAnyRole('ADMIN', 'JUDICIAL')")
    public List<AuditViewRecord> getAuditView() {
        return custodyService.getAuditView();
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasAnyRole('ADMIN', 'JUDICIAL')")
    public Map<String, Object> getAnalyticsViews() {
        return custodyService.getAnalyticsViews();
    }

    @GetMapping("/integrity-checks")
    @PreAuthorize("hasAnyRole('ADMIN', 'JUDICIAL')")
    public Map<String, Object> getIntegrityChecks() {
        return custodyService.getIntegrityChecks();
    }
}
