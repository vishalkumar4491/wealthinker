package in.wealthinker.wealthinker.modules.user.dto.request;

import java.time.LocalDate;

import in.wealthinker.wealthinker.modules.user.entity.UserProfile;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(min = 2, max = 20, message = "First name should be between 2 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'.-]+$", message = "First name can only contain letters, spaces, apostrophes, dots, and hyphens")
    private String firstName;

    @Size(min = 2, max = 20, message = "Last name should be between 2 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'.-]+$", message = "Last name can only contain letters, spaces, apostrophes, dots, and hyphens")
    private String lastName;

    @Pattern(regexp = "^(?:[6-9]\\d{9}|\\+?[1-9]\\d{1,14})$", message = "Alternative phone number must be in Valid Indian or International format")
    @Size(min = 10, max = 15)
    private String alternativePhone;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private UserProfile.Gender gender;

    @Size(max = 50, message = "Occupation should not exceed 50 characters")
    private String occupation;

    @Size(max = 50, message = "Company should not exceed 50 characters")
    private String company;

    @Size(max = 50, message = "Industry must not exceed 50 characters")
    private String industry;

    @Min(value = 0, message = "Work experience cannot be negative")
    @Max(value = 80, message = "Work experience cannot exceed 80 years")
    private Integer workExperienceYears;

    @Size(max = 500, message = "Bio should not exceed 500 characters")
    private String bio;

    private UserProfile.InvestmentExperience investmentExperience;

    private UserProfile.RiskTolerance riskTolerance;

    // Address Information
    private AddressUpdateRequest address;


    /**
     * Embedded Address Update Request
     *
     * WHY EMBEDDED:
     * - Address is conceptually part of profile
     * - Reduces API complexity (one request vs multiple)
     * - Maintains data consistency
     */
    @Data
    public static class AddressUpdateRequest {

        @Size(max = 200, message = "Address line 1 must not exceed 200 characters")
        private String addressLine1;

        @Size(max = 200, message = "Address line 2 must not exceed 200 characters")
        private String addressLine2;

        @Size(max = 100, message = "City must not exceed 100 characters")
        private String city;

        @Size(max = 100, message = "State must not exceed 100 characters")
        private String state;

        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        private String postalCode;

        @Size(max = 3, message = "Country code must not exceed 3 characters")
        @Pattern(regexp = "^[A-Z]{2,3}$", message = "Country must be a valid ISO country code")
        private String country;
    }
}
