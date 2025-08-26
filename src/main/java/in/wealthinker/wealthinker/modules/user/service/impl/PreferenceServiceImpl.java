package in.wealthinker.wealthinker.modules.user.service.impl;

import in.wealthinker.wealthinker.modules.user.repository.UserPreferenceRepository;
import in.wealthinker.wealthinker.shared.exceptions.BusinessException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdatePreferencesRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.PreferenceResponse;
import in.wealthinker.wealthinker.modules.user.entity.User;
import in.wealthinker.wealthinker.modules.user.entity.UserPreference;
import in.wealthinker.wealthinker.modules.user.mapper.PreferenceMapper;
import in.wealthinker.wealthinker.modules.user.repository.UserRepository;
import in.wealthinker.wealthinker.modules.user.service.PreferenceService;
import in.wealthinker.wealthinker.shared.constants.CacheConstants;
import in.wealthinker.wealthinker.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User Preference Service Implementation
 *
 * RESPONSIBILITIES:
 * - User settings and customization management
 * - UI/UX preferences (theme, language, timezone)
 * - Notification preferences management
 * - Privacy and data sharing settings
 * - Dashboard customization
 * - GDPR compliance for data sharing consents
 */

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PreferenceServiceImpl implements PreferenceService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final PreferenceMapper userPreferenceMapper;
    private final ApplicationEventPublisher eventPublisher;

    // Supported options
    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList(
            "en", "es", "fr", "de", "it", "pt", "ja", "ko", "zh", "hi", "ar"
    );

    private static final List<String> SUPPORTED_CURRENCIES = Arrays.asList(
            "USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "INR", "CNY", "BRL"
    );

    // =================== PREFERENCE RETRIEVAL ===================

    @Override
    @Cacheable(value = CacheConstants.USER_PREFERENCE_CACHE, key = "#userId", unless = "#result == null")
    public PreferenceResponse getUserPreferences(Long userId) {
        log.debug("Getting preferences for user: {}", userId);

        UserPreference preferences = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        return userPreferenceMapper.toResponse(preferences);
    }

    @Override
    @Cacheable(value = CacheConstants.USER_PREFERENCE_CACHE, key = "'ui_' + #userId", unless = "#result.isEmpty()")
    public Map<String, Object> getUIPreferences(Long userId) {
        log.debug("Getting UI preferences for user: {}", userId);

        UserPreference preferences = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        Map<String, Object> uiPrefs = new HashMap<>();
        uiPrefs.put("theme", preferences.getTheme());
        uiPrefs.put("language", preferences.getLanguage());
        uiPrefs.put("timezone", preferences.getTimezone());
        uiPrefs.put("currency", preferences.getCurrency());
        uiPrefs.put("dateFormat", preferences.getDateFormat());
        uiPrefs.put("numberFormat", preferences.getNumberFormat());
        uiPrefs.put("dashboardLayout", preferences.getDashboardLayout());
        uiPrefs.put("defaultPortfolioView", preferences.getDefaultPortfolioView());
        uiPrefs.put("chartType", preferences.getChartType());
        uiPrefs.put("chartTimeRange", preferences.getChartTimeRange());

        return uiPrefs;
    }

    @Override
    @Cacheable(value = CacheConstants.USER_PREFERENCE_CACHE, key = "'notifications_' + #userId", unless = "#result.isEmpty()")
    public Map<String, Object> getNotificationPreferences(Long userId) {
        log.debug("Getting notification preferences for user: {}", userId);

        UserPreference preferences = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        Map<String, Object> notificationPrefs = new HashMap<>();
        notificationPrefs.put("emailNotifications", preferences.getEmailNotifications());
        notificationPrefs.put("pushNotifications", preferences.getPushNotifications());
        notificationPrefs.put("smsNotifications", preferences.getSmsNotifications());
        notificationPrefs.put("marketingEmails", preferences.getMarketingEmails());
        notificationPrefs.put("portfolioAlerts", preferences.getPortfolioAlerts());
        notificationPrefs.put("priceAlerts", preferences.getPriceAlerts());
        notificationPrefs.put("newsAlerts", preferences.getNewsAlerts());
        notificationPrefs.put("transactionAlerts", preferences.getTransactionAlerts());
        notificationPrefs.put("loginNotifications", preferences.getLoginNotifications());

        return notificationPrefs;
    }

    // =================== PREFERENCE UPDATES ===================

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_PREFERENCE_CACHE, allEntries = true)
    public PreferenceResponse updatePreferences(Long userId, UpdatePreferencesRequest request) {
        log.info("Updating preferences for user: {}", userId);

        UserPreference preferences = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        // Update preferences using mapper
        userPreferenceMapper.updateFromRequest(request, preferences);

        // Validate preferences
        validatePreferences(preferences);

        // Save changes
        preferences = userPreferenceRepository.save(preferences);

        // Log important preference changes
        logPreferenceChanges(userId, request);

        // Publish preferences updated event
        publishPreferencesUpdatedEvent(preferences);

        log.info("Preferences updated successfully for user: {}", userId);
        return userPreferenceMapper.toResponse(preferences);
    }

    // =================== INDIVIDUAL PREFERENCE UPDATES ===================

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_PREFERENCE_CACHE, allEntries = true)
    public void updateTheme(Long userId, UserPreference.Theme theme) {
        log.debug("Updating theme for user: {} to {}", userId, theme);

        UserPreference preferences = getOrCreatePreferences(userId);
        preferences.setTheme(theme);
        userPreferenceRepository.save(preferences);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_PREFERENCE_CACHE, allEntries = true)
    public void updateLanguage(Long userId, String language) {
        log.debug("Updating language for user: {} to {}", userId, language);

        validateLanguage(language);

        UserPreference preferences = getOrCreatePreferences(userId);
        preferences.setLanguage(language);
        userPreferenceRepository.save(preferences);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_PREFERENCE_CACHE, allEntries = true)
    public void updateTimezone(Long userId, String timezone) {
        log.debug("Updating timezone for user: {} to {}", userId, timezone);

        validateTimezone(timezone);

        UserPreference preferences = getOrCreatePreferences(userId);
        preferences.setTimezone(timezone);
        userPreferenceRepository.save(preferences);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_PREFERENCE_CACHE, allEntries = true)
    public void updateCurrency(Long userId, String currency) {
        log.debug("Updating currency for user: {} to {}", userId, currency);

        validateCurrency(currency);

        UserPreference preferences = getOrCreatePreferences(userId);
        preferences.setCurrency(currency);
        userPreferenceRepository.save(preferences);
    }

    // =================== NOTIFICATION PREFERENCES ===================

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_PREFERENCE_CACHE, allEntries = true)
    public void updateEmailNotifications(Long userId, Boolean enabled) {
        log.debug("Updating email notifications for user: {} to {}", userId, enabled);

        UserPreference preferences = getOrCreatePreferences(userId);
        preferences.setEmailNotifications(enabled);
        userPreferenceRepository.save(preferences);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_PREFERENCE_CACHE, allEntries = true)
    public void updatePushNotifications(Long userId, Boolean enabled) {
        log.debug("Updating push notifications for user: {} to {}", userId, enabled);

        UserPreference preferences = getOrCreatePreferences(userId);
        preferences.setPushNotifications(enabled);
        userPreferenceRepository.save(preferences);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_PREFERENCE_CACHE, allEntries = true)
    public void updateSmsNotifications(Long userId, Boolean enabled) {
        log.debug("Updating SMS notifications for user: {} to {}", userId, enabled);

        UserPreference preferences = getOrCreatePreferences(userId);
        preferences.setSmsNotifications(enabled);
        userPreferenceRepository.save(preferences);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_PREFERENCE_CACHE, allEntries = true)
    public void updateMarketingEmails(Long userId, Boolean enabled) {
        log.info("Updating marketing emails for user: {} to {} (GDPR consent)", userId, enabled);

        UserPreference preferences = getOrCreatePreferences(userId);
        preferences.setMarketingEmails(enabled);
        userPreferenceRepository.save(preferences);

        // Log marketing consent change for GDPR compliance
        log.warn("Marketing consent changed for user: {} to: {} at: {}",
                userId, enabled, java.time.LocalDateTime.now());
    }

    // =================== DASHBOARD CUSTOMIZATION ===================

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_PREFERENCE_CACHE, allEntries = true)
    public void updateDashboardLayout(Long userId, UserPreference.DashboardLayout layout) {
        log.debug("Updating dashboard layout for user: {} to {}", userId, layout);

        UserPreference preferences = getOrCreatePreferences(userId);
        preferences.setDashboardLayout(layout);
        userPreferenceRepository.save(preferences);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_PREFERENCE_CACHE, allEntries = true)
    public void updatePortfolioView(Long userId, UserPreference.PortfolioView view) {
        log.debug("Updating portfolio view for user: {} to {}", userId, view);

        UserPreference preferences = getOrCreatePreferences(userId);
        preferences.setDefaultPortfolioView(view);
        userPreferenceRepository.save(preferences);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_PREFERENCE_CACHE, allEntries = true)
    public void updateChartType(Long userId, UserPreference.ChartType chartType) {
        log.debug("Updating chart type for user: {} to {}", userId, chartType);

        UserPreference preferences = getOrCreatePreferences(userId);
        preferences.setChartType(chartType);
        userPreferenceRepository.save(preferences);
    }

    // =================== PRIVACY SETTINGS ===================

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_PREFERENCE_CACHE, allEntries = true)
    public void updateProfileVisibility(Long userId, Boolean isPublic) {
        log.info("Updating profile visibility for user: {} to public: {}", userId, isPublic);

        UserPreference preferences = getOrCreatePreferences(userId);
        preferences.setProfilePublic(isPublic);
        userPreferenceRepository.save(preferences);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_PREFERENCE_CACHE, allEntries = true)
    public void updateDataSharing(Long userId, Boolean enabled) {
        log.info("Updating data sharing for user: {} to {} (GDPR consent)", userId, enabled);

        UserPreference preferences = getOrCreatePreferences(userId);
        preferences.setDataSharing(enabled);
        userPreferenceRepository.save(preferences);

        // Log data sharing consent change for GDPR compliance
        log.warn("Data sharing consent changed for user: {} to: {} at: {}",
                userId, enabled, java.time.LocalDateTime.now());
    }

    // =================== UTILITY METHODS ===================

    @Override
    public List<String> getSupportedLanguages() {
        return new ArrayList<>(SUPPORTED_LANGUAGES);
    }

    @Override
    public List<String> getSupportedTimezones() {
        return ZoneId.getAvailableZoneIds().stream()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getSupportedCurrencies() {
        return new ArrayList<>(SUPPORTED_CURRENCIES);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_PREFERENCE_CACHE, allEntries = true)
    public PreferenceResponse resetToDefaults(Long userId) {
        log.info("Resetting preferences to defaults for user: {}", userId);

        // Delete existing preferences
        userPreferenceRepository.findByUserId(userId)
                .ifPresent(userPreferenceRepository::delete);

        // Create new default preferences
        UserPreference defaults = createDefaultPreferences(userId);

        return userPreferenceMapper.toResponse(defaults);
    }

    // =================== ADMIN OPERATIONS ===================

    @Override
    @Cacheable(value = "preferenceStats", unless = "#result.isEmpty()")
    public Map<String, Object> getPreferenceStatistics() {
        log.debug("Calculating preference statistics");

        Map<String, Object> stats = new HashMap<>();

        // Theme distribution
        stats.put("themeDistribution", userPreferenceRepository.getThemeDistribution());

        // Language distribution
        stats.put("languageDistribution", userPreferenceRepository.getLanguageDistribution());

        // Notification statistics
        Object[] notificationStats = userPreferenceRepository.getNotificationStatistics();
        if (notificationStats != null && notificationStats.length >= 4) {
            Map<String, Long> notifications = new HashMap<>();
            notifications.put("emailEnabled", (Long) notificationStats[0]);
            notifications.put("pushEnabled", (Long) notificationStats[1]);
            notifications.put("smsEnabled", (Long) notificationStats[2]);
            notifications.put("marketingEnabled", (Long) notificationStats[3]);
            stats.put("notificationStatistics", notifications);
        }

        return stats;
    }

    @Override
    @Cacheable(value = "marketingConsentStats", unless = "#result.isEmpty()")
    public Map<String, Object> getMarketingConsentStatistics() {
        log.debug("Calculating marketing consent statistics");

        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userPreferenceRepository.count();
        long dataSharingOptIns = userPreferenceRepository.countDataSharingOptIns();

        List<UserPreference> marketingEnabledUsers = userPreferenceRepository.findByMarketingEmailsEnabled(true);
        long marketingOptIns = marketingEnabledUsers.size();

        stats.put("totalUsers", totalUsers);
        stats.put("marketingOptIns", marketingOptIns);
        stats.put("marketingOptInRate", totalUsers > 0 ? (marketingOptIns * 100.0 / totalUsers) : 0.0);
        stats.put("dataSharingOptIns", dataSharingOptIns);
        stats.put("dataSharingOptInRate", totalUsers > 0 ? (dataSharingOptIns * 100.0 / totalUsers) : 0.0);

        return stats;
    }

    // =================== PRIVATE HELPER METHODS ===================

    private UserPreference createDefaultPreferences(Long userId) {
        log.debug("Creating default preferences for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserPreference preferences = UserPreference.builder()
                .user(user)
                .theme(UserPreference.Theme.LIGHT)
                .language("en")
                .timezone("UTC")
                .currency("USD")
                .dateFormat(UserPreference.DateFormat.MM_DD_YYYY)
                .numberFormat(UserPreference.NumberFormat.US)
                .emailNotifications(true)
                .pushNotifications(true)
                .smsNotifications(false)
                .marketingEmails(false)
                .portfolioAlerts(true)
                .priceAlerts(true)
                .newsAlerts(false)
                .transactionAlerts(true)
                .dashboardLayout(UserPreference.DashboardLayout.DEFAULT)
                .defaultPortfolioView(UserPreference.PortfolioView.SUMMARY)
                .chartType(UserPreference.ChartType.LINE)
                .chartTimeRange(UserPreference.ChartTimeRange.ONE_MONTH)
                .profilePublic(false)
                .portfolioPublic(false)
                .showPerformance(true)
                .dataSharing(false)
                .analyticsTracking(true)
                .twoFactorEnabled(false)
                .loginNotifications(true)
                .sessionTimeoutMinutes(60)
                .build();

        return userPreferenceRepository.save(preferences);
    }

    private UserPreference getOrCreatePreferences(Long userId) {
        return userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
    }

    private void validatePreferences(UserPreference preferences) {
        if (preferences.getLanguage() != null) {
            validateLanguage(preferences.getLanguage());
        }

        if (preferences.getTimezone() != null) {
            validateTimezone(preferences.getTimezone());
        }

        if (preferences.getCurrency() != null) {
            validateCurrency(preferences.getCurrency());
        }

        if (preferences.getSessionTimeoutMinutes() != null) {
            if (preferences.getSessionTimeoutMinutes() < 5 || preferences.getSessionTimeoutMinutes() > 480) {
                throw new BusinessException("Session timeout must be between 5 and 480 minutes",
                        "INVALID_SESSION_TIMEOUT");
            }
        }
    }

    private void validateLanguage(String language) {
        if (!SUPPORTED_LANGUAGES.contains(language)) {
            throw new BusinessException("Unsupported language: " + language, "UNSUPPORTED_LANGUAGE");
        }
    }

    private void validateTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
        } catch (Exception e) {
            throw new BusinessException("Invalid timezone: " + timezone, "INVALID_TIMEZONE");
        }
    }

    private void validateCurrency(String currency) {
        if (!SUPPORTED_CURRENCIES.contains(currency)) {
            throw new BusinessException("Unsupported currency: " + currency, "UNSUPPORTED_CURRENCY");
        }
    }

    private void logPreferenceChanges(Long userId, UpdatePreferencesRequest request) {
        List<String> changes = new ArrayList<>();

        if (request.getLanguage() != null) changes.add("language");
        if (request.getTimezone() != null) changes.add("timezone");
        if (request.getCurrency() != null) changes.add("currency");
        if (request.getMarketingEmails() != null) changes.add("marketingEmails");
        if (request.getDataSharing() != null) changes.add("dataSharing");

        if (!changes.isEmpty()) {
            log.info("Preference changes for user {}: {}", userId, changes);
        }
    }

    private void publishPreferencesUpdatedEvent(UserPreference preferences) {
        // TODO: Implement preferences updated event
        log.debug("Publishing preferences updated event for user: {}", preferences.getUser().getId());
    }
}