package in.wealthinker.wealthinker.shared.utils;


import in.wealthinker.wealthinker.modules.auth.security.UserPrincipal;
import in.wealthinker.wealthinker.shared.enums.Permission;
import in.wealthinker.wealthinker.shared.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Security Utility Methods
 *
 * PURPOSE:
 * - Common security operations
 * - Get current user information
 * - Check permissions and roles
 * - Validate access to resources
 */
public class SecurityUtils {
    /**
     * Get current authenticated user
     */
    public static Optional<UserPrincipal> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return Optional.of(userPrincipal);
        }

        return Optional.empty();
    }

    /**
     * Get current user ID
     */
    public static Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(UserPrincipal::getId);
    }

    /**
     * Get current user email
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUser().map(UserPrincipal::getEmail);
    }

    /**
     * Get current username (actual username, not email)
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUser().map(UserPrincipal::getActualUsername);
    }

    /**
     * Check if current user has specific role
     */
    public static boolean hasRole(UserRole role) {
        return getCurrentUser()
                .map(user -> user.hasRole(role))
                .orElse(false);
    }

    /**
     * Check if current user has any of the specified roles
     */
    public static boolean hasAnyRole(UserRole... roles) {
        return getCurrentUser()
                .map(user -> user.hasAnyRole(roles))
                .orElse(false);
    }

    /**
     * Check if current user has specific permission
     */
    public static boolean hasPermission(Permission permission) {
        return getCurrentUser()
                .map(user -> user.hasAuthority(permission.name()))
                .orElse(false);
    }

    /**
     * Check if current user is admin or higher
     */
    public static boolean isAdmin() {
        return hasRole(UserRole.ADMIN) || hasRole(UserRole.SUPER_ADMIN);
    }

    /**
     * Check if current user can access resource
     */
    public static boolean canAccessUser(Long userId) {
        return getCurrentUserId()
                .map(currentUserId -> currentUserId.equals(userId) || isAdmin())
                .orElse(false);
    }

    /**
     * Check if current user can perform admin operations
     */
    public static boolean canPerformAdminOperations() {
        return hasRole(UserRole.ADMIN) || hasRole(UserRole.SUPER_ADMIN);
    }

    /**
     * Get current user role
     */
    public static Optional<UserRole> getCurrentUserRole() {
        return getCurrentUser().map(UserPrincipal::getRole);
    }

    /**
     * Check if current user account is fully verified
     */
    public static boolean isCurrentUserVerified() {
        return getCurrentUser()
                .map(UserPrincipal::isFullyVerified)
                .orElse(false);
    }

    /**
     * Get current user display name
     */
    public static Optional<String> getCurrentUserDisplayName() {
        return getCurrentUser().map(UserPrincipal::getDisplayName);
    }
}
