package in.wealthinker.wealthinker.shared.constants;


/**
 * Cache Configuration Constants
 *
 * WHY DIFFERENT TTL VALUES:
 *
 * Authentication Data (SHORT TTL):
 * - Changes frequently (login attempts, account status)
 * - Security-critical (can't serve stale data)
 * - High read volume (every request validates)
 *
 * Profile Data (MEDIUM TTL):
 * - Changes occasionally (user updates profile)
 * - Less critical if slightly stale
 * - Medium read volume (profile pages, dashboards)
 *
 * Preferences (LONG TTL):
 * - Rarely changes (UI settings, notifications)
 * - Safe to serve stale data
 * - Low read volume (app initialization)
 */

public class CacheConstants {

    // Cache Names
    public static final String USER_CACHE = "users";
    public static final String USER_PROFILE_CACHE = "userProfiles";
    public static final String USER_PREFERENCE_CACHE = "userPreferences";
    public static final String REFRESH_TOKEN_CACHE = "refreshTokens";

    // TTL Values (in seconds) TTL = Time To Live
    public static final int USER_CACHE_TTL = 3600;        // 1 hour - auth data changes frequently
    public static final int PROFILE_CACHE_TTL = 86400;    // 24 hours - profile rarely changes
    public static final int PREFERENCE_CACHE_TTL = 604800; // 7 days - settings very stable

    // Cache Sizes (number of entries)
    public static final int USER_CACHE_SIZE = 100_000;      // Support 100K active users in cache
    public static final int PROFILE_CACHE_SIZE = 50_000;    // Fewer profiles cached (less frequent access)
    public static final int PREFERENCE_CACHE_SIZE = 10_000; // Even fewer preferences (rare access)
    
    private CacheConstants() {}
}
