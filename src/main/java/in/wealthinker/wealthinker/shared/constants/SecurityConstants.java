package in.wealthinker.wealthinker.shared.constants;

public class SecurityConstants {

    // JWT Constants
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String TOKEN_TYPE = "JWT";
    public static final String AUTHORITIES_KEY = "auth";

    public static final String ROLE_PREFIX = "ROLE_";
    public static final String AUTHORITY_PREFIX = "SCOPE_";

    // Session Constants
    public static final int MAX_SESSIONS_PER_USER = 5;
    public static final int SESSION_TIMEOUT_HOURS = 24;
    public static final int INACTIVE_SESSION_TIMEOUT_MINUTES = 30;

    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 20;

    // Security Headers
    public static final String USER_ID_HEADER = "X-User-ID";
    public static final String SESSION_ID_HEADER = "X-Session-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    // Rate Limiting
    public static final int LOGIN_ATTEMPTS_LIMIT = 5;
    public static final int PASSWORD_RESET_LIMIT = 3;
    public static final int EMAIL_VERIFICATION_LIMIT = 5;
    
    private SecurityConstants() {}
}
