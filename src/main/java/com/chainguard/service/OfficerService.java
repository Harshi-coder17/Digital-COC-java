package com.chainguard.service;

import com.chainguard.dto.CreateOfficerRequest;
import com.chainguard.exception.ResourceNotFoundException;
import com.chainguard.model.Officer;
import com.chainguard.model.OfficerRole;
import com.chainguard.repository.OfficerRepository;
import com.chainguard.security.InputSanitizer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OfficerService {

    private final OfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;

    public OfficerService(OfficerRepository officerRepository, PasswordEncoder passwordEncoder) {
        this.officerRepository = officerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Officer> getAllOfficers() {
        return officerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Officer getOfficerById(int id) {
        return officerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found."));
    }

    @Transactional
    public int createOfficer(CreateOfficerRequest request) {
        OfficerRole role = (request.role() != null && !request.role().isBlank())
                ? OfficerRole.fromValue(request.role())
                : OfficerRole.OFFICER;
        String hash = passwordEncoder.encode(request.password());
        return officerRepository.create(
                InputSanitizer.sanitizeRequired(request.name(), "name"),
                InputSanitizer.sanitizeRequired(request.rank(), "rank"),
                InputSanitizer.sanitizeRequired(request.department(), "department"),
                role,
                hash
        );
    }
}
