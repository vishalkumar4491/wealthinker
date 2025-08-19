package in.wealthinker.wealthinker.modules.user.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_profiles")
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

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 20)
    @Column(name = "first_name")
    @NotNull(message = "First name cannot be null")
    private String firstName;

    @Size(max = 20)
    @Column(name = "last_name")
    private String lastName;

    // @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Pattern(
    regexp = "^(?:[6-9]\\d{9}|\\+?[1-9]\\d{1,14})$",
    message = "Phone number must be valid Indian or international format"
    )
    @Column(name = "phone_number", unique = true)
    @Size(min = 10, max = 15)
    @NotNull(message = "Phone number cannot be null")
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth")
    @NotNull(message = "Date of birth cannot be null")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Size(max = 50)
    @Column(name = "occupation")
    private String occupation;

    @Size(max = 50)
    @Column(name = "company")
    private String company;

    @Embedded
    private Address address;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Size(max = 500)
    @Column(name = "bio")
    private String bio;

    @Column(name = "annual_income")
    private Long annualIncome;

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_experience")
    @Builder.Default
    private InvestmentExperience investmentExperience = InvestmentExperience.BEGINNER;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_tolerance")
    @Builder.Default
    private RiskTolerance riskTolerance = RiskTolerance.MODERATE;

    @Column(name = "profile_completed", nullable = false)
    @Builder.Default
    private Boolean profileCompleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // Enums
    public enum Gender {
        MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    }

    public enum InvestmentExperience {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    public enum RiskTolerance {
        CONSERVATIVE, MODERATE, AGGRESSIVE, VERY_AGGRESSIVE
    }


    // Embedded class for Address
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        @Column(name = "address_line1")
        private String addressLine1;

        @Column(name = "address_line2")
        private String addressLine2;

        @Column(name = "city")
        private String city;

        @Column(name = "state")
        private String state;

        @Column(name = "country")
        private String country;

        @Column(name = "postal_code")
        private String postalCode;
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

    public int calculateProfileCompletionPercentage() {
        int totalFields = 10;
        int filledFields = 0;

        if (firstName != null && !firstName.trim().isEmpty()) filledFields++;
        if (lastName != null && !lastName.trim().isEmpty()) filledFields++;
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) filledFields++;
        if (dateOfBirth != null) filledFields++;
        if (gender != null) filledFields++;
        if (occupation != null && !occupation.trim().isEmpty()) filledFields++;
        if (company != null && !company.trim().isEmpty()) filledFields++;
        if (address != null && address.getCity() != null) filledFields++;
        if (annualIncome != null) filledFields++;
        if (bio != null && !bio.trim().isEmpty()) filledFields++;

        return (filledFields * 100) / totalFields;
    }
}
