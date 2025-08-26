package in.wealthinker.wealthinker.shared.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Permission Enum - Granular permissions for authorization
 *
 * PURPOSE:
 * - Define granular permissions for different resources
 * - Support fine-grained access control
 * - Enable role-based permission assignment
 * - Facilitate permission-based authorization
 */

@Getter
public enum Permission {

    // =================== USER MANAGEMENT PERMISSIONS ===================
    USERS_CREATE("Create new users"),
    USERS_READ("View user information"),
    USERS_UPDATE("Update user information"),
    USERS_DELETE("Delete users"),
    USERS_LIST("List all users"),
    USERS_SEARCH("Search users"),

    // =================== PROFILE MANAGEMENT PERMISSIONS ===================
    PROFILES_CREATE("Create user profiles"),
    PROFILES_READ("View user profiles"),
    PROFILES_UPDATE("Update user profiles"),
    PROFILES_DELETE("Delete user profiles"),
    PROFILES_KYC_APPROVE("Approve KYC documents"),
    PROFILES_KYC_REJECT("Reject KYC documents"),

    // =================== PORTFOLIO MANAGEMENT PERMISSIONS ===================
    PORTFOLIOS_CREATE("Create portfolios"),
    PORTFOLIOS_READ("View portfolio information"),
    PORTFOLIOS_UPDATE("Update portfolio information"),
    PORTFOLIOS_DELETE("Delete portfolios"),
    PORTFOLIOS_SHARE("Share portfolio information"),
    PORTFOLIOS_ANALYZE("Analyze portfolio performance"),


    // =================== TRANSACTION PERMISSIONS ===================
    TRANSACTIONS_CREATE("Create transactions"),
    TRANSACTIONS_READ("View transactions"),
    TRANSACTIONS_UPDATE("Update transactions"),
    TRANSACTIONS_DELETE("Delete transactions"),
    TRANSACTIONS_IMPORT("Import transactions"),
    TRANSACTIONS_EXPORT("Export transactions"),

    // =================== FINANCIAL DATA PERMISSIONS ===================
    MARKET_DATA_READ("Access market data"),
    MARKET_DATA_REAL_TIME("Access real-time market data"),
    FINANCIAL_REPORTS_READ("View financial reports"),
    FINANCIAL_REPORTS_GENERATE("Generate financial reports"),
    FINANCIAL_REPORTS_EXPORT("Export financial reports"),

    // =================== SYSTEM ADMINISTRATION PERMISSIONS ===================
    SYSTEM_CONFIG("System configuration access"),
    SYSTEM_MONITORING("System monitoring access"),
    SYSTEM_LOGS("Access system logs"),
    SYSTEM_BACKUP("Perform system backups"),
    SYSTEM_MAINTENANCE("System maintenance operations"),

    // =================== AUDIT AND COMPLIANCE PERMISSIONS ===================
    AUDIT_LOGS_READ("View audit logs"),
    AUDIT_REPORTS_GENERATE("Generate audit reports"),
    COMPLIANCE_REPORTS("Access compliance reports"),
    DATA_EXPORT("Export user data for GDPR"),

    // =================== API PERMISSIONS ===================
    API_ACCESS("Access API endpoints"),
    API_RATE_LIMIT_BYPASS("Bypass API rate limits"),
    API_ADMIN("API administration access"),

    // =================== NOTIFICATION PERMISSIONS ===================
    NOTIFICATIONS_SEND("Send notifications"),
    NOTIFICATIONS_BROADCAST("Broadcast notifications"),
    NOTIFICATIONS_MANAGE("Manage notification templates");

    private final String description;

    Permission(String description) {
        this.description = description;
    }

    /**
     * Get all permissions as strings
     */
    public static List<String> getAllPermissions() {
        return Arrays.stream(Permission.values())
                .map(Permission::name)
                .collect(Collectors.toList());
    }

    /**
     * Get permissions by category
     */
    public static List<Permission> getPermissionsByCategory(String category) {
        return Arrays.stream(Permission.values())
                .filter(permission -> permission.name().startsWith(category.toUpperCase()))
                .collect(Collectors.toList());
    }
}
