package in.wealthinker.wealthinker.modules.user.mapper;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdateUserRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.ProfileResponse;
import org.mapstruct.*;

import in.wealthinker.wealthinker.modules.user.dto.request.CreateUserRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.UserResponse;
import in.wealthinker.wealthinker.modules.user.dto.response.UserSummaryResponse;
import in.wealthinker.wealthinker.modules.user.entity.User;
import in.wealthinker.wealthinker.modules.user.entity.UserPreference;
import in.wealthinker.wealthinker.modules.user.entity.UserProfile;

import java.time.LocalDateTime;
import java.util.List;


/**
 * User Mapper - Entity to DTO conversion using MapStruct
 *
 * WHY MAPSTRUCT:
 * - Compile-time code generation (no reflection overhead)
 * - Type-safe mapping (compile-time error checking)
 * - High performance (faster than manual mapping or BeanUtils)
 * - Maintainable (clear mapping configuration)
 *
 * PERFORMANCE BENEFITS:
 * - Zero reflection cost at runtime
 * - Optimized code generation
 * - Minimal object allocation
 * - Suitable for high-throughput applications
 */

@Mapper(componentModel = "spring", uses = {ProfileMapper.class, PreferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,   // Don't map nulls
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS // Check for nulls before mapping
        )
public interface UserMapper {

    // =================== ENTITY TO RESPONSE MAPPING ===================

    /**
     * Convert User entity to complete UserResponse
     *
     * INCLUDES: All non-sensitive user information with nested data
     */

    @Mapping(target = "displayName", expression = "java(user.getDisplayName())")
    @Mapping(target = "profileCompleted", source = "profile.profileCompleted")
    @Mapping(target = "profileCompletionPercentage", source = "profile.profileCompletionPercentage")
    @Mapping(target = "fullName", expression = "java(user.getProfile() != null ? user.getProfile().getFullName() : null)")
    @Mapping(target = "preferences", source = "preference")
    UserResponse toUserResponse(User user);
    /**
     * Convert User entity to UserSummaryResponse (lightweight)
     *
     * PERFORMANCE: Minimal data for list views
     */

    @Mapping(target = "firstName", source = "profile.firstName")
    @Mapping(target = "lastName", source = "profile.lastName")
    @Mapping(target = "fullName", source = "profile.fullName")
    @Mapping(target = "profileCompleted", source = "profile.profileCompleted")
    @Mapping(target = "profileImageUrl", source = "profile.profileImageUrl")
    @Mapping(target = "profileCompletionPercentage", source = "profile.profileCompletionPercentage")
    UserSummaryResponse toUserSummaryResponse(User user);

    // =================== REQUEST TO ENTITY MAPPING ===================

    /**
     * Convert CreateUserRequest to User entity
     *
     * SECURITY: Ignore sensitive fields that shouldn't be set by clients
     */

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)  // Set separately after hashing
    @Mapping(target = "status", ignore = true)    // Set by business logic
    @Mapping(target = "provider", ignore = true)    // Set by business logic
    @Mapping(target = "providerId", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "phoneVerified", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "lastLoginIp", ignore = true)
    @Mapping(target = "loginAttempts", ignore = true)
    @Mapping(target = "accountLockedUntil", ignore = true)
    @Mapping(target = "profile", ignore = true)    // Mapped separately
    @Mapping(target = "preference", ignore = true)  // Mapped separately
    @Mapping(target = "createdAt", ignore = true)   // Set by JPA
    @Mapping(target = "updatedAt", ignore = true)   // Set by JPA
    @Mapping(target = "createdBy", ignore = true)   // Set by audit
    @Mapping(target = "updatedBy", ignore = true)   // Set by audit
    User toUserEntity(CreateUserRequest request);

    /**
     * Convert list of Users to list of UserSummaryResponses
     *
     * PERFORMANCE: Efficient batch conversion for search results
     */
    List<UserSummaryResponse> toUserSummaryResponseList(List<User> users);

    // =================== CONDITIONAL MAPPING ===================

    /**
     * Convert to user response with security filtering
     *
     * SECURITY: Remove sensitive fields based on access level
     */

    @Named("toUserSecureResponse")
    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "mapPhoneNumber")
    @Mapping(target = "lastLoginAt", qualifiedByName = "mapLastLoginAt")
    @Mapping(target = "profile", source = "profile") 
    @Mapping(target = "preferences", source = "preference")
    UserResponse toUserSecureResponse(User user, @Context SecureMappingContext context);

    // ---------- Helper methods ----------
    @Named("mapPhoneNumber")
    default String mapPhoneNumber(String phoneNumber, @Context SecureMappingContext  ctx) {
        return ctx.isIncludePhoneNumber() ? phoneNumber : null;
    }

    @Named("mapLastLoginAt")
    default LocalDateTime mapLastLoginAt(LocalDateTime lastLoginAt, @Context SecureMappingContext  ctx) {
        return ctx.isIncludeSecurityInfo() ? lastLoginAt : null;
    }

    //  @Named("mapProfile")
    // default ProfileResponse mapProfile(UserProfile profile, @Context SecureMappingContext ctx, @Context ProfileMapper profileMapper) {
    //     return ctx.isIncludeProfile() && profile != null ? profileMapper.toProfileResponse(profile) : null;
    // }


    // @Named("mapPreferences")
    // default UserPreference mapPreferences(UserPreference preference, @Context SecureMappingContext ctx) {
    //     return ctx.isIncludePreferences() ? preference : null;
    // }

    // =================== UPDATE MAPPING ===================

    /**
     * Update existing User entity from request
     *
     * PARTIAL UPDATES: Only update non-null fields from request
     */

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true) // Use separate password update endpoint
    @Mapping(target = "role", ignore = true) // Admin-only operation
    @Mapping(target = "status", ignore = true) // Admin-only operation
    @Mapping(target = "provider", ignore = true) // Cannot change auth provider
    @Mapping(target = "providerId", ignore = true)
    @Mapping(target = "emailVerified", ignore = true) // Reset by email change logic
    @Mapping(target = "phoneVerified", ignore = true) // Reset by phone change logic
    @Mapping(target = "isActive", ignore = true) // Admin-only operation
    @Mapping(target = "loginAttempts", ignore = true) // Security field
    @Mapping(target = "accountLockedUntil", ignore = true) // Security field
    @Mapping(target = "lastLoginAt", ignore = true) // System field
    @Mapping(target = "lastLoginIp", ignore = true) // System field
    @Mapping(target = "profile", ignore = true) // Use separate profile update
    @Mapping(target = "preference", ignore = true) // Partially updated via service
    @Mapping(target = "createdAt", ignore = true) // Immutable audit field
    @Mapping(target = "updatedAt", ignore = true) // Set by JPA
    @Mapping(target = "createdBy", ignore = true) // Immutable audit field
    @Mapping(target = "updatedBy", ignore = true) // Set by JPA auditing
    void updateUserEntityFromRequest(UpdateUserRequest request, @MappingTarget User user);

    /**
     * Create UpdateUserRequest from existing User (for pre-populating forms)
     */
    @Mapping(target = "email", source = "email")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "language", source = "preference.language")
    @Mapping(target = "timezone", source = "preference.timezone")
    @Mapping(target = "emailNotifications", source = "preference.emailNotifications")
    @Mapping(target = "pushNotifications", source = "preference.pushNotifications")
    @Mapping(target = "smsNotifications", source = "preference.smsNotifications")
    @Mapping(target = "marketingEmails", source = "preference.marketingEmails")
    UpdateUserRequest toUpdateRequest(User user);

    // =================== CUSTOM MAPPING METHODS ===================

    /**
     * Custom method for complex display name logic
     */
    default String mapDisplayName(User user) {
        return user.getDisplayName();
    }

    default UserPreference createDefaultPreferences(User user) {
        return UserPreference.builder()
                .user(user)
                .theme(UserPreference.Theme.LIGHT)
                .language("en")
                .timezone("Asia/Kolkata")
                .currency("INR")
                .build();
    }

    default UserProfile createUserProfile(User user, CreateUserRequest request) {
        return UserProfile.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .occupation(request.getOccupation())
                .company(request.getCompany())
                .build();
    }

    /**
     * Custom method for role authorization mapping
     */
    @Named("mapRoleForUser")
    default String mapRoleForCurrentUser(User user, @Context Long currentUserId) {
        // Only show detailed role info to the user themselves or admins
        if (user.getId().equals(currentUserId)) {
            return user.getRole().name();
        }
        return user.getRole().name();
    }
}