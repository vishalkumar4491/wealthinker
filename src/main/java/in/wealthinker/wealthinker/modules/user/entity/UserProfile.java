package in.wealthinker.wealthinker.modules.user.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


/**
 * User Profile Entity - Personal Information & KYC Data
 *
 * DESIGN PRINCIPLES:
 * 1. Separation of Concerns - Only profile data, no authentication
 * 2. Data Classification - Sensitive fields marked for encryption
 * 3. Compliance Ready - Fields align with KYC/AML requirements
 * 4. Performance Optimized - Indexed fields for common queries
 * 5. Future Proof - Extensible without breaking existing functionality
 *
 * SECURITY CONSIDERATIONS:
 * - Sensitive data (income, SSN) should be encrypted at application level
 * - PII fields need special handling for GDPR compliance
 * - Audit trail required for all changes (regulatory requirement)
 */

@Entity
@Table(name = "user_profiles",
        indexes = {
        @Index(name = "idx_profile_user_id", columnList = "user_id"),
        @Index(name = "idx_profile_completion", columnList = "profile_completed"),
        @Index(name = "idx_profile_kyc_status", columnList = "kyc_status"),
        @Index(name = "idx_profile_created_at", columnList = "created_at")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Relationship to User (One-to-One)
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_profile_user"))
    private User user;

    // Basic Personal Information (PUBLIC data classification)
    @Size(min = 2, max = 20, message = "First name must be between 2 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'.-]+$", message = "First name can only contain letters, spaces, apostrophes, dots, and hyphens")
    @Column(name = "first_name", length = 20)
    //@NotNull(message = "First name cannot be null")
    private String firstName;

    @Size(min = 2, max = 20, message = "First name must be between 2 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'.-]+$", message = "Last name can only contain letters, spaces, apostrophes, dots, and hyphens")
    @Column(name = "last_name", length = 20)
    private String lastName;

    // @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Invalid phone number format")
//    @Pattern(
//    regexp = "^(?:[6-9]\\d{9}|\\+?[1-9]\\d{1,14})$",
//    message = "Phone number must be valid Indian or international format"
//    )
//    @Column(name = "phone_number", unique = true)
//    @Size(min = 10, max = 15)
//    @NotNull(message = "Phone number cannot be null")
//    private String phoneNumber;

    // Demographics (INTERNAL data classification)
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Size(max = 10, message = "Nationality code must not exceed 10 characters")
    @Pattern(regexp = "^[A-Z]{2,3}$", message = "Nationality must be a valid country code")
    @Column(name = "nationality", length = 10)
    private String nationality; // ISO country code (e.g., "US", "IN", "GB")

    // Professional Information (INTERNAL data classification)
    @Size(max = 50, message = "Occupation must not exceed 50 characters")
    @Column(name = "occupation", length = 50)
    private String occupation;

    @Size(max = 50, message = "Company name must not exceed 50 characters")
    @Column(name = "company", length = 50)
    private String company;

    @Size(max = 50, message = "Industry must not exceed 50 characters")
    @Column(name = "industry", length = 50)
    private String industry;

    @Min(value = 0, message = "Work experience cannot be negative")
    @Max(value = 80, message = "Work experience cannot exceed 80 years")
    @Column(name = "work_experience_years")
    private Integer workExperienceYears;

    // Contact Information (INTERNAL data classification)
    @Embedded
    private Address address;

    // Financial Information (CONFIDENTIAL data classification)
    @Min(value = 0, message = "Annual income cannot be negative")
    @Column(name = "annual_income")
    private Long annualIncome;      // Store in base currency rupees to avoid floating point issues

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_experience", length = 20)
    @Builder.Default
    private InvestmentExperience investmentExperience = InvestmentExperience.BEGINNER;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_source", length = 30)
    @Builder.Default
    private IncomeSource incomeSource = IncomeSource.EMPLOYMENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_tolerance", length = 20)
    @Builder.Default
    private RiskTolerance riskTolerance = RiskTolerance.MODERATE;

    // // Personal Details
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    @Column(name = "bio", length = 500)
    private String bio;

    // KYC (Know Your Customer) Status
    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", length = 20)
    @Builder.Default
    private KycStatus kycStatus = KycStatus.NOT_STARTED;

    @Column(name = "kyc_submitted_at")
    private LocalDateTime kycSubmittedAt;

    @Column(name = "kyc_approved_at")
    private LocalDateTime kycApprovedAt;

    @Size(max = 500, message = "KYC rejection reason must not exceed 500 characters")
    @Column(name = "kyc_rejection_reason", length = 500)
    private String kycRejectionReason;

    // Profile Completion Status
    @Column(name = "profile_completed", nullable = false)
    @Builder.Default
    private Boolean profileCompleted = false;

    @Min(value = 0, message = "Profile completion percentage cannot be negative")
    @Max(value = 100, message = "Profile completion percentage cannot exceed 100")
    @Column(name = "profile_completion_percentage", nullable = false)
    @Builder.Default
    private Integer profileCompletionPercentage = 0;

    // Audit Fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;


    // Enums
    public enum Gender {
        MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    }

    public enum IncomeSource {
        EMPLOYMENT, BUSINESS, INVESTMENTS, PENSION, OTHER
    }

    public enum InvestmentExperience {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    public enum RiskTolerance {
        CONSERVATIVE, MODERATE, AGGRESSIVE, VERY_AGGRESSIVE
    }

    public enum KycStatus {
        NOT_STARTED, IN_PROGRESS, SUBMITTED, APPROVED, REJECTED, EXPIRED
    }


    // Embedded class for Address
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {

        @Size(max = 200, message = "Address line 1 must not exceed 200 characters")
        @Column(name = "address_line1", length = 200)
        private String addressLine1;

        @Size(max = 200, message = "Address line 2 must not exceed 200 characters")
        @Column(name = "address_line2", length = 200)
        private String addressLine2;

        @Size(max = 100, message = "City must not exceed 100 characters")
        @Column(name = "city", length = 100)
        private String city;

        @Size(max = 100, message = "State must not exceed 100 characters")
        @Column(name = "state", length = 100)
        private String state;

        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        @Column(name = "postal_code", length = 20)
        private String postalCode;

        @Size(max = 3, message = "Country code must not exceed 3 characters")
        @Pattern(regexp = "^[A-Z]{2,3}$", message = "Country must be a valid ISO country code")
        @Column(name = "country", length = 3)
        private String country; // ISO country code

        public boolean isComplete() {
            return addressLine1 != null && city != null &&
                    state != null && postalCode != null && country != null;
        }
    }

    /**
     * Calculate user's age from date of birth
     */
    public Integer getAge() {
        if (dateOfBirth == null) {
            return null;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    // Helper methods
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null) {
            fullName.append(firstName);
        }
        if (lastName != null) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName);
        }
        return fullName.toString();
    }

    /**
     * Calculate profile completion percentage based on filled mandatory fields
     *
     * BUSINESS LOGIC:
     * - Higher completion percentage = better user engagement
     * - Used for progressive profiling and feature unlocking
     * - KYC completion is weighted heavily for financial compliance
     */
    public void calculateAndSetCompletionPercentage() {
        int totalFields = 15; // Total mandatory fields
        int filledFields = 0;

        // Basic information (weight: 1 each)
        if (firstName != null && !firstName.trim().isEmpty()) filledFields++;
        if (lastName != null && !lastName.trim().isEmpty()) filledFields++;
        if (dateOfBirth != null) filledFields++;
        if (gender != null) filledFields++;

        // Contact information (weight: 1 each)
        if (address != null && address.isComplete()) filledFields += 2;

        // Professional information (weight: 1 each)
        if (occupation != null && !occupation.trim().isEmpty()) filledFields++;
        if (company != null && !company.trim().isEmpty()) filledFields++;
        if (industry != null && !industry.trim().isEmpty()) filledFields++;

        // Financial information (weight: 2 each - more important for fintech)
        if (annualIncome != null && annualIncome > 0) filledFields += 2;
        if (investmentExperience != null) filledFields++;
        if (riskTolerance != null) filledFields++;

        // KYC status (weight: 3 - most important for compliance)
        if (kycStatus == KycStatus.APPROVED) filledFields += 3;

        this.profileCompletionPercentage = Math.min(100, (filledFields * 100) / totalFields);
        this.profileCompleted = this.profileCompletionPercentage >= 80;
    }

    /**
     * Check if user meets suitability requirements for investment products
     *
     * REGULATORY REQUIREMENT:
     * - Securities regulations require suitability assessment
     * - Must verify customer has appropriate income/experience for products
     * - Used to prevent unsuitable investment recommendations
     */
    public boolean isSuitableForInvestments() {
        return kycStatus == KycStatus.APPROVED &&
                annualIncome != null &&
                annualIncome > 0 &&
                investmentExperience != null &&
                riskTolerance != null;
    }

    /**
     * Check if user is eligible for premium features
     */
    public boolean isEligibleForPremium() {
        return profileCompleted &&
                kycStatus == KycStatus.APPROVED &&
                profileCompletionPercentage >= 90;
    }

    // JPA Lifecycle Callbacks
    @PrePersist
    @PreUpdate
    protected void updateCompletionPercentage() {
        calculateAndSetCompletionPercentage();
    }

}
