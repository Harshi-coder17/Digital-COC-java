package com.chainguard.service;

import com.chainguard.dto.CreateEvidenceRequest;
import com.chainguard.exception.ResourceNotFoundException;
import com.chainguard.model.Evidence;
import com.chainguard.model.EvidenceStatus;
import com.chainguard.repository.EvidenceRepository;
import com.chainguard.security.InputSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class EvidenceService {

    private final EvidenceRepository evidenceRepository;

    public EvidenceService(EvidenceRepository evidenceRepository) {
        this.evidenceRepository = evidenceRepository;
    }

    @Transactional(readOnly = true)
    public List<Evidence> getAllEvidence(Integer caseId) {
        return evidenceRepository.findAll(caseId);
    }

    @Transactional(readOnly = true)
    public Evidence getEvidenceById(int id) {
        return evidenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found."));
    }

    @Transactional
    public int createEvidence(CreateEvidenceRequest request) {
        EvidenceStatus status = (request.status() != null && !request.status().isBlank())
                ? EvidenceStatus.fromValue(request.status())
                : EvidenceStatus.COLLECTED;
        return evidenceRepository.create(
                request.caseId(),
                InputSanitizer.sanitizeRequired(request.type(), "type"),
                InputSanitizer.sanitizeRequired(request.hashValue(), "hash_value"),
                status,
                InputSanitizer.sanitize(request.storageLocation())
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAuditSummary(int evidenceId) {
        // Verify evidence exists first
        getEvidenceById(evidenceId);
        int custodyCount = evidenceRepository.getCustodyCount(evidenceId);
        String summary = evidenceRepository.getAuditSummary(evidenceId);
        return Map.of(
                "evidence_id", evidenceId,
                "custody_count", custodyCount,
                "summary", summary != null ? summary : ""
        );
    }

    @Transactional
    public Evidence updateStorageLocation(int evidenceId, String location) {
        getEvidenceById(evidenceId);
        evidenceRepository.upsertStorageLocation(evidenceId, InputSanitizer.sanitizeRequired(location, "location"));
        return getEvidenceById(evidenceId);
    }
}
