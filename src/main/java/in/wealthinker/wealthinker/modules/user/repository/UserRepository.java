package in.wealthinker.wealthinker.modules.user.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.wealthinker.wealthinker.modules.user.entity.User;
import in.wealthinker.wealthinker.shared.enums.UserRole;
import in.wealthinker.wealthinker.shared.enums.UserStatus;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByPhoneNumber(String phoneNumber);

    //  Find by any identifier (for admin purposes)
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.username = :identifier OR u.phoneNumber = :identifier")
    Optional<User> findByEmailOrUsernameOrPhone(@Param("identifier") String identifier);

    Optional<User> findByEmailAndIsActiveTrue(String email);

    Optional<User> findByUsernameAndIsActiveTrue(String username);

    Optional<User> findByPhoneNumberAndIsActiveTrue(String phoneNumber);
    
    // Find by email, username, OR phone number
    @Query("SELECT u FROM User u WHERE (u.email = :identifier OR u.username = :identifier OR u.phoneNumber = :identifier) AND u.isActive = true")
    Optional<User> findByEmailOrUsernameOrPhoneAndIsActiveTrue(@Param("identifier") String identifier);

    // Find by social provider and provider ID
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phoneNumber);

    // Check if email, username, or phone exists
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email OR u.username = :username OR u.phoneNumber = :phoneNumber")
    boolean existsByEmailOrUsernameOrPhone(@Param("email") String email, @Param("username") String username, @Param("phoneNumber") String phoneNumber);

    // Check if any identifier exists
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :identifier OR u.username = :identifier OR u.phoneNumber = :identifier")
    boolean existsByAnyIdentifier(@Param("identifier") String identifier);

    Page<User> findByStatus(UserStatus status, Pageable pageable);
    
    Page<User> findByRole(UserRole role, Pageable pageable);

    // Find by email verification status
    @Query("SELECT u FROM User u WHERE u.emailVerified = :emailVerified")
    Page<User> findByEmailVerified(@Param("emailVerified") Boolean emailVerified, Pageable pageable);

    // Find by phone verification status
    // @Query("SELECT u FROM User u WHERE u.phoneVerified = :phoneVerified")
    // Page<User> findByPhoneVerified(@Param("phoneVerified") Boolean phoneVerified, Pageable pageable);

    // Find users registered after a specific date
    @Query("SELECT u FROM User u WHERE u.createdAt >= :fromDate")
    Page<User> findUsersRegisteredAfter(@Param("fromDate") LocalDateTime fromDate, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NULL OR u.lastLoginAt < :beforeDate")
    Page<User> findInactiveUsers(@Param("beforeDate") LocalDateTime beforeDate, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") UserStatus status);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);
    
    // Search users by name, email, or phone
    @Query("SELECT u FROM User u WHERE u.profile.firstName LIKE %:name% OR u.profile.lastName LIKE %:name% OR u.email LIKE %:name% OR u.username LIKE %:name% OR u.phoneNumber LIKE %:name%")
    Page<User> searchUsers(@Param("name") String name, Pageable pageable);
}
