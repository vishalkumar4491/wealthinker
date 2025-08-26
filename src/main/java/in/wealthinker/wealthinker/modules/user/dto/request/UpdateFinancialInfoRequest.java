package in.wealthinker.wealthinker.modules.user.dto.request;

import in.wealthinker.wealthinker.modules.user.entity.UserProfile;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Update Financial Information Request DTO
 *
 * SECURITY CONSIDERATIONS:
 * - Separate DTO for sensitive financial data
 * - Should require additional authentication (2FA, password confirmation)
 * - All changes logged for audit trail
 * - Input validation to prevent unrealistic values
 *
 * COMPLIANCE:
 * - Financial data changes must be tracked for regulatory purposes
 * - May require manual review for large changes
 * - KYC re-verification may be triggered
 */
@Data
public class UpdateFinancialInfoRequest {

    @Min(value = 0, message = "Annual income cannot be negative")
    private Long annualIncome;

    private UserProfile.IncomeSource incomeSource;

    private UserProfile.InvestmentExperience investmentExperience;

    private UserProfile.RiskTolerance riskTolerance;

    // Validation for income reasonableness
    @AssertTrue(message = "Income source must be specified when income is provided")
    public boolean isIncomeSourceProvidedWhenIncomeExists() {
        return annualIncome == null || annualIncome == 0 || incomeSource != null;
    }

    // Business validation for investment experience vs income
    @AssertTrue(message = "High investment experience requires minimum income verification")
    public boolean isInvestmentExperienceConsistent() {
        if (investmentExperience == UserProfile.InvestmentExperience.EXPERT &&
                (annualIncome == null || annualIncome < 50000)) {
            return false; // Expert investors should have reasonable income
        }
        return true;
    }
}
