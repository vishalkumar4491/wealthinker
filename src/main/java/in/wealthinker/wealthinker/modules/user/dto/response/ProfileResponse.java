package in.wealthinker.wealthinker.modules.user.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import in.wealthinker.wealthinker.modules.user.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private UserProfile.Gender gender;
    private String occupation;
    private String company;
    private String bio;
    private Long annualIncome;
    private UserProfile.InvestmentExperience investmentExperience;
    private UserProfile.RiskTolerance riskTolerance;
    private String profileImageUrl;
    private Boolean profileCompleted;
    private Integer profileCompletionPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Address information
    private AddressResponse address;

/**
 * Represents a response object containing address information.
 * This class is a static nested class and follows the Builder pattern.
 * It uses Lombok annotations to reduce boilerplate code.
 */
    @Data                    // Generates getters, setters, toString(), equals(), and hashCode() methods
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressResponse {
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String country;
        private String postalCode;
    }
}