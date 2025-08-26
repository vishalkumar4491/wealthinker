package in.wealthinker.wealthinker.modules.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * User Session Entity - Track active user sessions
 *
 * PURPOSE:
 * - Security monitoring and session management
 * - Multi-device session tracking
 * - Session revocation capability
 * - Audit trail for user access
 */
@Entity
@Table(name = "user_sessions",
        indexes = {
                @Index(name = "idx_session_user_id", columnList = "user_id"),
                @Index(name = "idx_session_id", columnList = "session_id"),
                @Index(name = "idx_session_active", columnList = "active"),
                @Index(name = "idx_session_created_at", columnList = "created_at")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_session_user"))
    private User user;

    @Column(name = "session_id", nullable = false, unique = true, length = 255)
    private String sessionId;

    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    @Column(name = "ip_address", length = 45) // Support IPv6
    private String ipAddress;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isActive() {
        return active && !isExpired() && revokedAt == null;
    }
}