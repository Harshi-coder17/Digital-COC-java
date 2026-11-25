package com.chainguard.controller;

import com.chainguard.dto.AppendFindingRequest;
import com.chainguard.dto.CreateForensicReportRequest;
import com.chainguard.model.ForensicFinding;
import com.chainguard.model.ForensicReport;
import com.chainguard.security.OfficerPrincipal;
import com.chainguard.service.ForensicService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forensic")
public class ForensicController {

    private final ForensicService forensicService;

    public ForensicController(ForensicService forensicService) {
        this.forensicService = forensicService;
    }

    @GetMapping("/findings/{evidenceId}")
    public List<ForensicFinding> getFindings(@PathVariable int evidenceId) {
        return forensicService.getFindingsForEvidence(evidenceId);
    }

    @PostMapping("/findings")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<Map<String, String>> appendFinding(
            @Valid @RequestBody AppendFindingRequest request,
            @AuthenticationPrincipal OfficerPrincipal principal) {
        forensicService.appendFinding(request, principal.officerId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Finding appended successfully."));
    }

    @PutMapping("/findings/{findingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public Map<String, String> updateFinding(
            @PathVariable int findingId,
            @RequestBody Map<String, String> body) {
        forensicService.updateFinding(findingId, body.getOrDefault("finding_text", ""));
        return Map.of("message", "Updated.");
    }

    @DeleteMapping("/findings/{findingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public Map<String, String> deleteFinding(@PathVariable int findingId) {
        forensicService.deleteFinding(findingId);
        return Map.of("message", "Deleted.");
    }

    @GetMapping("/reports")
    public List<ForensicReport> getAllReports(@RequestParam(name = "evidence_id", required = false) Integer evidenceId) {
        return forensicService.getReports(evidenceId);
    }

    @GetMapping("/reports/{evidenceId}")
    public List<ForensicReport> getReportsByEvidence(@PathVariable int evidenceId) {
        return forensicService.getReports(evidenceId);
    }

    @PostMapping("/reports")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<Map<String, Object>> createReport(
            @Valid @RequestBody CreateForensicReportRequest request,
            @AuthenticationPrincipal OfficerPrincipal principal) {
        int reportId = forensicService.createReport(request, principal.name());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Forensic report filed successfully.", "report_id", reportId));
    }
}
