package in.wealthinker.wealthinker.modules.user.dto.request;

import java.time.LocalDate;

import in.wealthinker.wealthinker.modules.user.entity.UserProfile;
import in.wealthinker.wealthinker.shared.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name should be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name should be between 2 and 50 characters")
    private String lastName;

    @Pattern(
    regexp = "^(?:[6-9]\\d{9}|\\+?[1-9]\\d{1,14})$",
    message = "Phone number must be valid Indian or international format"
    )
    @Size(min = 10, max = 20)
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private UserProfile.Gender gender;

    @Size(max = 100, message = "Occupation should not exceed 100 characters")
    private String occupation;

    @Size(max = 100, message = "Company should not exceed 100 characters")
    private String company;

    private UserRole role = UserRole.FREE;
}
