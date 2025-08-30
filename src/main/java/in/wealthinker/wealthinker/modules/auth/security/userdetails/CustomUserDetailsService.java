package in.wealthinker.wealthinker.modules.auth.security.userdetails;

import in.wealthinker.wealthinker.modules.user.entity.User;
import in.wealthinker.wealthinker.modules.user.repository.UserRepository;
import in.wealthinker.wealthinker.shared.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom User Details Service - Loads user details for Spring Security
 *
 * PURPOSE:
 * - Implementation of Spring Security's UserDetailsService
 * - Loads user from database for authentication
 * - Creates UserPrincipal object for security context
 * - Handles user status validation
 *
 * USED BY:
 * - DaoAuthenticationProvider for username/password authentication
 * - JWT authentication filter for user loading
 * - Spring Security authentication manager
 *
 * SECURITY CONSIDERATIONS:
 * - Only loads active and enabled users
 * - Validates account status before creating UserDetails
 * - Handles user not found scenarios gracefully
 * - Provides detailed logging for security monitoring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username (email) for Spring Security authentication
     *
     * AUTHENTICATION FLOW:
     * 1. Called by Spring Security during authentication
     * 2. Loads user from database by email
     * 3. Validates user status and account state
     * 4. Creates UserPrincipal object with authorities
     * 5. Returns UserDetails for authentication process
     *
     * @param username User identifier (email address in our case)
     * @return UserDetails object containing user information and authorities
     * @throws UsernameNotFoundException if user not found or invalid
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        try {
            // Load user from database
            // Note: We use email as username in our system
            User user = userRepository.findByEmailAndIsActiveTrue(username)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "User not found with email: " + username));

            // Validate user account status
            validateUserAccount(user, username);

            // Create UserPrincipal from User entity
            UserPrincipal userPrincipal = UserPrincipal.create(user);

            log.debug("User loaded successfully: email={}, role={}, enabled={}",
                    user.getEmail(), user.getRole(), userPrincipal.isEnabled());

            return userPrincipal;

        } catch (UsernameNotFoundException e) {
            log.warn("User not found during authentication: {}", username);
            throw e;
        } catch (Exception e) {
            log.error("Error loading user by username: {}", username, e);
            throw new UsernameNotFoundException("Error loading user: " + username, e);
        }
    }

    /**
     * Load user by ID for token-based authentication
     *
     * USED BY:
     * - JWT authentication when refreshing tokens
     * - Internal services that need user details by ID
     * - User profile operations requiring fresh user data
     *
     * @param userId User database ID
     * @return UserDetails object
     * @throws UsernameNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.debug("Loading user by ID: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "User not found with id: " + userId));

            // Check if user is still active
            if (!user.getIsActive()) {
                throw new UsernameNotFoundException("User account is deactivated: " + userId);
            }

            // Validate user account status
            validateUserAccount(user, String.valueOf(userId));

            UserPrincipal userPrincipal = UserPrincipal.create(user);

            log.debug("User loaded by ID successfully: userId={}, email={}, role={}",
                    userId, user.getEmail(), user.getRole());

            return userPrincipal;

        } catch (UsernameNotFoundException e) {
            log.warn("User not found by ID: {}", userId);
            throw e;
        } catch (Exception e) {
            log.error("Error loading user by ID: {}", userId, e);
            throw new UsernameNotFoundException("Error loading user with ID: " + userId, e);
        }
    }

    /**
     * Check if user exists by email
     *
     * @param email User email address
     * @return true if user exists and is active
     */
    @Transactional(readOnly = true)
    public boolean userExists(String email) {
        log.debug("Checking if user exists: {}", email);

        boolean exists = userRepository.existsByEmail(email);

        log.debug("User existence check result for {}: {}", email, exists);
        return exists;
    }

    /**
     * Validate user account status and constraints
     *
     * VALIDATION RULES:
     * - User must be active
     * - Account must not be locked
     * - Account status must be valid for authentication
     * - Account must not be expired
     *
     * @param user User entity to validate
     * @param identifier User identifier for logging
     * @throws UsernameNotFoundException if validation fails
     */
    private void validateUserAccount(User user, String identifier) {
        // Check if user account is active
        if (!user.getIsActive()) {
            log.warn("Authentication attempt for inactive user: {}", identifier);
            throw new UsernameNotFoundException("User account is deactivated: " + identifier);
        }

        // Check account lock status
        if (user.isAccountLocked()) {
            log.warn("Authentication attempt for locked user: {}", identifier);
            throw new UsernameNotFoundException("User account is locked: " + identifier);
        }

        // Check user status for authentication eligibility
        UserStatus status = user.getStatus();
        if (!isStatusValidForAuthentication(status)) {
            log.warn("Authentication attempt for user with invalid status: {} - status: {}",
                    identifier, status);
            throw new UsernameNotFoundException(
                    "User account status does not allow authentication: " + status);
        }

        // Additional validations can be added here
        // For example: email verification requirements, terms acceptance, etc.
    }

    /**
     * Check if user status allows authentication
     *
     * VALID STATUSES FOR AUTHENTICATION:
     * - ACTIVE: Fully verified and active users
     * - PENDING_VERIFICATION: Can login but with limited features
     * - PENDING_KYC: Can login but may have restrictions
     *
     * INVALID STATUSES:
     * - INACTIVE: Account deactivated
     * - SUSPENDED: Account suspended by admin
     * - DELETED: Account soft deleted
     * - KYC_REJECTED: KYC verification failed
     *
     * @param status User account status
     * @return true if status allows authentication
     */
    private boolean isStatusValidForAuthentication(UserStatus status) {
        return switch (status) {
            case ACTIVE, PENDING_VERIFICATION, PENDING_KYC -> true;
            case INACTIVE, SUSPENDED, DELETED, KYC_REJECTED -> false;
            default -> false;
        };
    }
}
