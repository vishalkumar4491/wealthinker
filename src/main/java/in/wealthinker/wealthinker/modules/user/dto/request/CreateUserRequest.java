package in.wealthinker.wealthinker.modules.user.dto.request;

import java.time.LocalDate;

import in.wealthinker.wealthinker.modules.user.entity.UserProfile;
import in.wealthinker.wealthinker.shared.enums.UserRole;
import in.wealthinker.wealthinker.shared.validation.ValidPassword;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 50, message = "Email should not exceed 50 characters")
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Username can only contain letters, numbers, dots, underscores and hyphens")
    private String username;

    @Pattern(
    regexp = "^(?:[6-9]\\d{9}|\\+?[1-9]\\d{1,14})$",
    message = "Phone number must be valid Indian or international format"
    )
    @Size(min = 10, max = 20)
    private String phoneNumber;

    // Password with custom validation
    @ValidPassword(message = "Password must meet security requirements")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    // Personal details

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 20, message = "First name should be between 2 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'.-]+$", message = "First name can only contain letters, spaces, apostrophes, dots, and hyphens")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 20, message = "Last name should be between 2 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'.-]+$", message = "Last name can only contain letters, spaces, apostrophes, dots, and hyphens")
    private String lastName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private UserProfile.Gender gender;

    @Size(max = 50, message = "Occupation should not exceed 50 characters")
    private String occupation;

    @Size(max = 50, message = "Company should not exceed 50 characters")
    private String company;

    private UserRole role = UserRole.FREE;

    // Terms and conditions acceptance
    @AssertTrue(message = "You must agree to the terms and conditions")
    private Boolean agreeToTerms;

    @AssertTrue(message = "You must accept the privacy policy")
    private Boolean acceptPrivacyPolicy;

    // Marketing consent (GDPR compliance)
    private Boolean marketingConsent = false; // Default to false (opt-in)

    // Custom validation methods

    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }

    @AssertTrue(message = "You must be at least 16 years old")
    public boolean isOfLegalAge() {
        if (dateOfBirth == null) {
            return true; // Optional field, let other validations handle
        }
        return dateOfBirth.isBefore(LocalDate.now().minusYears(16));
    }

    @AssertTrue(message = "Required consents must be provided")
    public boolean
    requirePrivacyPolicyAcceptance() {
        return Boolean.TRUE.equals(acceptPrivacyPolicy);
    }

}
