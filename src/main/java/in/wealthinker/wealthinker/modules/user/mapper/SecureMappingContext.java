package in.wealthinker.wealthinker.modules.user.mapper;

public class SecureMappingContext {
    private final boolean includePhoneNumber;
    private final boolean includeSecurityInfo;
    private final boolean includeProfile;
    private final boolean includePreferences;

    public SecureMappingContext(boolean includePhoneNumber, boolean includeSecurityInfo,
                                boolean includeProfile, boolean includePreferences) {
        this.includePhoneNumber = includePhoneNumber;
        this.includeSecurityInfo = includeSecurityInfo;
        this.includeProfile = includeProfile;
        this.includePreferences = includePreferences;
    }

    public boolean isIncludePhoneNumber() { return includePhoneNumber; }
    public boolean isIncludeSecurityInfo() { return includeSecurityInfo; }
    public boolean isIncludeProfile() { return includeProfile; }
    public boolean isIncludePreferences() { return includePreferences; }
}
