package in.wealthinker.wealthinker.shared.enums;

public enum UserRole {
    FREE("Free User", "Basic access to platform features"),
    PREMIUM("Premium User", "Enhanced features and analytics"),
    PRO("Pro User", "Full access to all platform features"),
    ADMIN("Administrator", "Administrative access to platform"),
    SUPER_ADMIN("Super Administrator", "Full system access");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
