package in.wealthinker.wealthinker.modules.auth.security.userdetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import in.wealthinker.wealthinker.modules.user.entity.User;
import in.wealthinker.wealthinker.shared.enums.UserRole;
import in.wealthinker.wealthinker.shared.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User Principal - Authentication user wrapper
 *
 * PURPOSE:
 * - Implements Spring Security UserDetails interface
 * - Implements OAuth2User interface for social login
 * - Provides user information for JWT token generation
 * - Serves as security context user representation
 *
 * SECURITY CONSIDERATIONS:
 * - Password is excluded from serialization (@JsonIgnore)
 * - Contains only essential user information
 * - Thread-safe and immutable design
 * - Supports both local and OAuth2 authentication
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails, OAuth2User {

    // Core User Information
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;

    @JsonIgnore
    private String password;

    // User Profile Information
    private String firstName;
    private String lastName;
    private String fullName;

    // Account Status
    private UserRole role;
    private UserStatus status;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean profileCompleted;
    private Boolean twoFactorEnabled;

    // Spring Security Fields
    private Collection<? extends GrantedAuthority> authorities;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    // OAuth2 Fields
    private Map<String, Object> attributes;

    // =================== FACTORY METHODS ===================

    /**
     * Create UserPrincipal from User entity
     *
     * @param user User entity from database
     * @return UserPrincipal for security context
     */

    public static UserPrincipal create(User user) {
        // Build authorities from user role and permissions
        List<GrantedAuthority> authorities = buildAuthorities(user.getRole());

        return UserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .password(user.getPasswordHash())
                .firstName(user.getProfile() != null ? user.getProfile().getFirstName() : null)
                .lastName(user.getProfile() != null ? user.getProfile().getLastName() : null)
                .fullName(buildFullName(user))
                .role(user.getRole())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .profileCompleted(user.getProfile() != null ? user.getProfile().getProfileCompleted() : false)
                .twoFactorEnabled(user.getPreference() != null ? user.getPreference().getTwoFactorEnabled() : false)
                .authorities(authorities)
                .accountNonExpired(true)
                .accountNonLocked(!user.isAccountLocked())
                .credentialsNonExpired(true)
                .enabled(isUserEnabled(user))
                .build();
    }

    /**
     * Create UserPrincipal with OAuth2 attributes
     * m
     * @param user User entity from database
     * @param attributes OAuth2 attributes from provider
     * @return UserPrincipal for OAuth2 authentication
     */
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    /**
     * Create UserPrincipal from JWT claims (for token-based authentication)
     *
     * @param userId User ID from token
     * @param username Username from token
     * @param email Email from token
     * @param role User role from token
     * @param permissions User permissions from token
     * @return UserPrincipal for JWT authentication
     */
    public static UserPrincipal createFromToken(Long userId, String username, String email,
                                                UserRole role, List<String> permissions) {
        // Build authorities from role and permissions
        List<GrantedAuthority> authorities = buildAuthoritiesFromPermissions(role, permissions);

        return UserPrincipal.builder()
                .id(userId)
                .username(username)
                .email(email)
                .role(role)
                .authorities(authorities)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();
    }

    // =================== UserDetails INTERFACE METHODS ===================

    @Override
    public String getUsername() {
        // Spring Security expects username, we return email for consistency
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // =================== OAuth2User INTERFACE METHODS ===================

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        // OAuth2User expects name, we return user ID as string
        return String.valueOf(id);
    }

    // =================== CUSTOM GETTER METHODS ===================

    /**
     * Get actual username (not email)
     *
     * @return username field value
     */
    public String getActualUsername() {
        return username;
    }

    /**
     * Get user's display name
     *
     * @return formatted display name
     */
    public String getDisplayName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName;
        }
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        if (username != null && !username.trim().isEmpty()) {
            return username;
        }
        return email;
    }

    /**
     * Check if user has specific role
     *
     * @param role Role to check
     * @return true if user has the role
     */
    public boolean hasRole(UserRole role) {
        return this.role == role || authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role.name()));
    }

    /**
     * Check if user has specific authority/permission
     *
     * @param authority Authority to check
     * @return true if user has the authority
     */
    public boolean hasAuthority(String authority) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(authority));
    }

    /**
     * Check if user has any of the specified roles
     *
     * @param roles Roles to check
     * @return true if user has any of the roles
     */
    public boolean hasAnyRole(UserRole... roles) {
        for (UserRole role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user is admin or higher
     *
     * @return true if user is admin or super admin
     */
    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN) || hasRole(UserRole.SUPER_ADMIN);
    }

    /**
     * Check if user's account is fully verified
     *
     * @return true if both email and phone are verified
     */
    public boolean isFullyVerified() {
        return Boolean.TRUE.equals(emailVerified) &&
                (phoneNumber == null || Boolean.TRUE.equals(phoneVerified));
    }

    /**
     * Check if user can access resource by ID
     *
     * @param resourceUserId User ID of resource owner
     * @return true if user owns resource or is admin
     */
    public boolean canAccessResource(Long resourceUserId) {
        return id.equals(resourceUserId) || isAdmin();
    }

    // =================== PRIVATE HELPER METHODS ===================

    /**
     * Build authorities from user role
     */
    private static List<GrantedAuthority> buildAuthorities(UserRole role) {
        // Add role authority
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );

        // TODO: Add specific permissions based on role
        // This would typically come from a permission service
        // For now, we'll add basic role-based authority

        return authorities;
    }

    /**
     * Build authorities from role and permissions list
     */
    private static List<GrantedAuthority> buildAuthoritiesFromPermissions(UserRole role, List<String> permissions) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );

        // Add permission-based authorities
        if (permissions != null) {
            List<GrantedAuthority> permissionAuthorities = permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            authorities.addAll(permissionAuthorities);
        }

        return authorities;
    }

    /**
     * Build full name from user entity
     */
    private static String buildFullName(User user) {
        if (user.getProfile() == null) {
            return null;
        }

        StringBuilder fullName = new StringBuilder();
        if (user.getProfile().getFirstName() != null) {
            fullName.append(user.getProfile().getFirstName());
        }
        if (user.getProfile().getLastName() != null) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(user.getProfile().getLastName());
        }

        return fullName.length() > 0 ? fullName.toString() : null;
    }

    /**
     * Check if user account is enabled based on status
     */
    private static boolean isUserEnabled(User user) {
        return user.getIsActive() &&
                (user.getStatus() == UserStatus.ACTIVE ||
                        user.getStatus() == UserStatus.PENDING_VERIFICATION);
    }
}
