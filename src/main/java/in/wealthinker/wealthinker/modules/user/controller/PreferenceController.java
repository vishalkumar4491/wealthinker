package in.wealthinker.wealthinker.modules.user.controller;

import in.wealthinker.wealthinker.modules.user.entity.UserPreference;
import in.wealthinker.wealthinker.shared.response.ApiResponseCustom;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdatePreferencesRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.PreferenceResponse;
import in.wealthinker.wealthinker.modules.user.service.PreferenceService;
import in.wealthinker.wealthinker.shared.constants.AppConstants;

import jakarta.validation.Valid;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(AppConstants.USER_ENDPOINT + "/{userId}/preferences")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Preferences", description = "User settings and preference management")
public class PreferenceController {

    private final PreferenceService userPreferenceService;

    // =================== PREFERENCE RETRIEVAL ===================

    @GetMapping
    @Operation(
            summary = "Get user preferences",
            description = "Retrieve complete user preferences and settings. Users can only access their own preferences.",
            responses = {
                    @ApiResponse(responseCode = "201", description =  "Preferences retrieved successfully"),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "Preferences not found")
            }
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<PreferenceResponse>> getUserPreferences(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        log.debug("Getting preferences for user: {}", userId);

        PreferenceResponse preferences = userPreferenceService.getUserPreferences(userId);

        return ResponseEntity.ok(ApiResponseCustom.success(preferences));
    }

    @GetMapping("/ui")
    @Operation(
            summary = "Get UI preferences",
            description = "Get UI-specific preferences (theme, language, timezone) for client applications"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Map<String, Object>>> getUIPreferences(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        log.debug("Getting UI preferences for user: {}", userId);

        Map<String, Object> uiPreferences = userPreferenceService.getUIPreferences(userId);

        return ResponseEntity.ok(ApiResponseCustom.success(uiPreferences));
    }

    @GetMapping("/notifications")
    @Operation(
            summary = "Get notification preferences",
            description = "Get notification settings for all channels (email, SMS, push)"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Map<String, Object>>> getNotificationPreferences(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        log.debug("Getting notification preferences for user: {}", userId);

        Map<String, Object> notificationPreferences = userPreferenceService.getNotificationPreferences(userId);

        return ResponseEntity.ok(ApiResponseCustom.success(notificationPreferences));
    }

    // =================== PREFERENCE UPDATES ===================

    @PutMapping
    @Operation(
            summary = "Update user preferences",
            description = "Update user preferences and settings. Supports partial updates."
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<PreferenceResponse>> updatePreferences(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UpdatePreferencesRequest request) {

        log.info("Updating preferences for user: {}", userId);

        PreferenceResponse preferences = userPreferenceService.updatePreferences(userId, request);

        return ResponseEntity.ok(ApiResponseCustom.success(preferences, "Preferences updated successfully"));
    }

    @PutMapping("/theme")
    @Operation(
            summary = "Update theme preference",
            description = "Update UI theme (light, dark, auto)"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Void>> updateTheme(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Theme (LIGHT, DARK, AUTO)") @RequestParam UserPreference.Theme theme) {

        log.debug("Updating theme for user: {} to {}", userId, theme);

        userPreferenceService.updateTheme(userId, theme);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "Theme updated successfully"));
    }

    @PutMapping("/language")
    @Operation(
            summary = "Update language preference",
            description = "Update user interface language"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Void>> updateLanguage(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Language code (ISO 639-1)") @RequestParam String language) {

        log.debug("Updating language for user: {} to {}", userId, language);

        userPreferenceService.updateLanguage(userId, language);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "Language updated successfully"));
    }

    @PutMapping("/timezone")
    @Operation(
            summary = "Update timezone preference",
            description = "Update user timezone for date/time display"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Void>> updateTimezone(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Timezone (IANA timezone identifier)") @RequestParam String timezone) {

        log.debug("Updating timezone for user: {} to {}", userId, timezone);

        userPreferenceService.updateTimezone(userId, timezone);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "Timezone updated successfully"));
    }

    @PutMapping("/currency")
    @Operation(
            summary = "Update currency preference",
            description = "Update default currency for financial displays"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Void>> updateCurrency(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Currency code (ISO 4217)") @RequestParam String currency) {

        log.debug("Updating currency for user: {} to {}", userId, currency);

        userPreferenceService.updateCurrency(userId, currency);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "Currency updated successfully"));
    }

    // =================== NOTIFICATION PREFERENCES ===================

    @PutMapping("/notifications/email")
    @Operation(
            summary = "Update email notification settings",
            description = "Enable/disable email notifications"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Void>> updateEmailNotifications(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Enable email notifications") @RequestParam Boolean enabled) {

        log.debug("Updating email notifications for user: {} to {}", userId, enabled);

        userPreferenceService.updateEmailNotifications(userId, enabled);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "Email notification settings updated"));
    }

    @PutMapping("/notifications/push")
    @Operation(
            summary = "Update push notification settings",
            description = "Enable/disable push notifications"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Void>> updatePushNotifications(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Enable push notifications") @RequestParam Boolean enabled) {

        log.debug("Updating push notifications for user: {} to {}", userId, enabled);

        userPreferenceService.updatePushNotifications(userId, enabled);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "Push notification settings updated"));
    }

    @PutMapping("/notifications/sms")
    @Operation(
            summary = "Update SMS notification settings",
            description = "Enable/disable SMS notifications"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Void>> updateSmsNotifications(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Enable SMS notifications") @RequestParam Boolean enabled) {

        log.debug("Updating SMS notifications for user: {} to {}", userId, enabled);

        userPreferenceService.updateSmsNotifications(userId, enabled);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "SMS notification settings updated"));
    }

    @PutMapping("/notifications/marketing")
    @Operation(
            summary = "Update marketing email settings",
            description = "Enable/disable marketing emails (GDPR compliant opt-in/out)"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Void>> updateMarketingEmails(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Enable marketing emails") @RequestParam Boolean enabled) {

        log.info("Updating marketing emails for user: {} to {} (GDPR consent)", userId, enabled);

        userPreferenceService.updateMarketingEmails(userId, enabled);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "Marketing email settings updated"));
    }

    // =================== DASHBOARD CUSTOMIZATION ===================

    @PutMapping("/dashboard/layout")
    @Operation(
            summary = "Update dashboard layout",
            description = "Update dashboard layout preference"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Void>> updateDashboardLayout(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Dashboard layout") @RequestParam UserPreference.DashboardLayout layout) {

        log.debug("Updating dashboard layout for user: {} to {}", userId, layout);

        userPreferenceService.updateDashboardLayout(userId, layout);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "Dashboard layout updated"));
    }

    @PutMapping("/dashboard/portfolio-view")
    @Operation(
            summary = "Update default portfolio view",
            description = "Update default portfolio view preference"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Void>> updatePortfolioView(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Portfolio view") @RequestParam UserPreference.PortfolioView view) {

        log.debug("Updating portfolio view for user: {} to {}", userId, view);

        userPreferenceService.updatePortfolioView(userId, view);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "Portfolio view updated"));
    }

    @PutMapping("/dashboard/chart-type")
    @Operation(
            summary = "Update default chart type",
            description = "Update default chart type for financial data"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Void>> updateChartType(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Chart type") @RequestParam UserPreference.ChartType chartType) {

        log.debug("Updating chart type for user: {} to {}", userId, chartType);

        userPreferenceService.updateChartType(userId, chartType);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "Chart type updated"));
    }

    // =================== PRIVACY SETTINGS ===================

    @PutMapping("/privacy/profile-public")
    @Operation(
            summary = "Update profile visibility",
            description = "Make profile public or private"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Void>> updateProfileVisibility(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Make profile public") @RequestParam Boolean isPublic) {

        log.debug("Updating profile visibility for user: {} to public: {}", userId, isPublic);

        userPreferenceService.updateProfileVisibility(userId, isPublic);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "Profile visibility updated"));
    }

    @PutMapping("/privacy/data-sharing")
    @Operation(
            summary = "Update data sharing consent",
            description = "Enable/disable data sharing for analytics (GDPR compliant)"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<Void>> updateDataSharing(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Allow data sharing") @RequestParam Boolean enabled) {

        log.info("Updating data sharing for user: {} to {} (GDPR consent)", userId, enabled);

        userPreferenceService.updateDataSharing(userId, enabled);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "Data sharing preferences updated"));
    }

    // =================== UTILITY ENDPOINTS ===================

    @GetMapping("/options")
    @Operation(
            summary = "Get preference options",
            description = "Get available options for various preference settings"
    )
    public ResponseEntity<ApiResponseCustom<Map<String, Object>>> getPreferenceOptions() {

        log.debug("Getting preference options");

        Map<String, Object> options = Map.of(
                "themes", Arrays.stream(UserPreference.Theme.values())
                        .collect(Collectors.toList()),
                "languages", userPreferenceService.getSupportedLanguages(),
                "timezones", userPreferenceService.getSupportedTimezones(),
                "currencies", userPreferenceService.getSupportedCurrencies(),
                "dateFormats", Arrays.stream(UserPreference.DateFormat.values())
                        .collect(Collectors.toList()),
                "dashboardLayouts", Arrays.stream(UserPreference.DashboardLayout.values())
                        .collect(Collectors.toList()),
                "chartTypes", Arrays.stream(UserPreference.ChartType.values())
                        .collect(Collectors.toList())
        );

        return ResponseEntity.ok(ApiResponseCustom.success(options));
    }

    @PostMapping("/reset")
    @Operation(
            summary = "Reset preferences to default",
            description = "Reset all preferences to system defaults"
    )
    @PreAuthorize("@securityUtils.canAccessUser(#userId)")
    public ResponseEntity<ApiResponseCustom<PreferenceResponse>> resetToDefaults(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        log.info("Resetting preferences to defaults for user: {}", userId);

        PreferenceResponse preferences = userPreferenceService.resetToDefaults(userId);

        return ResponseEntity.ok(ApiResponseCustom.success(preferences, "Preferences reset to defaults"));
    }

    // =================== ADMIN ENDPOINTS ===================

    @GetMapping("/admin/statistics")
    @Operation(
            summary = "Get preference statistics",
            description = "Get preference usage statistics. Admin access required."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseCustom<Map<String, Object>>> getPreferenceStatistics() {

        log.debug("Getting preference statistics");

        Map<String, Object> statistics = userPreferenceService.getPreferenceStatistics();

        return ResponseEntity.ok(ApiResponseCustom.success(statistics));
    }

    @GetMapping("/admin/marketing-consents")
    @Operation(
            summary = "Get marketing consent statistics",
            description = "Get marketing consent statistics for GDPR compliance. Admin access required."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseCustom<Map<String, Object>>> getMarketingConsentStatistics() {

        log.debug("Getting marketing consent statistics");

        Map<String, Object> statistics = userPreferenceService.getMarketingConsentStatistics();

        return ResponseEntity.ok(ApiResponseCustom.success(statistics));
    }




}
