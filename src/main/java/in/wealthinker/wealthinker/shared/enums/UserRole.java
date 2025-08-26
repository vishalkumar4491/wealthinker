package in.wealthinker.wealthinker.shared.enums;

import lombok.Getter;

import java.util.List;
import java.util.Set;

/**
 * User Role Enum - Updated with Permission Mapping
 */
@Getter
public enum UserRole {

    // End User Roles
    FREE("Free User", "Basic portfolio tracking", 1, Set.of(
            Permission.PORTFOLIOS_CREATE,
            Permission.PORTFOLIOS_READ,
            Permission.PORTFOLIOS_UPDATE,
            Permission.PORTFOLIOS_DELETE,
            Permission.PORTFOLIOS_SHARE,
            Permission.PORTFOLIOS_ANALYZE,
            Permission.TRANSACTIONS_CREATE,
            Permission.TRANSACTIONS_READ,
            Permission.MARKET_DATA_READ,
            Permission.PROFILES_READ,
            Permission.PROFILES_UPDATE
    )),

    PREMIUM("Premium User", "Advanced analytics + alerts", 2, Set.of(
            Permission.PORTFOLIOS_CREATE,
            Permission.PORTFOLIOS_READ,
            Permission.PORTFOLIOS_UPDATE,
            Permission.PORTFOLIOS_DELETE,
            Permission.PORTFOLIOS_SHARE,
            Permission.PORTFOLIOS_ANALYZE,
            Permission.TRANSACTIONS_CREATE,
            Permission.TRANSACTIONS_READ,
            Permission.TRANSACTIONS_UPDATE,
            Permission.TRANSACTIONS_IMPORT,
            Permission.MARKET_DATA_READ,
            Permission.MARKET_DATA_REAL_TIME,
            Permission.FINANCIAL_REPORTS_READ,
            Permission.PROFILES_READ,
            Permission.PROFILES_UPDATE
    )),

    PRO("Pro User", "Full feature access + API", 3, Set.of(
            Permission.PORTFOLIOS_CREATE,
            Permission.PORTFOLIOS_READ,
            Permission.PORTFOLIOS_UPDATE,
            Permission.PORTFOLIOS_DELETE,
            Permission.PORTFOLIOS_SHARE,
            Permission.PORTFOLIOS_ANALYZE,
            Permission.TRANSACTIONS_CREATE,
            Permission.TRANSACTIONS_READ,
            Permission.TRANSACTIONS_UPDATE,
            Permission.TRANSACTIONS_DELETE,
            Permission.TRANSACTIONS_IMPORT,
            Permission.TRANSACTIONS_EXPORT,
            Permission.MARKET_DATA_READ,
            Permission.MARKET_DATA_REAL_TIME,
            Permission.FINANCIAL_REPORTS_READ,
            Permission.FINANCIAL_REPORTS_GENERATE,
            Permission.FINANCIAL_REPORTS_EXPORT,
            Permission.API_ACCESS,
            Permission.PROFILES_READ,
            Permission.PROFILES_UPDATE
    )),

    // Internal Staff Roles
    SUPPORT("Support Agent", "Customer support access", 10, Set.of(
            Permission.USERS_READ,
            Permission.USERS_SEARCH,
            Permission.PROFILES_READ,
            Permission.PORTFOLIOS_READ,
            Permission.TRANSACTIONS_READ,
            Permission.AUDIT_LOGS_READ
    )),

    ANALYST("Financial Analyst", "Research and analysis tools", 15, Set.of(
            Permission.USERS_READ,
            Permission.USERS_SEARCH,
            Permission.PROFILES_READ,
            Permission.PORTFOLIOS_READ,
            Permission.PORTFOLIOS_ANALYZE,
            Permission.TRANSACTIONS_READ,
            Permission.MARKET_DATA_READ,
            Permission.MARKET_DATA_REAL_TIME,
            Permission.FINANCIAL_REPORTS_READ,
            Permission.FINANCIAL_REPORTS_GENERATE,
            Permission.AUDIT_LOGS_READ
    )),

    ADMIN("Administrator", "Administrative access", 20, Set.of(
            Permission.USERS_CREATE,
            Permission.USERS_READ,
            Permission.USERS_UPDATE,
            Permission.USERS_DELETE,
            Permission.USERS_LIST,
            Permission.USERS_SEARCH,
            Permission.PROFILES_CREATE,
            Permission.PROFILES_READ,
            Permission.PROFILES_UPDATE,
            Permission.PROFILES_DELETE,
            Permission.PROFILES_KYC_APPROVE,
            Permission.PROFILES_KYC_REJECT,
            Permission.PORTFOLIOS_CREATE,
            Permission.PORTFOLIOS_READ,
            Permission.PORTFOLIOS_UPDATE,
            Permission.PORTFOLIOS_DELETE,
            Permission.PORTFOLIOS_ANALYZE,
            Permission.TRANSACTIONS_CREATE,
            Permission.TRANSACTIONS_READ,
            Permission.TRANSACTIONS_UPDATE,
            Permission.TRANSACTIONS_DELETE,
            Permission.MARKET_DATA_READ,
            Permission.MARKET_DATA_REAL_TIME,
            Permission.FINANCIAL_REPORTS_READ,
            Permission.FINANCIAL_REPORTS_GENERATE,
            Permission.FINANCIAL_REPORTS_EXPORT,
            Permission.AUDIT_LOGS_READ,
            Permission.AUDIT_REPORTS_GENERATE,
            Permission.COMPLIANCE_REPORTS,
            Permission.NOTIFICATIONS_SEND,
            Permission.NOTIFICATIONS_MANAGE,
            Permission.API_ADMIN
    )),

    SUPER_ADMIN("Super Administrator", "Full system access", 25, Set.of(
            // Include all permissions
            Permission.values()
    ));

    private final String displayName;
    private final String description;
    private final int hierarchyLevel;
    private final Set<Permission> permissions;

    UserRole(String displayName, String description, int hierarchyLevel, Set<Permission> permissions) {
        this.displayName = displayName;
        this.description = description;
        this.hierarchyLevel = hierarchyLevel;
        this.permissions = permissions;
    }

    public String getAuthority() {
        return "ROLE_" + this.name();
    }

    public boolean hasPermissionLevel(UserRole other) {
        return this.hierarchyLevel >= other.hierarchyLevel;
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public List<String> getPermissionNames() {
        return permissions.stream()
                .map(Permission::name)
                .toList();
    }
}
