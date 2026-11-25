package com.chainguard.controller;

import com.chainguard.dto.CreateEvidenceRequest;
import com.chainguard.dto.UpdateStorageRequest;
import com.chainguard.model.Evidence;
import com.chainguard.service.EvidenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @GetMapping
    public List<Evidence> getAllEvidence(@RequestParam(name = "case_id", required = false) Integer caseId) {
        return evidenceService.getAllEvidence(caseId);
    }

    @GetMapping("/{id}")
    public Evidence getEvidenceById(@PathVariable int id) {
        return evidenceService.getEvidenceById(id);
    }

    @GetMapping("/{id}/audit-summary")
    public Map<String, Object> getAuditSummary(@PathVariable int id) {
        return evidenceService.getAuditSummary(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    public ResponseEntity<Map<String, Object>> createEvidence(@Valid @RequestBody CreateEvidenceRequest request) {
        int evidenceId = evidenceService.createEvidence(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Evidence created successfully", "evidence_id", evidenceId));
    }

    @PutMapping("/{id}/storage")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    public Evidence updateStorageLocation(@PathVariable int id, @Valid @RequestBody UpdateStorageRequest request) {
        return evidenceService.updateStorageLocation(id, request.location());
    }
}
