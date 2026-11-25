package com.chainguard.security;

import java.util.regex.Pattern;

/**
 * Server-side input sanitizer to neutralize XSS payloads, HTML injection,
 * and ASCII control characters before persistence.
 */
public final class InputSanitizer {

    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]");
    private static final Pattern SCRIPT_TAGS = Pattern.compile("(?is)<\\s*script[^>]*>.*?<\\s*/\\s*script\\s*>");
    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]+>");
    private static final Pattern DANGEROUS_URI = Pattern.compile("(?i)(javascript:|vbscript:|data:text/html)");
    private static final Pattern INLINE_EVENTS = Pattern.compile("(?i)\\bon\\w+\\s*=");

    private InputSanitizer() {}

    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        String cleaned = CONTROL_CHARS.matcher(input).replaceAll("");
        cleaned = SCRIPT_TAGS.matcher(cleaned).replaceAll("");
        cleaned = HTML_TAGS.matcher(cleaned).replaceAll("");
        cleaned = DANGEROUS_URI.matcher(cleaned).replaceAll("");
        cleaned = INLINE_EVENTS.matcher(cleaned).replaceAll("");
        return cleaned.trim();
    }

    public static String sanitizeRequired(String input, String fieldName) {
        String cleaned = sanitize(input);
        if (cleaned == null || cleaned.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty or contain only invalid characters.");
        }
        return cleaned;
    }
}
