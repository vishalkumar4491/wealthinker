package in.wealthinker.wealthinker.shared.constants;

public class JwtConstants {

    // Token Types
    public static final String ACCESS_TOKEN_TYPE = "ACCESS";
    public static final String REFRESH_TOKEN_TYPE = "REFRESH";
    public static final String REMEMBER_ME_TOKEN_TYPE = "REMEMBER_ME";

    // Token Expiration Times (in milliseconds)
    public static final long ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000L;        // 15 minutes
    public static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000L; // 7 days
    public static final long REMEMBER_ME_TOKEN_VALIDITY = 90 * 24 * 60 * 60 * 1000L; // 90 days

    // Token Claims
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_PERMISSIONS = "permissions";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";
    public static final String CLAIM_TOKEN_ID = "jti"; // JWT ID for revocation
    public static final String CLAIM_ISSUED_AT = "iat";
    public static final String CLAIM_EXPIRES_AT = "exp";
    public static final String CLAIM_NOT_BEFORE = "nbf";
    public static final String CLAIM_SUBJECT = "sub";
    public static final String CLAIM_ISSUER = "iss";
    public static final String CLAIM_AUDIENCE = "aud";

    // Security Headers
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";

    // Error Messages
    public static final String INVALID_TOKEN = "Invalid JWT token";
    public static final String EXPIRED_TOKEN = "JWT token has expired";
    public static final String UNSUPPORTED_TOKEN = "Unsupported JWT token";
    public static final String EMPTY_CLAIMS = "JWT claims string is empty";
    public static final String BLACKLISTED_TOKEN = "JWT token has been blacklisted";

    // Algorithm Types
    public static final String HMAC_SHA256 = "HS256";
    public static final String HMAC_SHA512 = "HS512";
    public static final String RSA_SHA256 = "RS256";
    public static final String RSA_SHA512 = "RS512";
    public static final String ECDSA_SHA256 = "ES256";

    // Default Configuration
    public static final String DEFAULT_ALGORITHM = HMAC_SHA256;
    public static final String DEFAULT_ISSUER = "wealthinker-platform";
    public static final String DEFAULT_AUDIENCE = "wealthinker-users";

    // Token Storage Keys (for blacklist/refresh token storage)
    public static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";
    public static final String REFRESH_TOKEN_KEY_PREFIX = "jwt:refresh:";
    public static final String ACCESS_TOKEN_KEY_PREFIX = "jwt:access:";

    // Rate Limiting
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final long LOCKOUT_DURATION = 15 * 60 * 1000L; // 15 minutes

    private JwtConstants() {
        // Utility class - prevent instantiation
    }
}
