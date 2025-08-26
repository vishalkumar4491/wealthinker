package in.wealthinker.wealthinker.modules.user.service.impl;

import in.wealthinker.wealthinker.modules.user.dto.request.CreateUserRequest;
import in.wealthinker.wealthinker.modules.user.dto.request.UpdateUserRequest;
import in.wealthinker.wealthinker.modules.user.entity.User;
import in.wealthinker.wealthinker.modules.user.repository.UserRepository;
import in.wealthinker.wealthinker.modules.user.service.UserValidationService;
import in.wealthinker.wealthinker.shared.enums.UserRole;
import in.wealthinker.wealthinker.shared.enums.UserStatus;
import in.wealthinker.wealthinker.shared.exceptions.BusinessException;
import in.wealthinker.wealthinker.shared.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

/**
 * User Validation Service Implementation
 *
 * VALIDATION STRATEGY:
 * - Fail fast approach (throw exception on first violation)
 * - Detailed error messages for better UX
 * - Comprehensive logging for debugging
 * - Business rule centralization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationServiceImpl implements UserValidationService {

    private final UserRepository userRepository;

    // Valid status transitions matrix
    private static final Set<StatusTransition> VALID_TRANSITIONS = Set.of(
            new StatusTransition(UserStatus.PENDING_VERIFICATION, UserStatus.ACTIVE),
            new StatusTransition(UserStatus.PENDING_VERIFICATION, UserStatus.DELETED),
            new StatusTransition(UserStatus.ACTIVE, UserStatus.INACTIVE),
            new StatusTransition(UserStatus.ACTIVE, UserStatus.SUSPENDED),
            new StatusTransition(UserStatus.ACTIVE, UserStatus.DELETED),
            new StatusTransition(UserStatus.INACTIVE, UserStatus.ACTIVE),
            new StatusTransition(UserStatus.INACTIVE, UserStatus.DELETED),
            new StatusTransition(UserStatus.SUSPENDED, UserStatus.ACTIVE),
            new StatusTransition(UserStatus.SUSPENDED, UserStatus.DELETED),
            new StatusTransition(UserStatus.PENDING_KYC, UserStatus.ACTIVE),
            new StatusTransition(UserStatus.PENDING_KYC, UserStatus.DELETED),
            new StatusTransition(UserStatus.KYC_REJECTED, UserStatus.PENDING_KYC),
            new StatusTransition(UserStatus.KYC_REJECTED, UserStatus.DELETED)
    );

    @Override
    public void validateUserCreation(CreateUserRequest request) {
        log.debug("Validating user creation for email: {}", request.getEmail());

        // 1. Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email address is already registered: " + request.getEmail());
        }

        // 2. Validate username uniqueness (if provided)
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new ValidationException("Username is already taken: " + request.getUsername());
            }
        }

        // 3. Validate phone number uniqueness (if provided)
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new ValidationException("Phone number is already registered: " + request.getPhoneNumber());
            }
        }

        // 4. Validate age requirement (18+ for financial services)
        if (request.getDateOfBirth() != null) {
            LocalDate eighteenYearsAgo = LocalDate.now().minusYears(18);
            if (request.getDateOfBirth().isAfter(eighteenYearsAgo)) {
                throw new ValidationException("User must be at least 18 years old to register");
            }
        }

        // 5. Validate password confirmation
        if (!Objects.equals(request.getPassword(), request.getConfirmPassword())) {
            throw new ValidationException("Password and confirmation do not match");
        }

        // 6. Validate terms acceptance
        if (!Boolean.TRUE.equals(request.getAgreeToTerms())) {
            throw new ValidationException("Terms and conditions must be accepted");
        }

        if (!Boolean.TRUE.equals(request.getAcceptPrivacyPolicy())) {
            throw new ValidationException("Privacy policy must be accepted");
        }

        log.debug("User creation validation passed for email: {}", request.getEmail());
    }

    @Override
    public void validateUserUpdate(User existingUser, UpdateUserRequest request) {
        log.debug("Validating user update for user ID: {}", existingUser.getId());

        // 0. Validate at least one field is being updated
        if (!request.hasUpdates()) {
            throw new ValidationException("At least one field must be provided for update");
        }

        // 1. Validate email uniqueness (excluding current user)
        if (request.getEmail() != null && !request.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ValidationException("Email address is already in use: " + request.getEmail());
            }

            // Additional email validation
            if (!request.isEmailValid()) {
                throw new ValidationException("Invalid email format: " + request.getEmail());
            }
        }

        // 2. Validate username uniqueness (excluding current user)
        if (request.getUsername() != null && !request.getUsername().equals(existingUser.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new ValidationException("Username is already taken: " + request.getUsername());
            }

            // Additional username validation
            if (!request.isUsernameValid()) {
                throw new ValidationException("Invalid username format: " + request.getUsername());
            }
        }

        // 3. Validate phone number uniqueness (excluding current user)
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(existingUser.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new ValidationException("Phone number is already registered: " + request.getPhoneNumber());
            }
        }

        // 4. Validate critical field changes (email changes may require re-verification)
        if (request.getEmail() != null && !request.getEmail().equals(existingUser.getEmail())) {
            log.warn("Email change requested for user {}: {} -> {}",
                    existingUser.getId(), existingUser.getEmail(), request.getEmail());
            // Consider requiring email verification for email changes
        }

        // 5. Validate timezone if provided
        if (request.getTimezone() != null) {
            try {
                java.time.ZoneId.of(request.getTimezone());
            } catch (Exception e) {
                throw new ValidationException("Invalid timezone: " + request.getTimezone());
            }
        }

        // 6. Validate language code if provided
        if (request.getLanguage() != null) {
            try {
                java.util.Locale.forLanguageTag(request.getLanguage());
            } catch (Exception e) {
                throw new ValidationException("Invalid language code: " + request.getLanguage());
            }
        }

        // 7. Log critical changes for security
        if (request.hasCriticalUpdates()) {
            log.warn("Critical field update requested for user {}: {}",
                    existingUser.getId(), request.getUpdatedFields());
        }


        log.debug("User update validation passed for user ID: {}", existingUser.getId());
    }

    @Override
    public void validateStatusTransition(UserStatus fromStatus, UserStatus toStatus) {
        log.debug("Validating status transition: {} -> {}", fromStatus, toStatus);

        if (fromStatus == toStatus) {
            throw new ValidationException("User is already in status: " + toStatus);
        }

        StatusTransition transition = new StatusTransition(fromStatus, toStatus);
        if (!VALID_TRANSITIONS.contains(transition)) {
            throw new BusinessException(
                    String.format("Invalid status transition from %s to %s", fromStatus, toStatus),
                    "INVALID_STATUS_TRANSITION"
            );
        }

        log.debug("Status transition validation passed: {} -> {}", fromStatus, toStatus);
    }

    @Override
    public void validateRoleAssignment(User user, UserRole newRole, User assigningUser) {
        log.debug("Validating role assignment for user {} to role {}", user.getId(), newRole);

        // 1. Check if assigning user has permission to assign this role
        if (!canAssignRole(assigningUser.getRole(), newRole)) {
            throw new BusinessException(
                    String.format("Insufficient permissions to assign role %s", newRole),
                    "INSUFFICIENT_PERMISSIONS"
            );
        }

        // 2. Validate role upgrade requirements
        if (isRoleUpgrade(user.getRole(), newRole)) {
            validateRoleUpgradeRequirements(user, newRole);
        }

        // 3. Special validation for admin roles
        if (newRole.getHierarchyLevel() >= UserRole.ADMIN.getHierarchyLevel()) {
            validateAdminRoleAssignment(user, newRole);
        }

        log.debug("Role assignment validation passed for user {}", user.getId());
    }

    @Override
    public void validateAccountDeletion(User user, String reason) {
        log.debug("Validating account deletion for user {}, reason: {}", user.getId(), reason);

        // 1. Cannot delete system accounts
        if (isSystemAccount(user)) {
            throw new BusinessException(
                    "Cannot delete system account: " + user.getEmail(),
                    "SYSTEM_ACCOUNT_DELETION"
            );
        }

        // 2. Cannot delete super admin if it's the last one
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            long superAdminCount = userRepository.countByRole(UserRole.SUPER_ADMIN);
            if (superAdminCount <= 1) {
                throw new BusinessException(
                        "Cannot delete the last super admin account",
                        "LAST_SUPER_ADMIN_DELETION"
                );
            }
        }

        // 3. Validate reason is provided for audit trail
        if (reason == null || reason.trim().isEmpty()) {
            throw new ValidationException("Deletion reason is required for audit purposes");
        }

        // 4. TODO: Check for active transactions, portfolios, etc.
        // This would involve checking other modules for user dependencies

        log.debug("Account deletion validation passed for user {}", user.getId());
    }

    // =================== PRIVATE HELPER METHODS ===================

    private boolean canAssignRole(UserRole assignerRole, UserRole targetRole) {
        // Only users with higher hierarchy level can assign roles
        return assignerRole.getHierarchyLevel() > targetRole.getHierarchyLevel();
    }

    private boolean isRoleUpgrade(UserRole currentRole, UserRole newRole) {
        return newRole.getHierarchyLevel() > currentRole.getHierarchyLevel();
    }

    private void validateRoleUpgradeRequirements(User user, UserRole newRole) {
        // Premium/Pro roles require verified email and completed profile
        if (newRole == UserRole.PREMIUM || newRole == UserRole.PRO) {
            if (!user.getEmailVerified()) {
                throw new BusinessException(
                        "Email verification required for " + newRole.getDisplayName(),
                        "EMAIL_VERIFICATION_REQUIRED"
                );
            }

            if (user.getProfile() == null || !user.getProfile().getProfileCompleted()) {
                throw new BusinessException(
                        "Profile completion required for " + newRole.getDisplayName(),
                        "PROFILE_COMPLETION_REQUIRED"
                );
            }
        }

        // Admin roles require additional verification
        if (newRole.getHierarchyLevel() >= UserRole.ADMIN.getHierarchyLevel()) {
            // TODO: Implement additional admin role requirements
            // - Background check completion
            // - Two-factor authentication enabled
            // - Manager approval
        }
    }

    private void validateAdminRoleAssignment(User user, UserRole newRole) {
        // Admin roles require two-factor authentication
        if (user.getPreference() == null || !user.getPreference().getTwoFactorEnabled()) {
            throw new BusinessException(
                    "Two-factor authentication required for admin roles",
                    "TWO_FACTOR_REQUIRED"
            );
        }

        // Additional admin role validations
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(
                    "User must be active to receive admin role",
                    "INACTIVE_USER_ADMIN_ROLE"
            );
        }
    }

    private boolean isSystemAccount(User user) {
        // Define what constitutes a system account
        return user.getEmail().endsWith("@system.wealthinker.com") ||
                user.getUsername() != null && user.getUsername().startsWith("system_");
    }

    // Helper record for status transitions
    private record StatusTransition(UserStatus from, UserStatus to) {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            StatusTransition that = (StatusTransition) obj;
            return from == that.from && to == that.to;
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to);
        }
    }
}
