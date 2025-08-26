package in.wealthinker.wealthinker.shared.constants;

public class AppConstants {
    // Pagination
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    // API Versioning
    public static final String API_V1_PREFIX = "/api/v1";
    public static final String AUTH_ENDPOINT = API_V1_PREFIX + "/auth";
    public static final String USER_ENDPOINT = API_V1_PREFIX + "/users";

    // Security
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int ACCOUNT_LOCK_DURATION_MINUTES = 30;

    // Business Rules
    public static final int PROFILE_COMPLETION_THRESHOLD = 80; // Percentage
    public static final int MAX_SESSIONS_PER_USER = 5; // Concurrent sessions

    private AppConstants() {}
}
