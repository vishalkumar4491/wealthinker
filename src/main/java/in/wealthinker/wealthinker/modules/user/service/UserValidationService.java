package in.wealthinker.wealthinker.modules.user.service;

import in.wealthinker.wealthinker.modules.user.dto.request.CreateUserRequest;
import in.wealthinker.wealthinker.modules.user.dto.request.UpdateUserRequest;
import in.wealthinker.wealthinker.modules.user.entity.User;
import in.wealthinker.wealthinker.shared.enums.UserRole;
import in.wealthinker.wealthinker.shared.enums.UserStatus;

/**
 * User Validation Service - Business rule validation
 *
 * PURPOSE:
 * - Centralize all business validation logic
 * - Separate validation from core business operations
 * - Provide reusable validation methods
 * - Ensure consistency across all user operations
 *
 * DESIGN PATTERN: Strategy Pattern for different validation rules
 */
public interface UserValidationService {

    /**
     * Validate user creation request
     *
     * BUSINESS RULES:
     * - Email must be unique and valid
     * - Username must be available (if provided)
     * - Phone number must be unique (if provided)
     * - Password must meet security requirements
     * - User must be of legal age (if DOB provided)
     * - Terms acceptance required
     */
     void validateUserCreation(CreateUserRequest request);

    /**
     * Validate user update request
     *
     * BUSINESS RULES:
     * - Email uniqueness (excluding current user)
     * - Username availability (excluding current user)
     * - Profile data consistency
     */
     void validateUserUpdate(User existingUser, UpdateUserRequest request);

    /**
     * Validate status transition
     *
     * BUSINESS RULES:
     * - Valid status transitions only
     * - Special permissions for certain transitions
     */
     void validateStatusTransition(UserStatus fromStatus, UserStatus toStatus);

    /**
     * Validate role assignment
     *
     * SECURITY: Role hierarchy and permission validation
     */
     void validateRoleAssignment(User user, UserRole newRole, User assigningUser);

    /**
     * Validate account deletion
     *
     * BUSINESS RULES:
     * - Cannot delete certain system accounts
     * - Active transactions must be handled
     */
     void validateAccountDeletion(User user, String reason);
}
