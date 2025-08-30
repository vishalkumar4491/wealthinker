package in.wealthinker.wealthinker.modules.auth.security.jwt;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * JWT Token Blacklist Interface
 *
 * PURPOSE:
 * - Provide token revocation capability for JWTs
 * - Support immediate logout functionality
 * - Enable security incident response (revoke compromised tokens)
 * - Maintain blacklist with automatic cleanup
 *
 * IMPLEMENTATION OPTIONS:
 * - Redis: Fast, distributed, automatic expiration
 * - Database: Persistent, but slower
 * - In-Memory: Fast, but single-server only
 */
public interface JwtTokenBlacklist {

    /**
     * Add token to blacklist
     */
    void blacklistToken(String tokenId, Date expirationDate);

    /**
     * Add token to blacklist with TTL
     */
    void blacklistToken(String tokenId, long ttl, TimeUnit timeUnit);

    /**
     * Check if token is blacklisted
     *
     * @param token Full JWT token (to extract token ID)
     * @return true if token is blacklisted
     */
    boolean isBlacklisted(String token);

    /**
     * Check if token ID is blacklisted
     */
    boolean isTokenIdBlacklisted(String tokenId);

    /**
     * Remove token from blacklist (for testing/admin use)
     */
    void removeFromBlacklist(String tokenId);

    /**
     * Get blacklist statistics
     *
     * @return Statistics about blacklisted tokens
     */
    BlacklistStats getStatistics();

    /**
     * Clean up expired blacklist entries
     *
     * @return Number of entries cleaned up
     */
    long cleanupExpiredEntries();
}
