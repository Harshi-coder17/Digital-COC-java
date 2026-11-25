package com.chainguard.service;

import com.chainguard.dto.TransferCustodyRequest;
import com.chainguard.model.AuditViewRecord;
import com.chainguard.model.CourtViewRecord;
import com.chainguard.model.CustodyLogEntry;
import com.chainguard.model.EvidenceStatus;
import com.chainguard.repository.CustodyRepository;
import com.chainguard.security.InputSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CustodyService {

    private final CustodyRepository custodyRepository;

    public CustodyService(CustodyRepository custodyRepository) {
        this.custodyRepository = custodyRepository;
    }

    @Transactional(readOnly = true)
    public List<CustodyLogEntry> getCustodyHistory(int evidenceId) {
        return custodyRepository.findByEvidenceId(evidenceId);
    }

    @Transactional
    public void transferCustody(TransferCustodyRequest request) {
        EvidenceStatus validatedStatus = EvidenceStatus.fromValue(request.newStatus());
        custodyRepository.callTransferCustody(
                request.evidenceId(),
                request.officerId(),
                InputSanitizer.sanitizeRequired(request.action(), "action"),
                validatedStatus.getDbValue()
        );
    }

    @Transactional
    public void deleteCustodyLog(int logId) {
        custodyRepository.deleteByLogId(logId);
    }

    @Transactional(readOnly = true)
    public List<CourtViewRecord> getCourtView() {
        return custodyRepository.findCourtView();
    }

    @Transactional(readOnly = true)
    public List<AuditViewRecord> getAuditView() {
        return custodyRepository.findAuditView();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAnalyticsViews() {
        return Map.of(
                "case_evidence_count", custodyRepository.findCaseEvidenceCounts(),
                "evidence_custody_stats", custodyRepository.findEvidenceCustodyStats(),
                "evidence_status_distribution", custodyRepository.findEvidenceStatusDistribution(),
                "cases_with_multiple_evidence", custodyRepository.findCasesWithMultipleEvidence()
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getIntegrityChecks() {
        return Map.of(
                "evidence_without_reports", custodyRepository.findEvidenceWithoutReports(),
                "officers_without_custody", custodyRepository.findOfficersWithoutCustody(),
                "cases_without_evidence", custodyRepository.findCasesWithoutEvidence()
        );
    }
}
