package in.wealthinker.wealthinker.modules.user.dto.request;

import in.wealthinker.wealthinker.modules.user.entity.UserPreference;
import lombok.Data;

@Data
public class UpdatePreferencesRequest {

    private UserPreference.Theme theme;
    private String language;
    private String timezone;
    private String currency;
    private String dateFormat;

    // Notification preferences
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean smsNotifications;
    private Boolean marketingEmails;
    private Boolean portfolioAlerts;
    private Boolean priceAlerts;
    private Boolean newsAlerts;

    // Dashboard preferences
    private String dashboardLayout;
    private String defaultPortfolioView;
    private String chartType;

    // Privacy preferences
    private Boolean profilePublic;
    private Boolean portfolioPublic;
    private Boolean showPerformance;
    private Boolean dataSharing;
}