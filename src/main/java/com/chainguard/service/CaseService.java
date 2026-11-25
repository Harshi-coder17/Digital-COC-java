package com.chainguard.service;

import com.chainguard.dto.CreateCaseRequest;
import com.chainguard.dto.UpdateCaseRequest;
import com.chainguard.exception.ResourceNotFoundException;
import com.chainguard.model.CaseDetails;
import com.chainguard.model.CaseStatus;
import com.chainguard.repository.CaseRepository;
import com.chainguard.security.InputSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CaseService {

    private final CaseRepository caseRepository;

    public CaseService(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    @Transactional(readOnly = true)
    public List<CaseDetails> getAllCases() {
        return caseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CaseDetails getCaseById(int id) {
        return caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found."));
    }

    @Transactional
    public int createCase(CreateCaseRequest request) {
        CaseStatus status = (request.status() != null && !request.status().isBlank())
                ? CaseStatus.fromValue(request.status())
                : CaseStatus.OPEN;
        return caseRepository.create(
                InputSanitizer.sanitizeRequired(request.caseTitle(), "case_title"),
                InputSanitizer.sanitize(request.description()),
                request.startDate(),
                status
        );
    }

    @Transactional
    public CaseDetails updateCase(int id, UpdateCaseRequest request) {
        CaseDetails existing = getCaseById(id);
        String title = (request.caseTitle() != null && !request.caseTitle().isBlank())
                ? InputSanitizer.sanitizeRequired(request.caseTitle(), "case_title")
                : existing.caseTitle();
        String desc = request.description() != null
                ? InputSanitizer.sanitize(request.description())
                : existing.description();
        CaseStatus status = (request.status() != null && !request.status().isBlank())
                ? CaseStatus.fromValue(request.status())
                : existing.status();
        caseRepository.update(id, title, desc, status);
        return getCaseById(id);
    }
}
