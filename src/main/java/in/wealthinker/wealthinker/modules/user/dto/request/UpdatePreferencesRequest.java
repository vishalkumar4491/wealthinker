package in.wealthinker.wealthinker.modules.user.dto.request;

import in.wealthinker.wealthinker.modules.user.entity.UserPreference;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePreferencesRequest {

    private UserPreference.Theme theme;

    @Size(min = 2, max = 10, message = "Language code must be between 2 and 10 characters")
    @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "Language must be a valid ISO language code")
    private String language;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;

    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO currency code Like INR, USD")
    private String currency;
    private UserPreference.DateFormat dateFormat;
    private UserPreference.NumberFormat numberFormat;

    // Notification preferences
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean smsNotifications;
    private Boolean marketingEmails;

    // Financial Notifications
    private Boolean portfolioAlerts;
    private Boolean priceAlerts;
    private Boolean newsAlerts;
    private Boolean transactionAlerts;

    // Dashboard Customization
    private UserPreference.DashboardLayout dashboardLayout;
    private UserPreference.PortfolioView defaultPortfolioView;
    private UserPreference.ChartType chartType;
    private UserPreference.ChartTimeRange chartTimeRange;

    // Privacy preferences
    private Boolean profilePublic;
    private Boolean portfolioPublic;
    private Boolean showPerformance;
    private Boolean dataSharing;
    private Boolean analyticsTracking;

    // Security Preferences
    private Boolean loginNotifications;

    @Max(value = 480, message = "Session timeout cannot exceed 8 hours")
    private Integer sessionTimeoutMinutes;
}