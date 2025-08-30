package in.wealthinker.wealthinker.modules.auth.security.jwt;


import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import in.wealthinker.wealthinker.modules.auth.security.userdetails.UserPrincipal;
import in.wealthinker.wealthinker.modules.auth.security.exceptions.JwtAuthenticationException;
import in.wealthinker.wealthinker.modules.auth.security.exceptions.TokenBlacklistedException;
import in.wealthinker.wealthinker.modules.auth.security.exceptions.TokenExpiredException;
import in.wealthinker.wealthinker.shared.constants.JwtConstants;
import in.wealthinker.wealthinker.shared.enums.UserRole;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import in.wealthinker.wealthinker.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;

/**
 * JWT Token Provider - Core JWT operations
 *
 * RESPONSIBILITIES:
 * - Generate access and refresh tokens
 * - Validate and parse JWT tokens
 * - Extract claims from tokens
 * - Handle token expiration and blacklisting
 * - Support both symmetric and asymmetric signing
 *
 * SECURITY FEATURES:
 * - Secure key management
 * - Token type validation
 * - Claims validation (issuer, audience, expiration)
 * - Protection against common JWT vulnerabilities
 * - Token blacklisting support
 *
 * PERFORMANCE OPTIMIZATIONS:
 * - Cached key generation
 * - Efficient claims parsing
 * - Minimal object creation
 * - Thread-safe implementation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;
    private final JwtTokenBlacklist tokenBlacklist;
    private final JwtKeyManager keyManager;

    private SecretKey signingKey;
    private JwtParser jwtParser;

    /**
     * Initialize JWT provider after dependency injection
     *
     * SECURITY: Validates configuration and prepares signing keys
     * PERFORMANCE: Pre-creates parser to avoid repeated initialization
     */
    @PostConstruct
    public void init() {
        try {
            // Validate JWT configuration
            jwtConfig.validate();

            // Initialize signing key based on algorithm
            initializeSigningKey();

            // Create JWT parser with validation rules
            initializeJwtParser();

            log.info("JWT Token Provider initialized successfully with algorithm: {}", jwtConfig.getAlgorithm());

        } catch (Exception e) {
            log.error("Failed to initialize JWT Token Provider", e);
            throw new IllegalStateException("JWT Token Provider initialization failed", e);
        }
    }

    // =================== TOKEN GENERATION ===================

    /**
     * Generate Access Token for authenticated user
     *
     * ACCESS TOKENS:
     * - Short-lived (15-30 minutes)
     * - Contains user identity and permissions
     * - Used for API authentication
     * - Should be stored in memory only (never localStorage)
     *
     * @param userPrincipal Authenticated user details
     * @return JWT access token
     */
    public String generateAccessToken(UserPrincipal userPrincipal) {
        return generateAccessToken(userPrincipal, false);
    }

    /**
     * Generate Access Token with optional remember me functionality
     *
     * REMEMBER ME:
     * - Longer expiration for convenience
     * - Should still be reasonable (90 days max)
     * - User explicitly opts in
     *
     * @param userPrincipal Authenticated user details
     * @param rememberMe Whether to use extended expiration
     * @return JWT access token
     */
    public String generateAccessToken(UserPrincipal userPrincipal, boolean rememberMe) {
        log.debug("Generating access token for user: {}, rememberMe: {}", userPrincipal.getUsername(), rememberMe);

        Instant now = Instant.now();
        String tokenType = rememberMe ? JwtConstants.REMEMBER_ME_TOKEN_TYPE : JwtConstants.ACCESS_TOKEN_TYPE;
        Long expiration = jwtConfig.getExpirationForTokenType(tokenType);
        Instant expiryDate = now.plus(expiration, ChronoUnit.MILLIS);

        // Extract user authorities/permissions
        List<String> authorities = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Extract user permissions (more granular than roles)
        List<String> permissions = extractPermissions(userPrincipal);

        // Generate unique token ID for blacklisting support
        String tokenId = UUID.randomUUID().toString();

        try {
            String token = Jwts.builder()
                    // Standard Claims (RFC 7519)
                    .setIssuer(jwtConfig.getIssuer())
                    .setAudience(jwtConfig.getAudience())
                    .setSubject(userPrincipal.getUsername())
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiryDate))
                    .setNotBefore(Date.from(now))
                    .setId(tokenId)

                    // Custom Claims
                    .claim(JwtConstants.CLAIM_USER_ID, userPrincipal.getId())
                    .claim(JwtConstants.CLAIM_EMAIL, userPrincipal.getEmail())
                    .claim(JwtConstants.CLAIM_ROLE, userPrincipal.getRole().name())
                    .claim(JwtConstants.CLAIM_PERMISSIONS, permissions)
                    .claim(JwtConstants.CLAIM_TOKEN_TYPE, tokenType)

                    // Security Claims
                    .signWith(signingKey)
                    .compact();

            log.debug("Access token generated successfully for user: {}, tokenId: {}", userPrincipal.getUsername(), tokenId);
            return token;

        } catch (Exception e) {
            log.error("Failed to generate access token for user: {}", userPrincipal.getUsername(), e);
            throw new JwtAuthenticationException("Failed to generate access token", e);
        }
    }

    /**
     * Generate Refresh Token for token renewal
     *
     * REFRESH TOKENS:
     * - Long-lived (7-30 days)
     * - Contains minimal information (just user ID)
     * - Used only to generate new access tokens
     * - Should be stored securely (httpOnly cookies or secure storage)
     * - Can be revoked independently
     *
     * @param userPrincipal Authenticated user details
     * @return JWT refresh token
     */
    public String generateRefreshToken(UserPrincipal userPrincipal) {
        log.debug("Generating refresh token for user: {}", userPrincipal.getUsername());

        Instant now = Instant.now();
        Instant expiryDate = now.plus(jwtConfig.getRefreshTokenExpiration(), ChronoUnit.MILLIS);
        String tokenId = UUID.randomUUID().toString();

        try {
            String token = Jwts.builder()
                    // Minimal claims for refresh tokens
                    .setIssuer(jwtConfig.getIssuer())
                    .setAudience(jwtConfig.getAudience())
                    .setSubject(userPrincipal.getUsername())
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiryDate))
                    .setNotBefore(Date.from(now))
                    .setId(tokenId)

                    // Minimal custom claims
                    .claim(JwtConstants.CLAIM_USER_ID, userPrincipal.getId())
                    .claim(JwtConstants.CLAIM_TOKEN_TYPE, JwtConstants.REFRESH_TOKEN_TYPE)

                    .signWith(signingKey)
                    .compact();

            log.debug("Refresh token generated successfully for user: {}, tokenId: {}", userPrincipal.getUsername(), tokenId);
            return token;

        } catch (Exception e) {
            log.error("Failed to generate refresh token for user: {}", userPrincipal.getUsername(), e);
            throw new JwtAuthenticationException("Failed to generate refresh token", e);
        }
    }

    // =================== TOKEN VALIDATION ===================

    /**
     * Validate JWT token structure, signature, and claims
     *
     * VALIDATION CHECKS:
     * 1. Token format and structure
     * 2. Signature verification
     * 3. Expiration check
     * 4. Issuer and audience validation
     * 5. Not-before time check
     * 6. Blacklist check (if enabled)
     *
     * @param token JWT token to validate
     * @return true if token is valid
     * @throws JwtAuthenticationException if token is invalid
     */
    public boolean validateToken(String token) {
        try {
            log.debug("Validating JWT token");

            // Check if token is blacklisted
            if (jwtConfig.getBlacklistEnabled() && tokenBlacklist.isBlacklisted(token)) {
                log.warn("Token validation failed: token is blacklisted");
                throw new TokenBlacklistedException("Token has been blacklisted");
            }

            // Parse and validate token
            Claims claims = jwtParser.parseClaimsJws(token).getBody();

            // Additional custom validations
            validateTokenClaims(claims);

            log.debug("Token validation successful");
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("Token validation failed: token expired at {}", e.getClaims().getExpiration());
            Instant expiredAt = e.getClaims().getExpiration().toInstant();
            throw new TokenExpiredException("Token has expired", expiredAt, e);
        } catch (UnsupportedJwtException e) {
            log.warn("Token validation failed: unsupported JWT token");
            throw new JwtAuthenticationException(JwtConstants.UNSUPPORTED_TOKEN, e);

        } catch (MalformedJwtException e) {
            log.warn("Token validation failed: invalid JWT token format");
            throw new JwtAuthenticationException(JwtConstants.INVALID_TOKEN, e);

        } catch (SignatureException e) {
            log.warn("Token validation failed: invalid JWT signature");
            throw new JwtAuthenticationException("Invalid JWT signature", e);

        } catch (IllegalArgumentException e) {
            log.warn("Token validation failed: JWT claims string is empty");
            throw new JwtAuthenticationException(JwtConstants.EMPTY_CLAIMS, e);

        } catch (TokenBlacklistedException e) {
            // Re-throw blacklist exceptions
            throw e;

        } catch (Exception e) {
            log.error("Token validation failed with unexpected error", e);
            throw new JwtAuthenticationException("Token validation failed", e);
        }
    }

    /**
     * Extract Claims from JWT token
     *
     * CLAIMS EXTRACTION:
     * - Validates token before extraction
     * - Returns all claims for further processing
     * - Thread-safe implementation
     *
     * @param token JWT token
     * @return Claims object containing all token claims
     */
    public Claims extractClaims(String token) {
        try {
            log.debug("Extracting claims from JWT token");

            // Validate token first
            validateToken(token);

            // Extract claims
            Claims claims = jwtParser.parseClaimsJws(token).getBody();

            log.debug("Claims extracted successfully for subject: {}", claims.getSubject());
            return claims;

        } catch (Exception e) {
            log.error("Failed to extract claims from token", e);
            throw new JwtAuthenticationException("Failed to extract claims", e);
        }
    }

    /**
     * Extract specific claim value from token
     *
     * @param token JWT token
     * @param claimName Name of the claim to extract
     * @return Claim value or null if not found
     */
    public <T> T extractClaim(String token, String claimName, Class<T> clazz) {
        Claims claims = extractClaims(token);
        return claims.get(claimName, clazz);
    }

    /**
     * Extract User ID from token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, JwtConstants.CLAIM_USER_ID, Long.class);
    }

    /**
     * Extract Username from token
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extract User Role from token
     */
    public UserRole extractUserRole(String token) {
        String roleString = extractClaim(token, JwtConstants.CLAIM_ROLE, String.class);
        return roleString != null ? UserRole.valueOf(roleString) : null;
    }

    /**
     * Extract User Permissions from token
     */
    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        return extractClaim(token, JwtConstants.CLAIM_PERMISSIONS, List.class);
    }

    /**
     * Get token expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        return extractClaims(token).getExpiration();
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Get token ID (for blacklisting)
     */
    public String getTokenId(String token) {
        return extractClaims(token).getId();
    }

    // =================== TOKEN BLACKLISTING ===================

    /**
     * Blacklist a token (for logout functionality)
     *
     * TOKEN BLACKLISTING:
     * - Immediate token revocation
     * - Required for secure logout
     * - Stores token ID with remaining TTL
     *
     * @param token JWT token to blacklist
     */
    public void blacklistToken(String token) {
        try {
            if (!jwtConfig.getBlacklistEnabled()) {
                log.debug("Token blacklisting is disabled");
                return;
            }

            String tokenId = getTokenId(token);
            Date expiration = getExpirationDateFromToken(token);

            tokenBlacklist.blacklistToken(tokenId, expiration);

            log.info("Token blacklisted successfully, tokenId: {}", tokenId);

        } catch (Exception e) {
            log.error("Failed to blacklist token", e);
            throw new JwtAuthenticationException("Failed to blacklist token", e);
        }
    }

    /**
     * Check if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        if (!jwtConfig.getBlacklistEnabled()) {
            return false;
        }

        try {
            return tokenBlacklist.isBlacklisted(token);
        } catch (Exception e) {
            log.warn("Error checking token blacklist status, treating as blacklisted", e);
            return true; // Fail secure
        }
    }

    // =================== PRIVATE HELPER METHODS ===================

    /**
     * Initialize signing key based on configured algorithm
     */
    private void initializeSigningKey() {
        if (jwtConfig.isSymmetricSigning()) {
            // Symmetric key for HMAC algorithms
            byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
            log.debug("Initialized symmetric signing key for algorithm: {}", jwtConfig.getAlgorithm());

        } else if (jwtConfig.isAsymmetricSigning()) {
            // Asymmetric key for RSA/ECDSA algorithms
            this.signingKey = (SecretKey) keyManager.getPrivateKey();
            log.debug("Initialized asymmetric signing key for algorithm: {}", jwtConfig.getAlgorithm());

        } else {
            throw new IllegalStateException("Unsupported JWT algorithm: " + jwtConfig.getAlgorithm());
        }
    }

    /**
     * Initialize JWT parser with security settings
     */
    private void initializeJwtParser() {
        JwtParserBuilder parserBuilder = Jwts.parser();

        // Set verification key
        if (jwtConfig.isSymmetricSigning()) {
            SecretKey secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
            parserBuilder.verifyWith(secretKey);
        } else {
            parserBuilder.verifyWith(keyManager.getPublicKey());
        }

        // Configure validation rules
        this.jwtParser = parserBuilder
                .requireIssuer(jwtConfig.getIssuer())
                .requireAudience(jwtConfig.getAudience())
                .clockSkewSeconds(jwtConfig.getClockSkewSeconds()) // ✅ renamed from setAllowedClockSkewSeconds
                .build();

        log.debug("JWT parser initialized with issuer: {} and audience: {}",
                jwtConfig.getIssuer(), jwtConfig.getAudience());
    }


    /**
     * Validate custom token claims
     */
    private void validateTokenClaims(Claims claims) {
        // Validate token type
        String tokenType = claims.get(JwtConstants.CLAIM_TOKEN_TYPE, String.class);
        if (tokenType == null) {
            throw new JwtAuthenticationException("Token type is missing");
        }

        // Validate user ID
        Long userId = claims.get(JwtConstants.CLAIM_USER_ID, Long.class);
        if (userId == null || userId <= 0) {
            throw new JwtAuthenticationException("Invalid user ID in token");
        }

        // Additional custom validations can be added here
    }

    /**
     * Extract permissions from user principal
     */
    private List<String> extractPermissions(UserPrincipal userPrincipal) {
        // This would typically come from a permission service or database
        // For now, derive basic permissions from role
        return userPrincipal.getRole().getPermissions().stream()
                .map(permission -> permission.name())
                .collect(Collectors.toList());
    }
}