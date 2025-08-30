package in.wealthinker.wealthinker.modules.auth.security.jwt;

import in.wealthinker.wealthinker.shared.constants.JwtConstants;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JWT Token Utilities - Helper methods for JWT operations
 *
 * PURPOSE:
 * - Provide utility methods for JWT token manipulation
 * - Extract common token operations into reusable methods
 * - Support testing and debugging scenarios
 * - Centralize token format and structure knowledge
 */
@Slf4j
public final class JwtTokenUtils {

    private JwtTokenUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Extract token from Authorization header
     *
     * @param authHeader Authorization header value
     * @return JWT token or null if not found
     */
    public static String extractTokenFromHeader(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(JwtConstants.TOKEN_PREFIX)) {
            return authHeader.substring(JwtConstants.TOKEN_PREFIX.length()).trim();
        }
        return null;
    }

    /**
     * Check if token is expired
     *
     * @param expirationDate Token expiration date
     * @return true if token is expired
     */
    public static boolean isTokenExpired(Date expirationDate) {
        return expirationDate != null && expirationDate.before(new Date());
    }

    /**
     * Check if token will expire soon
     *
     * @param expirationDate Token expiration date
     * @param thresholdMinutes Minutes before expiration to consider "soon"
     * @return true if token expires within threshold
     */
    public static boolean willExpireSoon(Date expirationDate, int thresholdMinutes) {
        if (expirationDate == null) {
            return true;
        }

        Instant threshold = Instant.now().plus(thresholdMinutes, ChronoUnit.MINUTES);
        return expirationDate.toInstant().isBefore(threshold);
    }

    /**
     * Calculate token remaining lifetime in seconds
     *
     * @param expirationDate Token expiration date
     * @return Remaining seconds or 0 if expired
     */
    public static long getRemainingLifetimeSeconds(Date expirationDate) {
        if (expirationDate == null) {
            return 0;
        }

        long remaining = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    /**
     * Get token type from claims
     *
     * @param claims JWT claims
     * @return Token type or null if not found
     */
    public static String getTokenType(Claims claims) {
        return claims.get(JwtConstants.CLAIM_TOKEN_TYPE, String.class);
    }

    /**
     * Check if token is of specific type
     *
     * @param claims JWT claims
     * @param expectedType Expected token type
     * @return true if token matches expected type
     */
    public static boolean isTokenType(Claims claims, String expectedType) {
        String tokenType = getTokenType(claims);
        return expectedType.equals(tokenType);
    }

    /**
     * Get user ID from claims
     *
     * @param claims JWT claims
     * @return User ID or null if not found
     */
    public static Long getUserId(Claims claims) {
        return claims.get(JwtConstants.CLAIM_USER_ID, Long.class);
    }

    /**
     * Get user email from claims
     *
     * @param claims JWT claims
     * @return User email or null if not found
     */
    public static String getUserEmail(Claims claims) {
        return claims.getSubject();
    }

    /**
     * Get user role from claims
     *
     * @param claims JWT claims
     * @return User role or null if not found
     */
    public static String getUserRole(Claims claims) {
        return claims.get(JwtConstants.CLAIM_ROLE, String.class);
    }

    /**
     * Get user permissions from claims
     *
     * @param claims JWT claims
     * @return List of permissions or empty list if not found
     */
    @SuppressWarnings("unchecked")
    public static List<String> getUserPermissions(Claims claims) {
        Object permissions = claims.get(JwtConstants.CLAIM_PERMISSIONS);
        if (permissions instanceof List) {
            return (List<String>) permissions;
        }
        return List.of();
    }

    /**
     * Create claims map for testing
     *
     * @param userId User ID
     * @param email User email
     * @param role User role
     * @param tokenType Token type
     * @return Claims map
     */
    public static Map<String, Object> createTestClaims(Long userId, String email, String role, String tokenType) {
        Instant now = Instant.now();
        Instant expiration = now.plus(15, ChronoUnit.MINUTES);

        return Map.of(
                JwtConstants.CLAIM_SUBJECT, email,
                JwtConstants.CLAIM_USER_ID, userId,
                JwtConstants.CLAIM_ROLE, role,
                JwtConstants.CLAIM_TOKEN_TYPE, tokenType,
                JwtConstants.CLAIM_ISSUED_AT, Date.from(now),
                JwtConstants.CLAIM_EXPIRES_AT, Date.from(expiration),
                JwtConstants.CLAIM_ISSUER, "test-issuer",
                JwtConstants.CLAIM_AUDIENCE, "test-audience"
        );
    }

    /**
     * Format token for logging (masked for security)
     *
     * @param token JWT token
     * @return Masked token for safe logging
     */
    public static String maskTokenForLogging(String token) {
        if (token == null || token.length() < 20) {
            return "***";
        }

        // Show first 10 and last 10 characters, mask the middle
        return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
    }

    /**
     * Validate token structure without parsing
     *
     * @param token JWT token
     * @return true if structure is valid
     */
    public static boolean hasValidStructure(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        String[] parts = token.split("\\.");
        return parts.length == 3 &&
                !parts[0].isEmpty() &&
                !parts[1].isEmpty() &&
                !parts[2].isEmpty();
    }
}
