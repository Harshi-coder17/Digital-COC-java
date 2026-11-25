package com.chainguard;

import com.chainguard.model.CaseStatus;
import com.chainguard.model.EvidenceStatus;
import com.chainguard.model.OfficerRole;
import com.chainguard.security.InputSanitizer;
import com.chainguard.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class ChainGuardTests {

    @Test
    void bcryptMatchesExistingSeedHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String seedHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        assertTrue(encoder.matches("password123", seedHash));
        assertFalse(encoder.matches("wrongpass", seedHash));
    }

    @Test
    void canonicalEnumsMapCorrectly() {
        assertEquals(CaseStatus.UNDER_INVESTIGATION, CaseStatus.fromValue("Under Investigation"));
        assertEquals(OfficerRole.JUDICIAL, OfficerRole.fromValue("judicial"));
        assertEquals(EvidenceStatus.IN_ANALYSIS, EvidenceStatus.fromValue("In Analysis"));
    }

    @Test
    void jwtRoundTripPreservesClaims() {
        JwtUtil jwtUtil = new JwtUtil("coc_jwt_secret_tiet_2026_change_this", 28800000L);
        String token = jwtUtil.generateToken(2, "officer", "Rajan Mehta");
        Claims claims = jwtUtil.parseToken(token);
        assertEquals(2, claims.get("id", Integer.class));
        assertEquals("officer", claims.get("role", String.class));
        assertEquals("Rajan Mehta", claims.get("name", String.class));
    }

    @Test
    void inputSanitizerStripsXssAndControlChars() {
        String dirty = "  <script>alert(1)</script><b>Test</b>\u0000 javascript:void(0) onload=hack()  ";
        String clean = InputSanitizer.sanitize(dirty);
        assertFalse(clean.contains("<script>"));
        assertFalse(clean.contains("<b>"));
        assertFalse(clean.toLowerCase().contains("javascript:"));
        assertFalse(clean.toLowerCase().contains("onload="));
        assertEquals("Test void(0) hack()", clean);
    }
}
