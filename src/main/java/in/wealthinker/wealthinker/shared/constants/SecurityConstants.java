package in.wealthinker.wealthinker.shared.constants;

public class SecurityConstants {
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String TOKEN_TYPE = "JWT";

    public static final String ROLE_PREFIX = "ROLE_";
    public static final String AUTHORITY_PREFIX = "SCOPE_";

    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 20;
    
    private SecurityConstants() {}
}
