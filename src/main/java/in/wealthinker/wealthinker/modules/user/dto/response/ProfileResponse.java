package in.wealthinker.wealthinker.modules.user.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    private LocalDate dateOfBirth;

    private Integer age; // Computed field
    private UserProfile.Gender gender;
    private String nationality;

    private String occupation;
    private String company;
    private String industry;
    private Integer workExperienceYears;

    private String bio;
    private String profileImageUrl;

    // Financial Information (restricted access)
    private Long annualIncome;
    private UserProfile.IncomeSource incomeSource;

    // Investment Profile (public information)
    private UserProfile.InvestmentExperience investmentExperience;
    private UserProfile.RiskTolerance riskTolerance;

    // KYC Information (very restricted access)
    private UserProfile.KycStatus kycStatus; // Only for compliance/owner

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime kycSubmittedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime kycApprovedAt;

    // Profile Completion
    private Boolean profileCompleted;
    private Integer profileCompletionPercentage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Contact information
    private String alternativePhone;
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
        private Boolean isComplete;
    }

    /**
     * Factory method for public profile view
     * Removes sensitive financial and KYC information
     */
    public static ProfileResponse createPublicView(ProfileResponse full) {
        return ProfileResponse.builder()
                .id(full.getId())
                .firstName(full.getFirstName())
                .lastName(full.getLastName())
                .fullName(full.getFullName())
                .age(full.getAge())
                .gender(full.getGender())
                .nationality(full.getNationality())
                .occupation(full.getOccupation())
                .company(full.getCompany())
                .industry(full.getIndustry())
                .workExperienceYears(full.getWorkExperienceYears())
                .bio(full.getBio())
                .profileImageUrl(full.getProfileImageUrl())
                .investmentExperience(full.getInvestmentExperience())
                .riskTolerance(full.getRiskTolerance())
                .profileCompleted(full.getProfileCompleted())
                .profileCompletionPercentage(full.getProfileCompletionPercentage())
                .createdAt(full.getCreatedAt())
                .build();
    }
}