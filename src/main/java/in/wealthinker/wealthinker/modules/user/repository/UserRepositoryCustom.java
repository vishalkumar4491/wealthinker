package in.wealthinker.wealthinker.modules.user.repository;

import in.wealthinker.wealthinker.modules.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Custom Repository Interface for complex queries
 *
 * WHY SEPARATE INTERFACE:
 * - Spring Data JPA has limitations for very complex queries
 * - Custom implementations allow fine-tuned performance
 * - Better control over query execution and caching
 * - Type-safe query building with Criteria API
 */
public interface UserRepositoryCustom {
    /**
     * Advanced user search with multiple filters
     *
     * BUSINESS USE: Admin panel advanced search
     * PERFORMANCE: Dynamic query building based on provided filters
     */
    Page<User> findUsersWithFilters(String email, String username, String phoneNumber,
                                    String firstName, String lastName, String role,
                                    String status, LocalDateTime registeredAfter,
                                    LocalDateTime registeredBefore, Boolean emailVerified,
                                    Boolean phoneVerified, Pageable pageable);

    Map<String, Long> getUserActivityStats();

    List<User> findUsersEligibleForFeature(String featureName);

    Page<User> findUsersWithIncompleteProfiles(Integer maxCompletionPercentage, Pageable pageable);
}
