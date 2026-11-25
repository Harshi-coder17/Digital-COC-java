package com.chainguard.controller;

import com.chainguard.dto.CreateCaseRequest;
import com.chainguard.dto.UpdateCaseRequest;
import com.chainguard.model.CaseDetails;
import com.chainguard.service.CaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @GetMapping
    public List<CaseDetails> getAllCases() {
        return caseService.getAllCases();
    }

    @GetMapping("/{id}")
    public CaseDetails getCaseById(@PathVariable int id) {
        return caseService.getCaseById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    public ResponseEntity<Map<String, Object>> createCase(@Valid @RequestBody CreateCaseRequest request) {
        int caseId = caseService.createCase(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("case_id", caseId, "message", "Case created."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    public CaseDetails updateCase(@PathVariable int id, @Valid @RequestBody UpdateCaseRequest request) {
        return caseService.updateCase(id, request);
    }
}
