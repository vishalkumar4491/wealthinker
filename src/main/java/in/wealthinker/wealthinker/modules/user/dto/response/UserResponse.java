package in.wealthinker.wealthinker.modules.user.dto.response;

import java.time.LocalDateTime;

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

    private Long id;
    private String userName;
    private String email;
    private String fullName;  
    private UserRole role;
    private UserStatus status;
    private AuthProvider provider;
    private Boolean emailVerified;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ProfileResponse profile;
    private PreferenceResponse preferences;
}

