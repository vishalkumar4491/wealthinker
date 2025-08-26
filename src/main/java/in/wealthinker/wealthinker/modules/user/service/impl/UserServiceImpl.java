package in.wealthinker.wealthinker.modules.user.service.impl;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdateUserRequest;
import in.wealthinker.wealthinker.modules.user.event.UserCreatedEvent;
import in.wealthinker.wealthinker.modules.user.event.UserUpdatedEvent;
import in.wealthinker.wealthinker.modules.user.mapper.SecureMappingContext;
import in.wealthinker.wealthinker.modules.user.repository.UserRepositoryCustom;
import in.wealthinker.wealthinker.modules.user.service.UserValidationService;
import in.wealthinker.wealthinker.shared.enums.AuthProvider;
import in.wealthinker.wealthinker.shared.enums.UserRole;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import in.wealthinker.wealthinker.modules.user.dto.request.CreateUserRequest;
import in.wealthinker.wealthinker.modules.user.dto.request.UpdateUsernameRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.UserResponse;
import in.wealthinker.wealthinker.modules.user.dto.response.UserSummaryResponse;
import in.wealthinker.wealthinker.modules.user.entity.User;
import in.wealthinker.wealthinker.modules.user.entity.UserPreference;
import in.wealthinker.wealthinker.modules.user.entity.UserProfile;
import in.wealthinker.wealthinker.modules.user.mapper.UserMapper;
import in.wealthinker.wealthinker.modules.user.repository.UserRepository;
import in.wealthinker.wealthinker.modules.user.service.UserService;
import in.wealthinker.wealthinker.shared.constants.CacheConstants;
import in.wealthinker.wealthinker.shared.enums.UserStatus;
import in.wealthinker.wealthinker.shared.exceptions.BusinessException;
import in.wealthinker.wealthinker.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) // Default to read-only transactions
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserValidationService validationService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepositoryCustom userRepositoryCustom;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());

        // Check if user already exists by email or username
        // 1. Validate business rules
        validationService.validateUserCreation(request);

        // 2. Create user entity
        User user = userMapper.toUserEntity(request);
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // 3. Create user profile
        UserProfile profile = userMapper.createUserProfile(user, request);
        
        // Create user preferences with defaults
        UserPreference preference = userMapper.createDefaultPreferences(user);

        user.setProfile(profile);
        user.setPreference(preference);

        //4. Save user
        user = userRepository.save(user);

        // 5. Publish domain event for system integration
        publishUserCreatedEvent(user);

        log.info("User created successfully with ID: {} and email: {}", user.getId(), user.getEmail());

        // 6. Return response DTO
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createOAuth2User(String email, String firstName, String lastName, String provider,
                                         String providerId, Map<String, Object> attributes) {

        log.info("Creating OAuth2 user with email: {} and provider: {}", email, provider);

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            // Link OAuth2 provider to existing account
            User user = existingUser.get();
            user.setProvider(AuthProvider.valueOf(provider.toUpperCase()));
            user.setProviderId(providerId);
            user = userRepository.save(user);
            log.info("Linked OAuth2 provider {} to existing user: {}", provider, email);
            return userMapper.toUserResponse(user);
        }

        // Create new user from OAuth2 data
        User user = User.builder()
                .email(email)
                .provider(AuthProvider.valueOf(provider.toUpperCase()))
                .providerId(providerId)
                .status(UserStatus.ACTIVE) // OAuth2 users are pre-verified
                .emailVerified(true)
                .role(UserRole.FREE)
                .isActive(true)
                .build();

        // Create profile from OAuth2 attributes
        UserProfile profile = UserProfile.builder()
                .user(user)
                .firstName(firstName)
                .lastName(lastName)
                .build();

        // Create default preferences
        UserPreference preference = UserPreference.builder()
                .user(user)
                .build();

        user.setProfile(profile);
        user.setPreference(preference);

        user = userRepository.save(user);

        publishUserCreatedEvent(user);

        log.info("OAuth2 user created successfully: {}", email);
        return userMapper.toUserResponse(user);
    }

    // =================== USER RETRIEVAL ===================

    @Override
    @Cacheable(value = CacheConstants.USER_CACHE, key = "#userId", unless = "#result.isEmpty()")
    public Optional<UserResponse> getUserById(Long userId) {
        log.debug("Retrieving user by ID: {}", userId);
        return userRepository.findById(userId)
               // .filter(User::getIsActive)
                .map(userMapper::toUserResponse);
    }

    @Override
    public Optional<UserResponse> getCurrentUserById(Long userId, Long currentUserId) {
        log.debug("Retrieving user {} for current user {}", userId, currentUserId);

        return userRepository.findById(userId)
                // .filter(User::getIsActive)
                .map(user -> {
                    // Return full details for account owner, limited for others
                    if (user.getId().equals(currentUserId)) {
                        return userMapper.toUserResponse(user);
                    } else {
                        SecureMappingContext ctx = new SecureMappingContext(
                                false, // includePhoneNumber
                                false, // includeSecurityInfo
                                false, // includeProfile
                                false  // includePreferences
                        );
                        return userMapper.toUserSecureResponse(user, ctx);
                    }
                });
    }

    @Override
    @Cacheable(value = CacheConstants.USER_CACHE, key = "#email", unless = "#result.isEmpty()")
    public Optional<UserResponse> getUserByEmail(String email) {
        log.debug("Retrieving user by email: {}", email);
        return userRepository.findByEmailAndIsActiveTrue(email)
                .map(userMapper::toUserResponse);
    }

    @Override
    @Cacheable(value = CacheConstants.USER_CACHE, key = "#username", unless = "#result.isEmpty()")
    public Optional<UserResponse> getUserByUsername(String username) {
        log.debug("Retrieving user by username: {}", username);

        return userRepository.findByUsernameAndIsActiveTrue(username)
                .map(userMapper::toUserResponse);
    }

    @Override
    @Cacheable(value = CacheConstants.USER_CACHE, key = "#phoneNumber")
    public Optional<UserResponse> getUserByPhoneNumber(String phoneNumber) {
        log.debug("Retrieving user by phoneNumber: {}", phoneNumber);
        return userRepository.findByPhoneNumberAndIsActiveTrue(phoneNumber).
                map(userMapper :: toUserResponse);
    }

    @Override
    @Cacheable(value = CacheConstants.USER_CACHE, key = "#identifier", unless = "#result.isEmpty()")
    public Optional<UserResponse> getUserByEmailOrUsernameOrPhoneNumber(String identifier) {
        log.debug("Retrieving user by identifier: {}", identifier);
        return userRepository.findByEmailOrUsernameOrPhoneAndIsActiveTrue(identifier)
                .map(userMapper::toUserResponse);
    }

    // =================== USER UPDATES ===================
    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_CACHE, allEntries = true)
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        log.info("Updating user with ID: {}, fields: {}", userId, request.getUpdatedFields());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Validate update request
        validationService.validateUserUpdate(user, request);

        // Update user entity
        String oldEmail = user.getEmail();
        String oldUsername = user.getUsername();
        String oldPhoneNumber = user.getPhoneNumber();

        userMapper.updateUserEntityFromRequest(request, user);

        // Handle email change logic
        if (request.getEmail() != null && !request.getEmail().equals(oldEmail)) {
            handleEmailChange(user, oldEmail, request.getEmail());
        }

        // Handle phone change logic
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(oldPhoneNumber)) {
            handlePhoneNumberChange(user, oldPhoneNumber, request.getPhoneNumber());
        }

        // Update preferences if provided
        updateUserPreferences(user, request);

        // Save changes
        user = userRepository.save(user);

        // Publish update event
        publishUserUpdatedEvent(user, oldEmail);

        log.info("User updated successfully: {}", userId);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_CACHE, allEntries = true)
    public UserResponse updateUserRole(Long userId, UserRole newRole, String reason) {
        log.info("Updating user {} role to {} for reason: {}", userId, newRole, reason);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserRole oldRole = user.getRole();
        user.setRole(newRole);
        user = userRepository.save(user);

        // Log role change for audit
        log.warn("User role changed - UserID: {}, OldRole: {}, NewRole: {}, Reason: {}",
                userId, oldRole, newRole, reason);

        publishUserUpdatedEvent(user, null);

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_CACHE, allEntries = true)
    public UserResponse updateUserStatus(Long userId, UserStatus newStatus, String reason) {
        log.info("Updating user {} status to {} for reason: {}", userId, newStatus, reason);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Validate status transition
        validationService.validateStatusTransition(user.getStatus(), newStatus);

        UserStatus oldStatus = user.getStatus();
        user.setStatus(newStatus);

        // Handle status-specific logic
        switch (newStatus) {
            case INACTIVE:
                user.setIsActive(false);
                break;
            case ACTIVE:
                user.setIsActive(true);
                user.setAccountLockedUntil(null); // Clear any locks
                break;
            case SUSPENDED:
                user.setIsActive(false);
                // TODO: Revoke all active sessions
                break;
        }

        user = userRepository.save(user);

        // Log status change for audit
        log.warn("User status changed - UserID: {}, OldStatus: {}, NewStatus: {}, Reason: {}",
                userId, oldStatus, newStatus, reason);

        publishUserUpdatedEvent(user, null);

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_CACHE, allEntries = true)
    public UserResponse updateUsername(Long userId, UpdateUsernameRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if username is available
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username is already taken", "USERNAME_ALREADY_EXISTS");
        }

        user.setUsername(request.getUsername());
        userRepository.save(user);

        log.info("Username updated for user: {}", user.getEmail());
        return userMapper.toUserResponse(user);
    }

    // =================== VALIDATION & UTILITIES ===================

    @Override
    public boolean isEmailAvailable(String email) {
        log.debug("Checking email availability: {}", email);
        return !userRepository.existsByEmail(email);
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        log.debug("Checking username availability: {}", username);
        return !userRepository.existsByUsername(username);
    }

    @Override
    public boolean isPhoneNumberAvailable(String phoneNumber) {
        log.debug("Checking phone number availability: {}", phoneNumber);
        return !userRepository.existsByPhoneNumber(phoneNumber);
    }


    @Override
    @Cacheable(value = "userStats", unless = "#result.isEmpty()")
    public Map<String, Object> getUserStatistics() {
        log.debug("Calculating user statistics");

        Map<String, Object> stats = new HashMap<>();

        // Basic counts
        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countActiveUsers());
        stats.put("verifiedUsers", userRepository.countByStatus(UserStatus.ACTIVE));

        // Role distribution
        stats.put("roleDistribution", userRepository.getUserRoleDistribution());

        // Registration stats for current month
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        stats.put("newUsersThisMonth", userRepository.countNewUsersInDateRange(startOfMonth, LocalDateTime.now()));

        // Custom analytics from repository
        stats.putAll(userRepositoryCustom.getUserActivityStats());

        log.debug("User statistics calculated: {}", stats);
        return stats;
    }

    @Override
    public UserSummaryResponse getUserSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));   
        return userMapper.toUserSummaryResponse(user);
    }

    // =================== USER SEARCH & LISTING ===================

    @Override
    public Page<UserSummaryResponse> getAllUsers(Pageable pageable) {
        log.debug("Retrieving all users with pagination: {}", pageable);

        return userRepository.findAll(pageable)
                .map(userMapper::toUserSummaryResponse);
    }

    @Override
    public Page<UserSummaryResponse> searchUsers(String searchTerm, String role, String status, Boolean emailVerified, Pageable pageable) {
        log.debug("Searching users with term: {}, role: {}, status: {}, emailVerified: {}",
                searchTerm, role, status, emailVerified);

        // Use custom repository method for complex search
        UserRole userRole = role != null ? UserRole.valueOf(role.toUpperCase()) : null;
        UserStatus userStatus = status != null ? UserStatus.valueOf(status.toUpperCase()) : null;

        return userRepositoryCustom.findUsersWithFilters(
                searchTerm, // email filter
                searchTerm, // username filter
                searchTerm, // phone filter
                searchTerm, // firstName filter
                searchTerm, // lastName filter
                role,
                status,
                null, // registeredAfter
                null, // registeredBefore
                emailVerified,
                null, // phoneVerified
                pageable
        ).map(userMapper::toUserSummaryResponse);
    }

    @Override
    public Page<UserSummaryResponse> getUsersRegisteredBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Retrieving users registered between {} and {}", startDate, endDate);

        return userRepositoryCustom.findUsersWithFilters(
                null, null, null, null, null, null, null,
                startDate, endDate, null, null, pageable
        ).map(userMapper::toUserSummaryResponse);
    }

    // @Override
    // @Transactional
    // public void changePassword(Long userId, ChangePasswordRequest request) {
    //     User user = userRepository.findById(userId)
    //             .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    //     // Verify current password
    //     if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
    //         throw new BusinessException("Current password is incorrect", "INVALID_CURRENT_PASSWORD");
    //     }

    //     // Validate new password
    //     if (!request.isPasswordMatching()) {
    //         throw new BusinessException("New passwords do not match", "PASSWORD_MISMATCH");
    //     }

    //     // Update password
    //     user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    //     userRepository.save(user);

    //     log.info("Password changed successfully for user: {}", user.getEmail());
    // }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_CACHE, allEntries = true)
    public void activateUser(Long userId) {
        log.info("Activating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setStatus(UserStatus.ACTIVE);
        user.setIsActive(true);
        user.setAccountLockedUntil(null);
        user.setLoginAttempts(0);

        userRepository.save(user);

        publishUserUpdatedEvent(user, null);

        log.info("User activated successfully: {}", user.getEmail());
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_CACHE, allEntries = true)
    public void deactivateUser(Long userId, String reason) {
        log.info("Deactivating user {} for reason: {}", userId, reason);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setStatus(UserStatus.INACTIVE);
        user.setIsActive(false);

        userRepository.save(user);

        // Log deactivation for audit
        log.warn("User deactivated - UserID: {}, Reason: {}", userId, reason);

        publishUserUpdatedEvent(user, null);
    }


    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_CACHE, allEntries = true)
    public void suspendUser(Long userId, String reason, Integer durationDays) {
        log.warn("Suspending user {} for {} days, reason: {}", userId, durationDays, reason);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setStatus(UserStatus.SUSPENDED);
        user.setIsActive(false);

        if (durationDays != null) {
            user.setAccountLockedUntil(LocalDateTime.now().plusDays(durationDays));
        }

        userRepository.save(user);

        // Log suspension for audit
        log.error("User suspended - UserID: {}, Duration: {} days, Reason: {}",
                userId, durationDays, reason);

        publishUserUpdatedEvent(user, null);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_CACHE, allEntries = true)
    public void deleteUser(Long userId, String reason) {
        log.warn("Soft deleting user {} for reason: {}", userId, reason);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Soft delete - don't physically remove data
        user.setStatus(UserStatus.DELETED);
        user.setIsActive(false);

        // Anonymize sensitive data for GDPR compliance
        user.setEmail("deleted_" + userId + "@deleted.com");
        user.setUsername("deleted_" + userId);
        user.setPhoneNumber(null);

        userRepository.save(user);

        // Log deletion for audit
        log.error("User soft deleted - UserID: {}, Reason: {}", userId, reason);

        publishUserUpdatedEvent(user, null);
    }

    // =================== BULK OPERATIONS ===================

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.USER_CACHE, allEntries = true)
    public void bulkUpdateUserStatus(List<Long> userIds, UserStatus status, String reason) {
        log.info("Bulk updating {} users to status {} for reason: {}", userIds.size(), status, reason);

        if (userIds.isEmpty()) {
            return;
        }

        int updatedCount = userRepository.updateUserStatus(userIds, status, getCurrentUsername());

        log.info("Bulk status update completed: {} users updated to {}", updatedCount, status);

        // Log for audit
        log.warn("Bulk user status change - UserIDs: {}, NewStatus: {}, Reason: {}",
                userIds, status, reason);
    }

    @Override
    public byte[] exportUserData(Long userId) {
        log.info("Exporting user data for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // TODO: Implement comprehensive data export for GDPR compliance
        // This should include all user data across all modules

        UserResponse userData = userMapper.toUserResponse(user);

        // Convert to JSON or XML format
        // Return as byte array for download
        return userData.toString().getBytes();
    }


    // =================== PRIVATE HELPER METHODS ===================

    private void publishUserCreatedEvent(User user) {
        UserCreatedEvent event = UserCreatedEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .timestamp(LocalDateTime.now())
                .build();

        eventPublisher.publishEvent(event);
        log.debug("Published UserCreatedEvent for user: {}", user.getId());
    }

    private void publishUserUpdatedEvent(User user, String oldEmail) {
        UserUpdatedEvent event = UserUpdatedEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .oldEmail(oldEmail)
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .timestamp(LocalDateTime.now())
                .build();

        eventPublisher.publishEvent(event);
        log.debug("Published UserUpdatedEvent for user: {}", user.getId());
    }

    private String getCurrentUsername() {
        // TODO: Get from SecurityContext
        return "system";
    }

    /**
     * Handle email change business logic
     */
    private void handleEmailChange(User user, String oldEmail, String newEmail) {
        log.info("Handling email change for user {}: {} -> {}", user.getId(), oldEmail, newEmail);

        // Reset email verification status
        user.setEmailVerified(false);

        // TODO: Send verification email to new address
        // TODO: Send notification to old address
        // emailService.sendEmailChangeNotification(oldEmail, newEmail);
        // emailService.sendEmailVerification(user, newEmail);

        log.debug("Email change handled for user: {}", user.getId());
    }

    /**
     * Handle phone number change business logic
     */
    private void handlePhoneNumberChange(User user, String oldPhoneNumber, String newPhoneNumber) {
        log.info("Handling phone change for user {}: {} -> {}", user.getId(), oldPhoneNumber, newPhoneNumber);

        // Reset phone verification status
        user.setPhoneVerified(false);

        // TODO: Send verification SMS to new number
        // TODO: Send notification to old number if available
        // smsService.sendPhoneChangeNotification(oldPhoneNumber, newPhoneNumber);
        // smsService.sendPhoneVerification(user, newPhoneNumber);

        log.debug("Phone change handled for user: {}", user.getId());
    }

    /**
     * Update user preferences from request
     */
    private void updateUserPreferences(User user, UpdateUserRequest request) {
        if (user.getPreference() == null) {
            user.setPreference(UserPreference.builder().user(user).build());
        }

        UserPreference preferences = user.getPreference();

        // Update preference fields if provided
        if (request.getLanguage() != null) {
            preferences.setLanguage(request.getLanguage());
        }
        if (request.getTimezone() != null) {
            preferences.setTimezone(request.getTimezone());
        }
        if (request.getEmailNotifications() != null) {
            preferences.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getPushNotifications() != null) {
            preferences.setPushNotifications(request.getPushNotifications());
        }
        if (request.getSmsNotifications() != null) {
            preferences.setSmsNotifications(request.getSmsNotifications());
        }
        if (request.getMarketingEmails() != null) {
            preferences.setMarketingEmails(request.getMarketingEmails());
        }
    }
}
