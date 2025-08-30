package in.wealthinker.wealthinker.modules.auth.security.exceptions;

import java.time.Instant;

/**
 * Token Expired Exception - Specific exception for expired JWT tokens
 *
 * PURPOSE:
 * - Indicates JWT token has expired
 * - Provides expiration timestamp information
 * - Enables specific handling for expired tokens
 * - Supports automatic token refresh logic
 */
public class TokenExpiredException extends JwtAuthenticationException {

    private static final long serialVersionUID = 1L;

    private final Instant expiredAt;
    private final boolean canRefresh;

    /**
     * Constructor with basic expiration info
     */
    public TokenExpiredException(String message, Instant expiredAt) {
        super(message, "TOKEN_EXPIRED");
        this.expiredAt = expiredAt;
        this.canRefresh = true;
    }

    /**
     * Constructor with refresh capability flag
     */
    public TokenExpiredException(String message, Instant expiredAt, boolean canRefresh) {
        super(message, "TOKEN_EXPIRED");
        this.expiredAt = expiredAt;
        this.canRefresh = canRefresh;
    }

    /**
     * Constructor with cause
     */
    public TokenExpiredException(String message, Instant expiredAt, Throwable cause) {
        super(message, "TOKEN_EXPIRED", cause);
        this.expiredAt = expiredAt;
        this.canRefresh = true;
    }

    /**
     * Constructor with all parameters
     */
    public TokenExpiredException(String message, Instant expiredAt, boolean canRefresh, Throwable cause) {
        super(message, "TOKEN_EXPIRED", cause);
        this.expiredAt = expiredAt;
        this.canRefresh = canRefresh;
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public boolean canRefresh() {
        return canRefresh;
    }

    public long getExpiredSecondsAgo() {
        return Instant.now().getEpochSecond() - expiredAt.getEpochSecond();
    }

    @Override
    public String toString() {
        return "TokenExpiredException{" +
                "message='" + getMessage() + '\'' +
                ", expiredAt=" + expiredAt +
                ", canRefresh=" + canRefresh +
                ", expiredSecondsAgo=" + getExpiredSecondsAgo() +
                '}';
    }
}