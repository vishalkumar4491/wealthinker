package in.wealthinker.wealthinker.modules.user.repository;

import in.wealthinker.wealthinker.modules.user.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.active = true AND s.expiresAt > :now")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    default List<UserSession> findActiveSessionsByUserId(Long userId) {
        return findActiveSessionsByUserId(userId, LocalDateTime.now());
    }

    Optional<UserSession> findBySessionIdAndUserId(String sessionId, Long userId);

    Optional<UserSession> findBySessionId(String sessionId);

    @Modifying
    @Query("UPDATE UserSession s SET s.active = false, s.revokedAt = :revokedAt WHERE s.user.id = :userId AND s.active = true")
    int revokeAllSessionsForUser(@Param("userId") Long userId, @Param("revokedAt") LocalDateTime revokedAt);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :expiredBefore")
    int deleteExpiredSessions(@Param("expiredBefore") LocalDateTime expiredBefore);

    long countByUserIdAndActiveTrue(Long userId);
}
