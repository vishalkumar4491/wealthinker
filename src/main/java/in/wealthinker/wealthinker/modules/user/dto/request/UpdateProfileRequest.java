package in.wealthinker.wealthinker.modules.user.dto.request;

import java.time.LocalDate;

import in.wealthinker.wealthinker.modules.user.entity.UserProfile;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(min = 2, max = 20, message = "First name should be between 2 and 20 characters")
    private String firstName;

    @Size(min = 2, max = 20, message = "Last name should be between 2 and 20 characters")
    private String lastName;

    @Pattern(regexp = "^(?:[6-9]\\d{9}|\\+?[1-9]\\d{1,14})$", message = "Invalid phone number format")
    @Size(min = 10, max = 15)
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private UserProfile.Gender gender;

    @Size(max = 50, message = "Occupation should not exceed 50 characters")
    private String occupation;

    @Size(max = 50, message = "Company should not exceed 50 characters")
    private String company;

    @Size(max = 500, message = "Bio should not exceed 500 characters")
    private String bio;

    private Long annualIncome;

    private UserProfile.InvestmentExperience investmentExperience;

    private UserProfile.RiskTolerance riskTolerance;

    // Address fields
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
