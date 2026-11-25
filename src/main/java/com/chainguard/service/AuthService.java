package com.chainguard.service;

import com.chainguard.dto.LoginRequest;
import com.chainguard.dto.LoginResponse;
import com.chainguard.model.Officer;
import com.chainguard.repository.OfficerRepository;
import com.chainguard.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final OfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(
            OfficerRepository officerRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.officerRepository = officerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        Officer officer = officerRepository.findById(request.officerId())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials."));

        if (officer.passwordHash() == null || !passwordEncoder.matches(request.password(), officer.passwordHash())) {
            throw new BadCredentialsException("Invalid credentials.");
        }

        String token = jwtUtil.generateToken(
                officer.officerId(),
                officer.role().getDbValue(),
                officer.name()
        );

        return new LoginResponse(
                token,
                officer.role().getDbValue(),
                officer.name(),
                officer.officerId()
        );
    }
}
