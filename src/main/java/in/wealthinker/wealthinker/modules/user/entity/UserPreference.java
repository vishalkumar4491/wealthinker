package in.wealthinker.wealthinker.modules.user.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Currency;
import java.util.Locale;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * User Preferences Entity - Settings & Customization
 *
 * PURPOSE:
 * - Store user's UI/UX preferences (theme, language, timezone)
 * - Manage notification settings (email, SMS, push)
 * - Control privacy and data sharing preferences
 * - Handle dashboard and trading customizations
 *
 * DESIGN DECISIONS:
 * 1. Separate from UserProfile - different access patterns and caching needs
 * 2. Default values for all settings - users can start immediately
 * 3. Validation for all inputs - prevent invalid configurations
 * 4. Cache-friendly - rarely changes, can be cached for long periods
 * 5. Privacy-focused - granular controls for data sharing
 */
@Entity
@Table(name = "user_preferences",
        indexes = {
                @Index(name = "idx_preference_user_id", columnList = "user_id"),
                @Index(name = "idx_preference_language", columnList = "language"),
                @Index(name = "idx_preference_timezone", columnList = "timezone")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Relationship to User
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_preference_user"))
    private User user;

    // UI/UX Preferences
    @Enumerated(EnumType.STRING)
    @Column(name = "theme", length = 10)
    @Builder.Default
    private Theme theme = Theme.LIGHT;

    @Size(min = 2, max = 10, message = "Language code must be between 2 and 10 characters")
    @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "Language must be a valid ISO language code")
    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "en"; // ISO 639-1 language code

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "Asia/Kolkata";

    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "number_format", length = 20)
    @Builder.Default
    private NumberFormat numberFormat = NumberFormat.IN;

    @Enumerated(EnumType.STRING)
    @Column(name = "date_format", length = 20)
    @Builder.Default
    private DateFormat dateFormat = DateFormat.MM_DD_YYYY;

    // Notification Preferences
    @Column(name = "email_notifications", nullable = false)
    @Builder.Default
    private Boolean emailNotifications = true;

    @Column(name = "push_notifications", nullable = false)
    @Builder.Default
    private Boolean pushNotifications = true;

    @Column(name = "sms_notifications", nullable = false)
    @Builder.Default
    private Boolean smsNotifications = false;

    @Column(name = "marketing_emails", nullable = false)
    @Builder.Default
    private Boolean marketingEmails = true;

    // Financial Notifications

    @Column(name = "portfolio_alerts", nullable = false)
    @Builder.Default
    private Boolean portfolioAlerts = true;

    @Column(name = "price_alerts", nullable = false)
    @Builder.Default
    private Boolean priceAlerts = true;

    @Column(name = "news_alerts", nullable = false)
    @Builder.Default
    private Boolean newsAlerts = false;

    @Column(name = "transaction_alerts", nullable = false)
    @Builder.Default
    private Boolean transactionAlerts = true;

    // Dashboard Preferences
    @Enumerated(EnumType.STRING)
    @Column(name = "dashboard_layout", length = 20)
    @Builder.Default
    private DashboardLayout dashboardLayout = DashboardLayout.DEFAULT;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_portfolio_view", length = 20)
    @Builder.Default
    private PortfolioView defaultPortfolioView = PortfolioView.SUMMARY;

    @Enumerated(EnumType.STRING)
    @Column(name = "chart_type", length = 20)
    @Builder.Default
    private ChartType chartType = ChartType.LINE;

    @Enumerated(EnumType.STRING)
    @Column(name = "chart_time_range", length = 10)
    @Builder.Default
    private ChartTimeRange chartTimeRange = ChartTimeRange.ONE_MONTH;

    // Privacy Preferences
    @Column(name = "profile_public", nullable = false)
    @Builder.Default
    private Boolean profilePublic = false;

    @Column(name = "portfolio_public", nullable = false)
    @Builder.Default
    private Boolean portfolioPublic = false;

    @Column(name = "show_performance", nullable = false)
    @Builder.Default
    private Boolean showPerformance = true;

    @Column(name = "data_sharing", nullable = false)
    @Builder.Default
    private Boolean dataSharing = false;

    @Column(name = "analytics_tracking", nullable = false)
    @Builder.Default
    private Boolean analyticsTracking = true;

    // Security Preferences
    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    @Column(name = "login_notifications", nullable = false)
    @Builder.Default
    private Boolean loginNotifications = true;

    @Column(name = "session_timeout_minutes")
    @Builder.Default
    private Integer sessionTimeoutMinutes = 60; // Auto-logout after inactivity

    // Audit Fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Enums
    public enum Theme {
        LIGHT, DARK, AUTO
    }

    public enum DateFormat {
        MM_DD_YYYY("MM/dd/yyyy"),
        DD_MM_YYYY("dd/MM/yyyy"),
        YYYY_MM_DD("yyyy-MM-dd"),
        DD_MMM_YYYY("dd MMM yyyy");

        private final String pattern;

        DateFormat(String pattern) {
            this.pattern = pattern;
        }

        public String getPattern() {
            return pattern;
        }
    }

    public enum NumberFormat {
        US("1,234.56"),      // US format
        EU("1.234,56"), // European format
        IN("1,23,456.78"); // Indian lakh/crore format

        private final String example;

        NumberFormat(String example) {
            this.example = example;
        }

        public String getExample() {
            return example;
        }
    }

    public enum DashboardLayout {
        DEFAULT, COMPACT, DETAILED
    }

    public enum PortfolioView {
        SUMMARY, DETAILED, PERFORMANCE, HOLDINGS
    }

    public enum ChartType {
        LINE, CANDLESTICK, BAR, AREA
    }

    public enum ChartTimeRange {
        ONE_DAY("1D"),
        ONE_WEEK("1W"),
        ONE_MONTH("1M"),
        THREE_MONTHS("3M"),
        ONE_YEAR("1Y"),
        ALL_TIME("ALL");

        private final String displayName;

        ChartTimeRange(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Helper Methods

    // Helper methods
    public ZoneId getTimezoneAsZoneId() {
        try {
            return ZoneId.of(timezone);
        } catch (Exception e) {
            return ZoneId.of("Asia/Kolkata");
        }
    }

    public Currency getCurrencyAsObject() {
        try {
            return Currency.getInstance(currency);
        } catch (Exception e) {
            return Currency.getInstance("INR");
        }
    }

    public Locale getLocaleAsObject() {
        try {
            return Locale.forLanguageTag(language);
        } catch (Exception e) {
            return Locale.ENGLISH;
        }
    }

    /**
     * Check if user has opted in for marketing communications
     * Important for GDPR compliance
     */
    public boolean hasMarketingConsent() {
        return marketingEmails || smsNotifications;
    }

    /**
     * Check if user allows data collection for analytics
     * Important for privacy compliance
     */
    public boolean allowsDataCollection() {
        return analyticsTracking || dataSharing;
    }

    /**
     * Get effective session timeout based on security settings
     * Higher security users get shorter timeouts
     */
    public int getEffectiveSessionTimeout() {
        if (twoFactorEnabled) {
            return sessionTimeoutMinutes; // Use user preference for 2FA users
        } else {
            return Math.min(sessionTimeoutMinutes, 40); // Max 40 minutes for non-2FA users
        }
    }
}
