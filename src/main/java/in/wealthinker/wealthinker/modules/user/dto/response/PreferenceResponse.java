package in.wealthinker.wealthinker.modules.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import in.wealthinker.wealthinker.modules.user.entity.UserPreference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceResponse {

    private Long id;
    private UserPreference.Theme theme;
    private String language;
    private String timezone;
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

    // Privacy & Data Sharing
    private Boolean profilePublic;
    private Boolean portfolioPublic;
    private Boolean showPerformance;
    private Boolean dataSharing;
    private Boolean analyticsTracking;

    // Security Preferences
    private Boolean twoFactorEnabled;
    private Boolean loginNotifications;
    private Integer sessionTimeoutMinutes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Computed fields for client convenience
    private Boolean hasMarketingConsent;
    private Boolean allowsDataCollection;
}