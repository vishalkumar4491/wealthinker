package in.wealthinker.wealthinker.modules.user.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Currency;
import java.util.Locale;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_preferences")
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // UI Preferences
    @Enumerated(EnumType.STRING)
    @Column(name = "theme")
    @Builder.Default
    private Theme theme = Theme.LIGHT;

    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "en";

    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "Asia/Kolkata";

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "date_format", length = 20)
    @Builder.Default
    private String dateFormat = "dd/MM/yyyy";

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

    @Column(name = "portfolio_alerts", nullable = false)
    @Builder.Default
    private Boolean portfolioAlerts = true;

    @Column(name = "price_alerts", nullable = false)
    @Builder.Default
    private Boolean priceAlerts = true;

    @Column(name = "news_alerts", nullable = false)
    @Builder.Default
    private Boolean newsAlerts = false;

    // Dashboard Preferences
    @Column(name = "dashboard_layout", length = 20)
    @Builder.Default
    private String dashboardLayout = "default";

    @Column(name = "default_portfolio_view", length = 20)
    @Builder.Default
    private String defaultPortfolioView = "summary";

    @Column(name = "chart_type", length = 20)
    @Builder.Default
    private String chartType = "line";

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Enums
    public enum Theme {
        LIGHT, DARK, AUTO
    }

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
}
