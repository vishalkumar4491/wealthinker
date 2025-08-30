package in.wealthinker.wealthinker.modules.auth.security.exceptions;

import java.time.Instant;

/**
 * Token Blacklisted Exception - Exception for blacklisted JWT tokens
 *
 * PURPOSE:
 * - Indicates JWT token has been revoked/blacklisted
 * - Provides blacklist timestamp information
 * - Prevents use of explicitly revoked tokens
 * - Supports security incident response
 */
public class TokenBlacklistedException extends JwtAuthenticationException {

    private static final long serialVersionUID = 1L;

    private final String tokenId;
    private final Instant blacklistedAt;
    private final String reason;

    /**
     * Constructor with basic blacklist info
     */
    public TokenBlacklistedException(String message) {
        super(message, "TOKEN_BLACKLISTED");
        this.tokenId = null;
        this.blacklistedAt = Instant.now();
        this.reason = null;
    }

    /**
     * Constructor with token ID
     */
    public TokenBlacklistedException(String message, String tokenId) {
        super(message, "TOKEN_BLACKLISTED");
        this.tokenId = tokenId;
        this.blacklistedAt = Instant.now();
        this.reason = null;
    }

    /**
     * Constructor with token ID and timestamp
     */
    public TokenBlacklistedException(String message, String tokenId, Instant blacklistedAt) {
        super(message, "TOKEN_BLACKLISTED");
        this.tokenId = tokenId;
        this.blacklistedAt = blacklistedAt;
        this.reason = null;
    }

    /**
     * Constructor with all parameters
     */
    public TokenBlacklistedException(String message, String tokenId, Instant blacklistedAt, String reason) {
        super(message, "TOKEN_BLACKLISTED");
        this.tokenId = tokenId;
        this.blacklistedAt = blacklistedAt;
        this.reason = reason;
    }

    /**
     * Constructor with cause
     */
    public TokenBlacklistedException(String message, Throwable cause) {
        super(message, "TOKEN_BLACKLISTED", cause);
        this.tokenId = null;
        this.blacklistedAt = Instant.now();
        this.reason = null;
    }

    public String getTokenId() {
        return tokenId;
    }

    public Instant getBlacklistedAt() {
        return blacklistedAt;
    }

    public String getReason() {
        return reason;
    }

    public long getBlacklistedSecondsAgo() {
        return Instant.now().getEpochSecond() - blacklistedAt.getEpochSecond();
    }

    @Override
    public String toString() {
        return "TokenBlacklistedException{" +
                "message='" + getMessage() + '\'' +
                ", tokenId='" + tokenId + '\'' +
                ", blacklistedAt=" + blacklistedAt +
                ", reason='" + reason + '\'' +
                ", blacklistedSecondsAgo=" + getBlacklistedSecondsAgo() +
                '}';
    }
}