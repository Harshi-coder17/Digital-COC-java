package com.chainguard.service;

import com.chainguard.dto.AppendFindingRequest;
import com.chainguard.dto.CreateForensicReportRequest;
import com.chainguard.model.ForensicFinding;
import com.chainguard.model.ForensicReport;
import com.chainguard.repository.ForensicRepository;
import com.chainguard.security.InputSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ForensicService {

    private final ForensicRepository forensicRepository;

    public ForensicService(ForensicRepository forensicRepository) {
        this.forensicRepository = forensicRepository;
    }

    @Transactional(readOnly = true)
    public List<ForensicFinding> getFindingsForEvidence(int evidenceId) {
        return forensicRepository.findByEvidenceId(evidenceId);
    }

    @Transactional
    public void appendFinding(AppendFindingRequest request, int reportedByOfficerId) {
        // Sanitize XSS payloads while still passing empty/whitespace strings through to
        // AppendForensicFinding so MySQL SQLState 45000 validation is enforced at the DB layer.
        String sanitized = request.findingText() != null
                ? InputSanitizer.sanitize(request.findingText())
                : null;
        forensicRepository.callAppendForensicFinding(
                request.evidenceId(),
                reportedByOfficerId,
                sanitized
        );
    }

    @Transactional
    public void updateFinding(int findingId, String findingText) {
        forensicRepository.updateFinding(findingId, findingText);
    }

    @Transactional
    public void deleteFinding(int findingId) {
        forensicRepository.deleteFinding(findingId);
    }

    @Transactional(readOnly = true)
    public List<ForensicReport> getReports(Integer evidenceId) {
        return forensicRepository.findAllReports(evidenceId);
    }

    @Transactional
    public int createReport(CreateForensicReportRequest request, String analystName) {
        LocalDate date = request.reportDate() != null ? request.reportDate() : LocalDate.now();
        return forensicRepository.createReport(
                request.evidenceId(),
                InputSanitizer.sanitizeRequired(analystName, "analyst_name"),
                date
        );
    }
}
