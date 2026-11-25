package com.chainguard.controller;

import com.chainguard.dto.CreateOfficerRequest;
import com.chainguard.model.Officer;
import com.chainguard.service.OfficerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/officers")
public class OfficerController {

    private final OfficerService officerService;

    public OfficerController(OfficerService officerService) {
        this.officerService = officerService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    public List<Officer> getAllOfficers() {
        return officerService.getAllOfficers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    public Officer getOfficerById(@PathVariable int id) {
        return officerService.getOfficerById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createOfficer(@Valid @RequestBody CreateOfficerRequest request) {
        int officerId = officerService.createOfficer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Officer created successfully", "officer_id", officerId));
    }
}
