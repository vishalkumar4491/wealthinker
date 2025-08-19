package in.wealthinker.wealthinker.modules.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.wealthinker.wealthinker.modules.user.entity.UserProfile;

public interface UserProfileRepository {
    Optional<UserProfile> findByUserId(Long userId);
    
    @Query("SELECT up FROM UserProfile up WHERE up.user.email = :email")
    Optional<UserProfile> findByUserEmail(@Param("email") String email);

    // @Query("SELECT up FROM UserProfile up WHERE up.user.username = :username")
    // Optional<UserProfile> findByUserUsername(@Param("username") String username);
    
    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.profileCompleted = true")
    long countCompletedProfiles();
}
