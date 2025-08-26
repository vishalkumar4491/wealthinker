package in.wealthinker.wealthinker.modules.user.service;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdatePreferencesRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.PreferenceResponse;
import in.wealthinker.wealthinker.modules.user.entity.UserPreference;

import java.util.List;
import java.util.Map;

public interface PreferenceService {

    // =================== PREFERENCE RETRIEVAL ===================

    PreferenceResponse getUserPreferences(Long userId);

    Map<String, Object> getUIPreferences(Long userId);

    Map<String, Object> getNotificationPreferences(Long userId);

    // =================== PREFERENCE UPDATES ===================

    PreferenceResponse updatePreferences(Long userId, UpdatePreferencesRequest request);

    // =================== INDIVIDUAL PREFERENCE UPDATES ===================

    void updateTheme(Long userId, UserPreference.Theme theme);

    void updateLanguage(Long userId, String language);

    void updateTimezone(Long userId, String timezone);

    void updateCurrency(Long userId, String currency);

    // =================== NOTIFICATION PREFERENCES ===================

    void updateEmailNotifications(Long userId, Boolean enabled);

    void updatePushNotifications(Long userId, Boolean enabled);

    void updateSmsNotifications(Long userId, Boolean enabled);

    void updateMarketingEmails(Long userId, Boolean enabled);

    // =================== DASHBOARD CUSTOMIZATION ===================

    void updateDashboardLayout(Long userId, UserPreference.DashboardLayout layout);

    void updatePortfolioView(Long userId, UserPreference.PortfolioView view);

    void updateChartType(Long userId, UserPreference.ChartType chartType);

    // =================== PRIVACY SETTINGS ===================

    void updateProfileVisibility(Long userId, Boolean isPublic);

    void updateDataSharing(Long userId, Boolean enabled);

    // =================== UTILITY METHODS ===================

    List<String> getSupportedLanguages();

    List<String> getSupportedTimezones();

    List<String> getSupportedCurrencies();

    PreferenceResponse resetToDefaults(Long userId);

    Map<String, Object> getPreferenceStatistics();

    Map<String, Object> getMarketingConsentStatistics();

}
