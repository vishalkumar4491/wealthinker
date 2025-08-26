package in.wealthinker.wealthinker.shared.events;

import in.wealthinker.wealthinker.shared.enums.UserRole;
import in.wealthinker.wealthinker.shared.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserUpdatedEvent {
    private Long userId;
    private String email;
    private String oldEmail;
    private String username;
    private UserRole role;
    private UserRole oldRole;
    private UserStatus status;
    private UserStatus oldStatus;
    private LocalDateTime timestamp;

    // Additional context
    private String updatedBy;
    private String updateReason;
}
