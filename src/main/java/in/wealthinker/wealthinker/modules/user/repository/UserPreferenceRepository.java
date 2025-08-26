package in.wealthinker.wealthinker.modules.user.repository;

import java.util.List;
import java.util.Optional;

import in.wealthinker.wealthinker.modules.user.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByUserId(Long userId);

    @Query("SELECT up FROM UserPreference up WHERE up.marketingEmails = :enabled")
    List<UserPreference> findByMarketingEmailsEnabled(@Param("enabled") Boolean enabled);

    @Query("SELECT up FROM UserPreference up WHERE up.pushNotifications = :enabled")
    List<UserPreference> findByPushNotificationsEnabled(@Param("enabled") Boolean enabled);

    List<UserPreference> findByTheme(UserPreference.Theme theme);

    List<UserPreference> findByLanguage(String language);

    List<UserPreference> findByTimezone(String timezone);

    @Query("SELECT up FROM UserPreference up WHERE up.twoFactorEnabled = true")
    List<UserPreference> findUsersWithTwoFactorEnabled();

    // =================== ANALYTICS QUERIES ===================

    @Query("SELECT up.theme, COUNT(up) FROM UserPreference up GROUP BY up.theme")
    List<Object[]> getThemeDistribution();

    @Query("SELECT up.language, COUNT(up) FROM UserPreference up GROUP BY up.language")
    List<Object[]> getLanguageDistribution();

    @Query("SELECT " +
            "SUM(CASE WHEN up.emailNotifications = true THEN 1 ELSE 0 END) as emailEnabled, " +
            "SUM(CASE WHEN up.pushNotifications = true THEN 1 ELSE 0 END) as pushEnabled, " +
            "SUM(CASE WHEN up.smsNotifications = true THEN 1 ELSE 0 END) as smsEnabled, " +
            "SUM(CASE WHEN up.marketingEmails = true THEN 1 ELSE 0 END) as marketingEnabled " +
            "FROM UserPreference up")
    Object[] getNotificationStatistics();

    @Query("SELECT COUNT(up) FROM UserPreference up WHERE up.dataSharing = true")
    long countDataSharingOptIns();
}
