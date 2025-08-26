package in.wealthinker.wealthinker.modules.user.event;

import in.wealthinker.wealthinker.shared.enums.UserRole;
import in.wealthinker.wealthinker.shared.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserUpdatedEvent {

    private Long userId;
    private String oldEmail;
    private String email;
    private String username;

    private UserRole oldRole;
    private UserRole role;

    private UserStatus status;
    private LocalDateTime timestamp;

    // Additional context for event handlers
    private String source; // "web", "mobile", "api", "admin"
    private String userAgent;
    private String ipAddress;
}
