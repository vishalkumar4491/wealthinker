package in.wealthinker.wealthinker.modules.user.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.wealthinker.wealthinker.modules.user.entity.UserProfile;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);
    
    @Query("SELECT up FROM UserProfile up WHERE up.user.email = :email")
    Optional<UserProfile> findByUserEmail(@Param("email") String email);

    @Query("SELECT up FROM UserProfile up WHERE up.user.username = :username")
    Optional<UserProfile> findByUserUsername(@Param("username") String username);
    
    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.profileCompleted = true")
    long countCompletedProfiles();

    @Query("SELECT up FROM UserProfile up WHERE up.profileCompletionPercentage BETWEEN :minPercentage AND :maxPercentage")
    Page<UserProfile> findByCompletionPercentageRange(@Param("minPercentage") Integer minPercentage,
                                                      @Param("maxPercentage") Integer maxPercentage,
                                                      Pageable pageable);

    Page<UserProfile> findByKycStatus(UserProfile.KycStatus kycStatus, Pageable pageable);

    @Query("SELECT up FROM UserProfile up WHERE up.dateOfBirth BETWEEN :startDate AND :endDate")
    Page<UserProfile> findByAgeRange(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     Pageable pageable);

    Page<UserProfile> findByInvestmentExperience(UserProfile.InvestmentExperience experience, Pageable pageable);

    Page<UserProfile> findByRiskTolerance(UserProfile.RiskTolerance riskTolerance, Pageable pageable);

    @Query("SELECT up FROM UserProfile up WHERE up.kycStatus = 'APPROVED' AND up.annualIncome > 0 AND up.investmentExperience IS NOT NULL")
    Page<UserProfile> findInvestmentEligibleProfiles(Pageable pageable);

    // =================== STATISTICS QUERIES ===================

    @Query("SELECT AVG(up.profileCompletionPercentage) FROM UserProfile up")
    Double getAverageCompletionPercentage();

    @Query("SELECT " + "CASE " + "  WHEN up.profileCompletionPercentage < 25 THEN '0-25%' " + "  WHEN up.profileCompletionPercentage < 50 THEN '25-50%' " +
            "  WHEN up.profileCompletionPercentage < 75 THEN '50-75%' " + "  ELSE '75-100%' " + "END as range, " + "COUNT(up) as count " + "FROM UserProfile up " + "GROUP BY " +
            "CASE " + "  WHEN up.profileCompletionPercentage < 25 THEN '0-25%' " + "  WHEN up.profileCompletionPercentage < 50 THEN '25-50%' " + "  WHEN up.profileCompletionPercentage < 75 THEN '50-75%' " +
            "  ELSE '75-100%' " + "END")
    List<Object[]> getCompletionDistribution();

    @Query("SELECT up.kycStatus, COUNT(up) FROM UserProfile up GROUP BY up.kycStatus")
    List<Object[]> getKycStatusDistribution();
}
