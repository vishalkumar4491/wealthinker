package in.wealthinker.wealthinker.modules.user.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import in.wealthinker.wealthinker.shared.enums.AuthProvider;
import in.wealthinker.wealthinker.shared.enums.UserRole;
import in.wealthinker.wealthinker.shared.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    // Core User Information
    private Long id;
    private String email;
    private String username;
    private String phoneNumber;

    // Account Status
    private String fullName;
    private UserRole role;
    private UserStatus status;
    private AuthProvider provider;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean isActive;

    // Security Information (limited)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private ProfileResponse profile;
    private PreferenceResponse preferences;

    // Computed Fields
    private Boolean profileCompleted;
    private Integer profileCompletionPercentage;
    private String displayName;

    /**
     * Factory method for creating response without sensitive data
     *
     * SECURITY: Use for public APIs or when user lacks permissions
     */
    public static UserResponse createPublic(UserResponse full) {
        return UserResponse.builder()
                .id(full.getId())
                .username(full.getUsername())
                .role(full.getRole())
                .emailVerified(full.getEmailVerified())
                .profileCompleted(full.getProfileCompleted())
                .displayName(full.getDisplayName())
                .createdAt(full.getCreatedAt())
                .preferences(full.getPreferences())
                .build();
    }
}

