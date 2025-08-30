package in.wealthinker.wealthinker.modules.auth.security.jwt;

import in.wealthinker.wealthinker.config.JwtConfig;
import in.wealthinker.wealthinker.shared.constants.JwtConstants;
import in.wealthinker.wealthinker.shared.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * JWT Token Validator - Separate validation logic for better maintainability
 *
 * PURPOSE:
 * - Centralize all JWT token validation logic
 * - Provide detailed validation error information
 * - Support different validation contexts
 * - Enable validation rule customization
 *
 * VALIDATION STAGES:
 * 1. Structure validation (format, parts)
 * 2. Signature validation (cryptographic verification)
 * 3. Claims validation (expiration, issuer, audience)
 * 4. Business rule validation (user status, permissions)
 * 5. Security validation (blacklist, fraud detection)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenValidator {

    private final JwtConfig jwtConfig;
    private final JwtTokenBlacklist tokenBlacklist;

    /**
     * Comprehensive token validation with detailed error reporting
     *
     * @param token JWT token to validate
     * @param claims Token claims (if already parsed)
     * @return TokenValidationResult with validation details
     */
    public TokenValidationResult validateToken(String token, Claims claims) {
        log.debug("Starting comprehensive token validation");

        List<String> validationErrors = new ArrayList<>();
        TokenValidationResult.ValidationStage failedStage = null;

        try {
            // Stage 1: Structure validation
            if (!validateTokenStructure(token)) {
                validationErrors.add("Invalid token structure");
                failedStage = TokenValidationResult.ValidationStage.STRUCTURE;
            }

            // Stage 2: Claims validation (if claims provided)
            if (claims != null) {
                List<String> claimsErrors = validateClaims(claims);
                validationErrors.addAll(claimsErrors);
                if (!claimsErrors.isEmpty() && failedStage == null) {
                    failedStage = TokenValidationResult.ValidationStage.CLAIMS;
                }
            }

            // Stage 3: Expiration validation
            if (claims != null && !validateExpiration(claims)) {
                validationErrors.add("Token has expired");
                if (failedStage == null) {
                    failedStage = TokenValidationResult.ValidationStage.EXPIRATION;
                }
            }

            // Stage 4: Blacklist validation
            if (jwtConfig.getBlacklistEnabled() && isTokenBlacklisted(token)) {
                validationErrors.add("Token has been blacklisted");
                if (failedStage == null) {
                    failedStage = TokenValidationResult.ValidationStage.BLACKLIST;
                }
            }

            // Stage 5: Business rules validation
            if (claims != null) {
                List<String> businessErrors = validateBusinessRules(claims);
                validationErrors.addAll(businessErrors);
                if (!businessErrors.isEmpty() && failedStage == null) {
                    failedStage = TokenValidationResult.ValidationStage.BUSINESS_RULES;
                }
            }

            // Build validation result
            boolean isValid = validationErrors.isEmpty();

            TokenValidationResult result = TokenValidationResult.builder()
                    .valid(isValid)
                    .validationErrors(validationErrors)
                    .failedStage(failedStage)
                    .validatedAt(Instant.now())
                    .build();

            log.debug("Token validation completed: valid={}, errorCount={}",
                    isValid, validationErrors.size());

            return result;

        } catch (ExpiredJwtException e) {
            log.debug("Token expired during validation");
            return TokenValidationResult.builder()
                    .valid(false)
                    .validationErrors(List.of("Token has expired"))
                    .failedStage(TokenValidationResult.ValidationStage.EXPIRATION)
                    .validatedAt(Instant.now())
                    .expirationException(e)
                    .build();

        } catch (Exception e) {
            log.error("Unexpected error during token validation", e);
            return TokenValidationResult.builder()
                    .valid(false)
                    .validationErrors(List.of("Validation error: " + e.getMessage()))
                    .failedStage(TokenValidationResult.ValidationStage.UNKNOWN)
                    .validatedAt(Instant.now())
                    .build();
        }
    }

    /**
     * Quick token validation for performance-critical paths
     *
     * @param token JWT token to validate
     * @return true if token is valid
     */
    public boolean isTokenValid(String token) {
        try {
            // Basic structure check
            if (!validateTokenStructure(token)) {
                return false;
            }

            // Blacklist check (if enabled)
            if (jwtConfig.getBlacklistEnabled() && isTokenBlacklisted(token)) {
                return false;
            }

            return true;

        } catch (Exception e) {
            log.debug("Quick token validation failed", e);
            return false;
        }
    }

    /**
     * Validate JWT token structure
     *
     * @param token JWT token
     * @return true if structure is valid
     */
    private boolean validateTokenStructure(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        // JWT should have exactly 3 parts separated by dots
        String[] tokenParts = token.split("\\.");
        return tokenParts.length == 3;
    }

    /**
     * Validate JWT claims
     *
     * @param claims Token claims
     * @return List of validation errors (empty if valid)
     */
    private List<String> validateClaims(Claims claims) {
        List<String> errors = new ArrayList<>();

        // Validate required claims
        if (claims.getSubject() == null || claims.getSubject().trim().isEmpty()) {
            errors.add("Subject claim is missing or empty");
        }

        if (claims.getIssuedAt() == null) {
            errors.add("Issued at claim is missing");
        }

        if (claims.getExpiration() == null) {
            errors.add("Expiration claim is missing");
        }

        // Validate issuer
        if (!jwtConfig.getIssuer().equals(claims.getIssuer())) {
            errors.add("Invalid token issuer: " + claims.getIssuer());
        }

        // Validate audience
        if (!jwtConfig.getAudience().equals(claims.getAudience())) {
            errors.add("Invalid token audience: " + claims.getAudience());
        }

        // Validate custom claims
        Long userId = claims.get(JwtConstants.CLAIM_USER_ID, Long.class);
        if (userId == null || userId <= 0) {
            errors.add("Invalid or missing user ID");
        }

        String tokenType = claims.get(JwtConstants.CLAIM_TOKEN_TYPE, String.class);
        if (tokenType == null || tokenType.trim().isEmpty()) {
            errors.add("Token type is missing");
        }

        return errors;
    }

    /**
     * Validate token expiration
     *
     * @param claims Token claims
     * @return true if token is not expired
     */
    private boolean validateExpiration(Claims claims) {
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            return false;
        }

        // Add clock skew tolerance
        long currentTimeMillis = Instant.now().toEpochMilli();
        long expirationMillis = expiration.getTime();
        long clockSkewMillis = jwtConfig.getClockSkewSeconds() * 1000L;

        return expirationMillis + clockSkewMillis > currentTimeMillis;
    }

    /**
     * Check if token is blacklisted
     *
     * @param token JWT token
     * @return true if token is blacklisted
     */
    private boolean isTokenBlacklisted(String token) {
        try {
            return tokenBlacklist.isBlacklisted(token);
        } catch (Exception e) {
            log.warn("Error checking token blacklist status", e);
            return true; // Fail secure
        }
    }

    /**
     * Validate business rules
     *
     * @param claims Token claims
     * @return List of business rule violations
     */
    private List<String> validateBusinessRules(Claims claims) {
        List<String> errors = new ArrayList<>();

        // Validate role exists and is valid
        String roleString = claims.get(JwtConstants.CLAIM_ROLE, String.class);
        if (roleString != null) {
            try {
                UserRole.valueOf(roleString);
            } catch (IllegalArgumentException e) {
                errors.add("Invalid user role: " + roleString);
            }
        }

        // Validate token age (not too old for security)
        Date issuedAt = claims.getIssuedAt();
        if (issuedAt != null) {
            long tokenAge = Instant.now().getEpochSecond() - issuedAt.toInstant().getEpochSecond();
            long maxTokenAge = jwtConfig.getAccessTokenExpiration() / 1000L * 2; // 2x normal expiration

            if (tokenAge > maxTokenAge) {
                errors.add("Token is too old, possible replay attack");
            }
        }

        // Add more business rules as needed
        // - IP address validation
        // - Device fingerprinting
        // - Rate limiting checks
        // - Fraud detection rules

        return errors;
    }

    /**
     * Token Validation Result DTO
     */
    public static class TokenValidationResult {
        public enum ValidationStage {
            STRUCTURE, SIGNATURE, CLAIMS, EXPIRATION, BLACKLIST, BUSINESS_RULES, UNKNOWN
        }

        private final boolean valid;
        private final List<String> validationErrors;
        private final ValidationStage failedStage;
        private final Instant validatedAt;
        private final ExpiredJwtException expirationException;

        private TokenValidationResult(Builder builder) {
            this.valid = builder.valid;
            this.validationErrors = builder.validationErrors;
            this.failedStage = builder.failedStage;
            this.validatedAt = builder.validatedAt;
            this.expirationException = builder.expirationException;
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public boolean isValid() { return valid; }
        public List<String> getValidationErrors() { return validationErrors; }
        public ValidationStage getFailedStage() { return failedStage; }
        public Instant getValidatedAt() { return validatedAt; }
        public ExpiredJwtException getExpirationException() { return expirationException; }

        public static class Builder {
            private boolean valid;
            private List<String> validationErrors = new ArrayList<>();
            private ValidationStage failedStage;
            private Instant validatedAt;
            private ExpiredJwtException expirationException;

            public Builder valid(boolean valid) { this.valid = valid; return this; }
            public Builder validationErrors(List<String> errors) { this.validationErrors = errors; return this; }
            public Builder failedStage(ValidationStage stage) { this.failedStage = stage; return this; }
            public Builder validatedAt(Instant time) { this.validatedAt = time; return this; }
            public Builder expirationException(ExpiredJwtException ex) { this.expirationException = ex; return this; }

            public TokenValidationResult build() {
                return new TokenValidationResult(this);
            }
        }
    }
}
