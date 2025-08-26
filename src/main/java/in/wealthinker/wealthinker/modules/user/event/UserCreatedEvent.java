package in.wealthinker.wealthinker.modules.user.event;

import in.wealthinker.wealthinker.shared.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * User Created Event - Published when a new user is registered
 *
 * PURPOSE:
 * - Decouple user creation from side effects
 * - Enable system integration without tight coupling
 * - Support async processing of user creation workflows
 * - Maintain audit trail of user events
 *
 * EVENT-DRIVEN BENEFITS:
 * - Email service can send welcome email
 * - Analytics service can track user acquisition
 * - Marketing service can add to nurture campaigns
 * - Audit service can log user creation
 */
@Data
@Builder
public class UserCreatedEvent {
    private Long userId;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private UserRole role;
    private LocalDateTime timestamp;

    // Additional context for event handlers
    private String source; // "web", "mobile", "api", "admin"
    private String userAgent;
    private String ipAddress;
    private String referralCode;
}
