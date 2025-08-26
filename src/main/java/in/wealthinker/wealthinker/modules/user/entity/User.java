package in.wealthinker.wealthinker.modules.user.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import in.wealthinker.wealthinker.shared.enums.AuthProvider;
import in.wealthinker.wealthinker.shared.enums.UserRole;
import in.wealthinker.wealthinker.shared.enums.UserStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * Core User Entity
 *
 * Design Decisions:
 * 1. Separate profile data into UserProfile entity (Single Responsibility)
 * 2. Use phone number as unique identifier for OTP-based login
 * 3. Track login attempts for security (brute force protection)
 * 4. Support multiple authentication providers (OAuth2 ready)
 * 5. Audit fields for compliance (who created/modified when)
 */
@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_username", columnList = "username"),
                @Index(name = "idx_users_phone", columnList = "phone_number"),
                @Index(name = "idx_users_status", columnList = "status"),
                @Index(name = "idx_users_created_at", columnList = "created_at"),
                @Index(name = "idx_users_last_login", columnList = "last_login_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "phone_number")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded=true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Email(message = "Email must be valid")
    @Size(max = 50, message = "Email should not exceed 50 characters")
    @Column(nullable = false, unique = true)
    private String email;

    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Username can only contain letters, numbers, dots, underscores and hyphens")
    @Column(unique = true, length = 20)
    private String username;

    @Pattern(
    regexp = "^(?:[6-9]\\d{9}|\\+?[1-9]\\d{1,14})$",
    message = "Phone number must be valid Indian or international format"
    )
    @Column(name = "phone_number", unique = true, length = 20)
    @Size(min = 10, max = 20)
    private String phoneNumber;

    @Size(max = 128, message = "Password hash too long")
    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.FREE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    // OAuth2 support
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(name = "provider_id")
    private String providerId;

    // Verification status
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    @Builder.Default
    private Boolean phoneVerified = false;

    // Account status
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Security tracking
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip")
    private String lastLoginIp;

    @Column(name = "login_attempts", nullable = false)
    @Builder.Default
    private Integer loginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    // Relationships (Lazy loading for performance)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserProfile profile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserPreference preference;

    // Audit fields (JPA Auditing)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Helper methods

    /**
     * Check if account is currently locked due to failed login attempts
     */
    public boolean isAccountLocked() {
        return accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * Check if user is using OAuth2 authentication
     */
    public boolean isOAuth2User() {
        return provider != AuthProvider.LOCAL;
    }

    public String getDisplayName() {
        if (profile != null && profile.getFirstName() != null) {
            return profile.getFullName();
        }
        if (username != null) {
            return username;
        }
        return email;
    }

    /**
     * Check if user can perform actions that require verification
     */
    public boolean isFullyVerified() {
        return emailVerified && (phoneNumber == null || phoneVerified);
    }

    // Helper method to get login identifier type
    public String getIdentifierType(String identifier) {
        if (identifier.equals(email)) return "email";
        if (identifier.equals(username)) return "username";
        if (identifier.equals(phoneNumber)) return "phone";
        return "unknown";
    }

    @PrePersist
    protected void onCreate() {
        if (createdBy == null) {
            createdBy = "system";
        }
        updatedBy = createdBy;
    }

    @PreUpdate
    protected void onUpdate() {
        if (updatedBy == null) {
            updatedBy = "system";
        }
    }
}
