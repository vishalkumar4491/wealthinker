package in.wealthinker.wealthinker.modules.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import in.wealthinker.wealthinker.modules.user.entity.User;
import in.wealthinker.wealthinker.shared.enums.UserRole;
import in.wealthinker.wealthinker.shared.enums.UserStatus;
import org.springframework.stereotype.Repository;

/**
 * User Repository - Core user data access with advanced querying
 *
 * DESIGN PRINCIPLES:
 * 1. Method naming follows Spring Data conventions
 * 2. Custom @Query for complex operations
 * 3. QueryHints for performance optimization
 * 4. Separate methods for different use cases
 * 5. Security-first approach (active users only)
 *
 * PERFORMANCE CONSIDERATIONS:
 * - Indexed queries for O(log n) lookup time
 * - Query hints for JPA optimization
 * - Batch operations for bulk updates
 * - Read-only queries marked appropriately
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // =================== SINGLE USER LOOKUPS ===================

    /**
     * Find user by email (most common login method)
     *
     * PERFORMANCE: Uses idx_users_email index
     * SECURITY: No filtering - used for authentication
     */
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByPhoneNumber(String phoneNumber);

    //  Find by any identifier (for admin purposes)
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.username = :identifier OR u.phoneNumber = :identifier")
    Optional<User> findByEmailOrUsernameOrPhone(@Param("identifier") String identifier);

    /**
     * Find active user by email (for application use)
     *
     * PERFORMANCE: Uses idx_users_email_active composite index
     * SECURITY: Only returns active users
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    Optional<User> findByEmailAndIsActiveTrue(String email);

    Optional<User> findByUsernameAndIsActiveTrue(String username);

    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber AND u.isActive = true")
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    Optional<User> findByPhoneNumberAndIsActiveTrue(String phoneNumber);
    
    // Find by email, username, OR phone number
    @Query("SELECT u FROM User u WHERE (u.email = :identifier OR u.username = :identifier OR u.phoneNumber = :identifier) AND u.isActive = true")
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    Optional<User> findByEmailOrUsernameOrPhoneAndIsActiveTrue(@Param("identifier") String identifier);


    // =================== OAUTH2 SUPPORT ===================

    /**
     * Find user by OAuth2 provider and provider ID
     *
     * USED FOR: Google, Facebook, GitHub login
     * PERFORMANCE: Composite index on (provider, provider_id)
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    // =================== EXISTENCE CHECKS ===================

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phoneNumber);

    // Check if email, username, or phone exists
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email OR u.username = :username OR u.phoneNumber = :phoneNumber")
    boolean existsByEmailOrUsernameOrPhone(@Param("email") String email, @Param("username") String username, @Param("phoneNumber") String phoneNumber);

    // Check if any identifier exists
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :identifier OR u.username = :identifier OR u.phoneNumber = :identifier")
    boolean existsByAnyIdentifier(@Param("identifier") String identifier);

    // =================== FILTERED QUERIES ===================

    /**
     * Find users by status with pagination
     *
     * ADMIN USE: View users by status (active, suspended, etc.)
     * PERFORMANCE: Uses idx_users_status index
     */

    Page<User> findByStatus(UserStatus status, Pageable pageable);
    
    Page<User> findByRole(UserRole role, Pageable pageable);

    // Find by email verification status
    @Query("SELECT u FROM User u WHERE u.emailVerified = :emailVerified")
    Page<User> findByEmailVerified(@Param("emailVerified") Boolean emailVerified, Pageable pageable);

    // Find by phone verification status
     @Query("SELECT u FROM User u WHERE u.phoneVerified = :phoneVerified")
     Page<User> findByPhoneVerified(@Param("phoneVerified") Boolean phoneVerified, Pageable pageable);

    // =================== DATE RANGE QUERIES ===================

    // Find users registered after a specific date
    @Query("SELECT u FROM User u WHERE u.createdAt >= :fromDate ORDER BY u.createdAt DESC")
    Page<User> findUsersRegisteredAfter(@Param("fromDate") LocalDateTime fromDate, Pageable pageable);

    // Find inactive users (no recent login)
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NULL OR u.lastLoginAt < :beforeDate")
    Page<User> findInactiveUsers(@Param("beforeDate") LocalDateTime beforeDate, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.loginAttempts >= :attempts AND u.isActive = true")
    List<User> findUsersWithFailedAttempts(@Param("attempts") Integer attempts);

    // =================== AGGREGATION QUERIES ===================

    /**
     * Count users by status
     *
     * ANALYTICS USE: Dashboard metrics
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") UserStatus status);

    /**
     * Count users by role
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);

    /**
     * Count active users (for metrics dashboard)
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    /**
     * Count new users in date range
     *
     * ANALYTICS USE: Growth metrics
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    long countNewUsersInDateRange(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    // =================== BULK OPERATIONS ===================

    /**
     * Bulk update user status
     *
     * ADMIN USE: Mass user operations (activate/deactivate)
     * PERFORMANCE: Single SQL UPDATE instead of N queries
     */
    @Modifying
    @Query("UPDATE User u SET u.status = :status, u.updatedAt = CURRENT_TIMESTAMP, u.updatedBy = :updatedBy WHERE u.id IN :userIds")
    int updateUserStatus(@Param("userIds") List<Long> userIds,
                         @Param("status") UserStatus status,
                         @Param("updatedBy") String updatedBy);

    /**
     * Bulk reset login attempts
     *
     * SECURITY USE: Reset after successful login or admin action
     */
    @Modifying
    @Query("UPDATE User u SET u.loginAttempts = 0, u.accountLockedUntil = NULL WHERE u.id IN :userIds")
    int resetLoginAttempts(@Param("userIds") List<Long> userIds);

    /**
     * Mark users as inactive who haven't logged in for X days
     *
     * CLEANUP USE: Automated cleanup job
     */
    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.lastLoginAt < :cutoffDate AND u.status = 'ACTIVE'")
    int markInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate,
                          @Param("status") UserStatus status);

    // =================== STATISTICS QUERIES ===================

    /**
     * Get user registration statistics by month
     *
     * ANALYTICS USE: Growth charts
     */
    @Query("SELECT EXTRACT(YEAR FROM u.createdAt) as year, " +
            "EXTRACT(MONTH FROM u.createdAt) as month, " +
            "COUNT(u) as count " +
            "FROM User u " +
            "WHERE u.createdAt >= :startDate " +
            "GROUP BY EXTRACT(YEAR FROM u.createdAt), EXTRACT(MONTH FROM u.createdAt) " +
            "ORDER BY year DESC, month DESC")
    List<Object[]> getUserRegistrationStats(@Param("startDate") LocalDateTime startDate);

    /**
     * Get user distribution by role
     */
    @Query("SELECT u.role, COUNT(u) FROM User u WHERE u.isActive = true GROUP BY u.role")
    List<Object[]> getUserRoleDistribution();

    // =================== SEARCH FUNCTIONALITY ===================

    /**
     * Search users by name, email, or username
     *
     * ADMIN USE: User search in admin panel
     * PERFORMANCE: Uses ILIKE for case-insensitive search
     */
    @Query("SELECT u FROM User u LEFT JOIN u.profile p WHERE " +
            "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
}
