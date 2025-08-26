package in.wealthinker.wealthinker.modules.user.service;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdateUserRequest;
import in.wealthinker.wealthinker.shared.enums.UserRole;
import in.wealthinker.wealthinker.shared.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import in.wealthinker.wealthinker.modules.user.dto.request.CreateUserRequest;
import in.wealthinker.wealthinker.modules.user.dto.request.UpdateUsernameRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.UserResponse;
import in.wealthinker.wealthinker.modules.user.dto.response.UserSummaryResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {

    // =================== USER CREATION & REGISTRATION ===================

    /**
     * Create new user account with complete profile setup
     *
     * BUSINESS LOGIC:
     * - Validate email/username uniqueness
     * - Hash password securely
     * - Create default profile and preferences
     * - Send welcome email
     * - Publish user created event
     *
     * @param request User registration details
     * @return Complete user information
     * @throws ValidationException if validation fails
     * @throws DuplicateEmailException if email already exists
     */

    UserResponse createUser(CreateUserRequest request);

    /**
     * Register user through OAuth2 provider
     *
     * OAUTH2 FLOW:
     * - Extract user info from OAuth2 attributes
     * - Check if user already exists (email matching)
     * - Create or update user record
     * - Link OAuth2 provider information
     */
    UserResponse createOAuth2User(String email, String firstName, String lastName,
                                  String provider, String providerId,
                                  Map<String, Object> attributes);


    // =================== USER RETRIEVAL ===================

    /**
     * Get user by ID with complete information
     *
     * CACHING: Cached for 1 hour (user data changes infrequently)
     * SECURITY: Returns public view for non-owners
     */

    Optional<UserResponse> getUserById(Long userId);

    /**
     * Get user by ID for current authenticated user
     *
     * SECURITY: Returns complete information for account owner
     */
    Optional<UserResponse> getCurrentUserById(Long userId, Long currentUserId);
    
    Optional<UserResponse> getUserByEmail(String email);

    Optional<UserResponse> getUserByUsername(String username);

    Optional<UserResponse> getUserByPhoneNumber(String phoneNumber);

    /**
     * Multi-identifier lookup (email, username, or phone)
     *
     * AUTHENTICATION: Primary method for login identification
     */
    Optional<UserResponse> getUserByEmailOrUsernameOrPhoneNumber(String identifier);

    // =================== USER UPDATES ===================

    /**
     * Update user basic information
     *
     * BUSINESS LOGIC:
     * - Validate new email/username uniqueness
     * - Update audit fields
     * - Clear relevant caches
     * - Publish user updated event
     */
    UserResponse updateUser(Long userId, UpdateUserRequest request);

    UserResponse updateUserRole(Long userId, UserRole newRole, String reason);

    UserResponse updateUserStatus(Long userId, UserStatus newStatus, String reason);

    UserResponse updateUsername(Long userId, UpdateUsernameRequest request);

    // =================== VALIDATION & UTILITIES ===================

    boolean isUsernameAvailable(String username);

    boolean isEmailAvailable(String email);

    boolean isPhoneNumberAvailable(String phoneNumber);

    /**
     * Get user statistics for admin dashboard
     *
     * ANALYTICS: User metrics and KPIs
     */
    Map<String, Object> getUserStatistics();
    
    UserSummaryResponse getUserSummary(Long userId);

    // =================== USER SEARCH & LISTING ===================

    /**
     * Get paginated list of all users (admin only)
     *
     * ADMIN FEATURE: Returns summary view for performance
     * SECURITY: Requires ADMIN role
     */
    Page<UserSummaryResponse> getAllUsers(Pageable pageable);
    
    Page<UserSummaryResponse> searchUsers(String searchTerm, String role, String status, Boolean emailVerified, Pageable pageable);

    /**
     * Get users registered in date range
     *
     * ANALYTICS: User acquisition analysis
     */
    Page<UserSummaryResponse> getUsersRegisteredBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // void changePassword(Long userId, ChangePasswordRequest request);

    // =================== ACCOUNT MANAGEMENT ===================
    
    void activateUser(Long userId);

    void deactivateUser(Long userId, String reason);

    void suspendUser(Long userId, String reason, Integer durationDays);

    void deleteUser(Long userId, String reason);

    // =================== BULK OPERATIONS ===================

    /**
     * Bulk update user status
     *
     * ADMIN FEATURE: Mass operations for user management
     * PERFORMANCE: Single transaction for all updates
     */
    void bulkUpdateUserStatus(List<Long> userIds, UserStatus status, String reason);

    /**
     * Export user data (GDPR compliance)
     *
     * PRIVACY: User can request their data export
     */
    byte[] exportUserData(Long userId);
}
