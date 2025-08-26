package in.wealthinker.wealthinker.modules.user.dto.request;

import in.wealthinker.wealthinker.shared.validation.ValidPassword;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 128, message = "Password should be between 8 and 128 characters")
    @ValidPassword(message = "New password does not meet security requirements")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @AssertTrue(message = "New password and confirmation must match")
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }

    @AssertTrue(message = "New password must be different from current password")
    public boolean isPasswordDifferent() {
        return currentPassword == null || newPassword == null ||
                !currentPassword.equals(newPassword);
    }
}
