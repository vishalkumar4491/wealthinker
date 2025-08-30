package in.wealthinker.wealthinker.config;

import in.wealthinker.wealthinker.shared.constants.JwtConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * JWT Configuration Properties
 *
 * PURPOSE:
 * - Externalize JWT configuration for different environments
 * - Provide type-safe configuration with validation
 * - Support both symmetric and asymmetric signing
 * - Enable easy configuration changes without code modifications
 *
 * SECURITY BEST PRACTICES:
 * - Secret keys should never be hardcoded
 * - Different keys for different environments
 * - Support key rotation strategies
 * - Validate configuration on startup
 */

@Configuration
@ConfigurationProperties(prefix = "wealthinker.jwt")
@Data
@Validated
public class JwtConfig {

    /**
     * JWT Secret Key for symmetric signing (HMAC)
     *
     * SECURITY: Must be at least 256 bits (32 characters) for HS256
     * PRODUCTION: Should be generated using cryptographically secure random generator
     * EXAMPLE: openssl rand -base64 32
     */
    @NotBlank(message = "JWT secret cannot be blank")
    private String secret = "defaultSecretKeyForDevelopmentOnlyPleaseChangeInProduction";

    /**
     * JWT Algorithm for token signing
     *
     * SUPPORTED: HS256, HS512, RS256, RS512, ES256
     * DEFAULT: HS256 (good balance of security and performance)
     */
    @NotBlank(message = "JWT algorithm cannot be blank")
    private String algorithm = JwtConstants.DEFAULT_ALGORITHM;

    /**
     * Access Token Expiration (in milliseconds)
     *
     * SECURITY BEST PRACTICE: Short-lived tokens (15-30 minutes)
     * REASON: Limits exposure if token is compromised
     */
    @Positive(message = "Access token expiration must be positive")
    private Long accessTokenExpiration = JwtConstants.ACCESS_TOKEN_VALIDITY;

    /**
     * Refresh Token Expiration (in milliseconds)
     *
     * BALANCE: Long enough for good UX, short enough for security
     * TYPICAL: 7-30 days depending on application sensitivity
     */
    @Positive(message = "Refresh token expiration must be positive")
    private Long refreshTokenExpiration = JwtConstants.REFRESH_TOKEN_VALIDITY;

    /**
     * Remember Me Token Expiration (in milliseconds)
     *
     * EXTENDED SESSIONS: For "remember me" functionality
     * COMPLIANCE: Should respect data retention policies (GDPR)
     */
    @Positive(message = "Remember me token expiration must be positive")
    private Long rememberMeTokenExpiration = JwtConstants.REMEMBER_ME_TOKEN_VALIDITY;

    /**
     * Token Issuer
     *
     * PURPOSE: Identifies who issued the token
     * SECURITY: Helps prevent token misuse across different applications
     */
    @NotBlank(message = "JWT issuer cannot be blank")
    private String issuer = JwtConstants.DEFAULT_ISSUER;

    /**
     * Token Audience
     *
     * PURPOSE: Identifies who the token is intended for
     * SECURITY: Prevents tokens from being used in wrong context
     */
    @NotBlank(message = "JWT audience cannot be blank")
    private String audience = JwtConstants.DEFAULT_AUDIENCE;

    /**
     * Enable/Disable JWT token blacklisting
     *
     * PERFORMANCE: Adds overhead but increases security
     * USE CASE: Required for logout functionality and token revocation
     */
    @NotNull(message = "Blacklist enabled flag cannot be null")
    private Boolean blacklistEnabled = true;

    /**
     * RSA Private Key Path (for asymmetric signing)
     *
     * SECURITY: Keep private keys secure and encrypted
     * FORMAT: PEM format expected
     */
    private String privateKeyPath;

    /**
     * RSA Public Key Path (for asymmetric signing)
     *
     * DISTRIBUTION: Public key can be shared for token verification
     * FORMAT: PEM format expected
     */
    private String publicKeyPath;

    /**
     * Key Store Configuration (alternative to individual key files)
     */
    private String keyStorePath;
    private String keyStorePassword;
    private String keyAlias;

    /**
     * Clock Skew Tolerance (in seconds)
     *
     * PURPOSE: Account for time differences between servers
     * RECOMMENDED: 60-120 seconds for distributed systems
     */
    @Positive(message = "Clock skew must be positive")
    private Integer clockSkewSeconds = 60;

    /**
     * Enable/Disable token refresh on each request
     *
     * SECURITY: Rotating tokens increase security
     * PERFORMANCE: May impact performance under high load
     */
    private Boolean refreshOnEachRequest = false;

    /**
     * Maximum number of refresh tokens per user
     *
     * PURPOSE: Limit the number of active sessions per user
     * SECURITY: Prevents unlimited token generation
     */
    @Positive(message = "Max refresh tokens must be positive")
    private Integer maxRefreshTokensPerUser = 5;

    // Helper Methods

    /**
     * Check if asymmetric signing is configured
     */
    public boolean isAsymmetricSigning() {
        return algorithm.startsWith("RS") || algorithm.startsWith("ES");
    }

    /**
     * Check if symmetric signing is configured
     */
    public boolean isSymmetricSigning() {
        return algorithm.startsWith("HS");
    }

    /**
     * Get token expiration based on token type
     */
    public Long getExpirationForTokenType(String tokenType) {
        return switch (tokenType) {
            case JwtConstants.ACCESS_TOKEN_TYPE -> accessTokenExpiration;
            case JwtConstants.REFRESH_TOKEN_TYPE -> refreshTokenExpiration;
            case JwtConstants.REMEMBER_ME_TOKEN_TYPE -> rememberMeTokenExpiration;
            default -> accessTokenExpiration;
        };
    }

    /**
     * Validate configuration on startup
     */
    public void validate() {
        if (isSymmetricSigning() && (secret == null || secret.length() < 32)) {
            throw new IllegalStateException("JWT secret must be at least 32 characters for symmetric signing");
        }

        if (isAsymmetricSigning() && (privateKeyPath == null || publicKeyPath == null)) {
            throw new IllegalStateException("Private and public key paths must be configured for asymmetric signing");
        }

        if (accessTokenExpiration >= refreshTokenExpiration) {
            throw new IllegalStateException("Access token expiration must be less than refresh token expiration");
        }
    }

}
