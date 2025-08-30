package in.wealthinker.wealthinker.modules.auth.security.jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import in.wealthinker.wealthinker.config.JwtConfig;
import in.wealthinker.wealthinker.shared.constants.JwtConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis-based JWT Token Blacklist Implementation
 *
 * REDIS ADVANTAGES:
 * - Fast lookup performance (O(1) for key lookup)
 * - Automatic expiration (TTL) for cleanup
 * - Distributed across multiple servers
 * - Persistent across application restarts
 * - High availability with Redis Cluster
 *
 * KEY DESIGN:
 * - Key pattern: "jwt:blacklist:{tokenId}"
 * - Value: timestamp when blacklisted
 * - TTL: Set to remaining token lifetime
 *
 * PERFORMANCE:
 * - Sub-millisecond lookup times
 * - Minimal memory usage (keys auto-expire)
 * - No manual cleanup required
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisJwtTokenBlacklist implements JwtTokenBlacklist {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtConfig jwtConfig;

    /**
     * Add token to blacklist with automatic expiration
     *
     * REDIS STRATEGY:
     * - Store tokenId as key with timestamp as value
     * - Set TTL to match token's remaining lifetime
     * - After TTL expires, entry is automatically removed
     * - No manual cleanup required
     *
     * @param tokenId JWT token ID (jti claim)
     * @param expirationDate When token naturally expires
     */

    @Override
    public void blacklistToken(String tokenId, Date expirationDate) {
        try {
            // Calculate remaining token lifetime
            long currentTime = Instant.now().toEpochMilli();
            long expirationTime = expirationDate.getTime();
            long remainingLifetime = expirationTime - currentTime;

            if (remainingLifetime <= 0) {
                // Token already expired, no need to blacklist
                log.debug("Token {} is already expired, skipping blacklist", tokenId);
                return;
            }

            // Create Redis key
            String blacklistKey = buildBlacklistKey(tokenId);

            // Store in Redis with TTL
            redisTemplate.opsForValue().set(
                    blacklistKey,
                    currentTime, // Store blacklist timestamp
                    Duration.ofMillis(remainingLifetime)
            );

            log.info("Token blacklisted successfully: tokenId={}, ttlSeconds={}",
                    tokenId, remainingLifetime / 1000);

        } catch (Exception e) {
            log.error("Failed to blacklist token: {}", tokenId, e);
            throw new RuntimeException("Failed to blacklist token", e);
        }
    }

    /**
     * Add token to blacklist with specific TTL
     */
    @Override
    public void blacklistToken(String tokenId, long ttl, TimeUnit timeUnit) {
        try {
            String blacklistKey = buildBlacklistKey(tokenId);
            long currentTime = Instant.now().toEpochMilli();

            redisTemplate.opsForValue().set(
                    blacklistKey,
                    currentTime,
                    Duration.ofMillis(timeUnit.toMillis(ttl))
            );

            log.info("Token blacklisted with custom TTL: tokenId={}, ttl={} {}",
                    tokenId, ttl, timeUnit);

        } catch (Exception e) {
            log.error("Failed to blacklist token with TTL: {}", tokenId, e);
            throw new RuntimeException("Failed to blacklist token", e);
        }
    }

    /**
     * Check if JWT token is blacklisted
     *
     * PERFORMANCE OPTIMIZATION:
     * - Extract token ID only if blacklist is enabled
     * - Single Redis lookup operation
     * - Return false immediately if Redis is unavailable (fail open)
     *
     * @param token Complete JWT token
     * @return true if token is blacklisted
     */
    @Override
    public boolean isBlacklisted(String tokenId) {
        // Quick exit if blacklisting is disabled
        if (!jwtConfig.getBlacklistEnabled()) {
            return false;
        }

        try {
            String blacklistKey = buildBlacklistKey(tokenId);
            Boolean exists = redisTemplate.hasKey(blacklistKey);
            boolean isBlacklisted = Boolean.TRUE.equals(exists);

            if (isBlacklisted) {
                log.debug("Token found in blacklist: {}", tokenId);
            }

            return isBlacklisted;

        } catch (Exception e) {
            log.warn("Error checking token blacklist status, treating as blacklisted", e);
            return true; // Fail secure - treat errors as blacklisted
        }
    }

    /**
     * Check if token ID is blacklisted
     *
     * REDIS OPERATION:
     * - Single EXISTS command for O(1) performance
     * - Returns false if key doesn't exist (not blacklisted)
     * - Returns true if key exists (blacklisted)
     */
    @Override
    public boolean isTokenIdBlacklisted(String tokenId) {
        try {
            String blacklistKey = buildBlacklistKey(tokenId);
            Boolean exists = redisTemplate.hasKey(blacklistKey);

            boolean isBlacklisted = Boolean.TRUE.equals(exists);

            if (isBlacklisted) {
                log.debug("Token found in blacklist: {}", tokenId);
            }

            return isBlacklisted;

        } catch (Exception e) {
            log.warn("Error checking token ID blacklist status: {}", tokenId, e);
            return true; // Fail secure
        }
    }

    /**
     * Remove token from blacklist
     *
     * USE CASES:
     * - Testing and development
     * - Administrative token restoration
     * - Error correction
     */
    @Override
    public void removeFromBlacklist(String tokenId) {
        try {
            String blacklistKey = buildBlacklistKey(tokenId);
            Boolean deleted = redisTemplate.delete(blacklistKey);

            if (Boolean.TRUE.equals(deleted)) {
                log.info("Token removed from blacklist: {}", tokenId);
            } else {
                log.warn("Token not found in blacklist for removal: {}", tokenId);
            }

        } catch (Exception e) {
            log.error("Failed to remove token from blacklist: {}", tokenId, e);
            throw new RuntimeException("Failed to remove token from blacklist", e);
        }
    }

    /**
     * Get blacklist statistics
     *
     * REDIS OPERATIONS:
     * - SCAN command to find all blacklist keys
     * - Count matching keys for statistics
     * - Efficient pattern matching
     */
    @Override
    public BlacklistStats getStatistics() {
        try {
            String pattern = JwtConstants.BLACKLIST_KEY_PREFIX + "*";
            Set<String> blacklistKeys = redisTemplate.keys(pattern);

            long totalBlacklistedTokens = blacklistKeys != null ? blacklistKeys.size() : 0;

            return BlacklistStats.builder()
                    .totalBlacklistedTokens(totalBlacklistedTokens)
                    .storageType("Redis")
                    .lastUpdated(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to get blacklist statistics", e);
            return BlacklistStats.builder()
                    .totalBlacklistedTokens(-1) // Indicate error
                    .storageType("Redis")
                    .lastUpdated(Instant.now())
                    .build();
        }
    }

    /**
     * Clean up expired entries
     *
     * NOTE: Redis automatically removes expired keys
     * This method is a no-op for Redis implementation
     */
    @Override
    public long cleanupExpiredEntries() {
        // Redis automatically handles TTL expiration
        log.debug("Redis automatically handles expired key cleanup");
        return 0;
    }

    /**
     * Build Redis key for blacklisted token
     *
     * KEY PATTERN: "jwt:blacklist:{tokenId}"
     *
     * @param tokenId JWT token identifier
     * @return Redis key
     */
    private String buildBlacklistKey(String tokenId) {
        return JwtConstants.BLACKLIST_KEY_PREFIX + tokenId;
    }
}
