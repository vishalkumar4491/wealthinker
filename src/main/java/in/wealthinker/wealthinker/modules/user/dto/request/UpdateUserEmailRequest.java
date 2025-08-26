package in.wealthinker.wealthinker.modules.user.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Update User Email Request DTO
 *
 * SECURITY CONSIDERATIONS:
 * - Email changes require password confirmation
 * - May trigger email re-verification process
 * - Both old and new emails get notifications
 */

@Data
public class UpdateUserEmailRequest {
    @NotBlank(message = "New email is required")
    @Email(message = "Email must be valid")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    private String newEmail;

    @NotBlank(message = "Password confirmation is required for email changes")
    private String password;

    @AssertTrue(message = "You must acknowledge that email verification will be required")
    private Boolean acknowledgeVerificationRequired;
}
