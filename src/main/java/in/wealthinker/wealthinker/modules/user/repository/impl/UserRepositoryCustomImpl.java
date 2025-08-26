package in.wealthinker.wealthinker.modules.user.repository.impl;

import in.wealthinker.wealthinker.modules.user.entity.User;
import in.wealthinker.wealthinker.modules.user.repository.UserRepositoryCustom;
import in.wealthinker.wealthinker.shared.enums.UserRole;
import in.wealthinker.wealthinker.shared.enums.UserStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom Repository Implementation using JPA Criteria API
 *
 * WHY CRITERIA API:
 * - Type-safe query building (compile-time error checking)
 * - Dynamic query construction based on runtime conditions
 * - Better performance than string concatenation
 * - Protection against SQL injection
 * - IDE auto-completion and refactoring support
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryCustomImpl.class);

    private final EntityManager entityManager;

    @Override
    public Page<User> findUsersWithFilters(String email, String username, String phoneNumber,
                                           String firstName, String lastName, String role,
                                           String status, LocalDateTime registeredAfter,
                                           LocalDateTime registeredBefore, Boolean emailVerified,
                                           Boolean phoneVerified, Pageable pageable) {

        log.debug("Finding users with filters - email: {}, username: {}, role: {}, status: {}",
                email, username, role, status);


        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> user = query.from(User.class);

        // Join with profile for name search
        Join<Object, Object> profile = user.join("profile", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        // Email filter
        if (email != null && !email.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(user.get("email")),
                    "%" + email.toLowerCase() + "%"));
        }

        // Username filter
        if (username != null && !username.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(user.get("username")),
                    "%" + username.toLowerCase() + "%"));
        }

        // Phone filter
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            predicates.add(cb.like(user.get("phoneNumber"),
                    "%" + phoneNumber + "%"));
        }

        // First name filter
        if (firstName != null && !firstName.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(profile.get("firstName")),
                    "%" + firstName.toLowerCase() + "%"));
        }

        // Last name filter
        if (lastName != null && !lastName.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(profile.get("lastName")),
                    "%" + lastName.toLowerCase() + "%"));
        }

        // Role filter
        if (role != null && !role.trim().isEmpty()) {
            try {
                UserRole userRole = UserRole.valueOf(role.toUpperCase());
                predicates.add(cb.equal(user.get("role"), userRole));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role filter value: {}", role);
            }
        }

        // Status filter
        if (status != null && !status.trim().isEmpty()) {
            try {
                UserStatus userStatus = UserStatus.valueOf(status.toUpperCase());
                predicates.add(cb.equal(user.get("status"), userStatus));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter value: {}", status);
            }
        }

        // Date range filters
        if (registeredAfter != null) {
            predicates.add(cb.greaterThanOrEqualTo(user.get("createdAt"), registeredAfter));
        }

        if (registeredBefore != null) {
            predicates.add(cb.lessThanOrEqualTo(user.get("createdAt"), registeredBefore));
        }

        // Verification filters
        if (emailVerified != null) {
            predicates.add(cb.equal(user.get("emailVerified"), emailVerified));
        }

        if (phoneVerified != null) {
            predicates.add(cb.equal(user.get("phoneVerified"), phoneVerified));
        }

        // Combine all predicates
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        // Default ordering by creation date (newest first)
        query.orderBy(cb.desc(user.get("createdAt")));

        // Execute count query for pagination
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);
        countRoot.join("profile", JoinType.LEFT); // Same join for accurate count
        countQuery.select(cb.count(countRoot));
        countQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        // Execute main query with pagination
        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<User> users = typedQuery.getResultList();

        log.debug("Found {} users matching filters (total: {})", users.size(), totalElements);

        return new PageImpl<>(users, pageable, totalElements);
    }

    @Override
    public Map<String, Long> getUserActivityStats() {
        log.debug("Calculating user activity statistics");

        Map<String, Long> stats = new HashMap<>();

        // Total users
        Long totalUsers = entityManager.createQuery(
                        "SELECT COUNT(u) FROM User u", Long.class)
                .getSingleResult();
        stats.put("totalUsers", totalUsers);

        // Active users
        Long activeUsers = entityManager.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.isActive = true", Long.class)
                .getSingleResult();
        stats.put("activeUsers", activeUsers);

        // Verified users
        Long verifiedUsers = entityManager.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.emailVerified = true", Long.class)
                .getSingleResult();
        stats.put("verifiedUsers", verifiedUsers);

        // Users registered this month
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        Long newUsersThisMonth = entityManager.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate", Long.class)
                .setParameter("startDate", startOfMonth)
                .getSingleResult();
        stats.put("newUsersThisMonth", newUsersThisMonth);

        // Users with complete profiles
        Long completeProfiles = entityManager.createQuery(
                        "SELECT COUNT(u) FROM User u JOIN u.profile p WHERE p.profileCompleted = true", Long.class)
                .getSingleResult();
        stats.put("completeProfiles", completeProfiles);

        log.debug("User activity stats calculated: {}", stats);
        return stats;
    }

    @Override
    public List<User> findUsersEligibleForFeature(String featureName) {
        log.debug("Finding users eligible for feature: {}", featureName);

        // Example: Premium features require verified email and completed profile
        if ("premium_features".equals(featureName)) {
            return entityManager.createQuery(
                            "SELECT u FROM User u JOIN u.profile p WHERE " +
                                    "u.emailVerified = true AND " +
                                    "p.profileCompleted = true AND " +
                                    "u.role IN ('PREMIUM', 'PRO') AND " +
                                    "u.isActive = true", User.class)
                    .getResultList();
        }

        // Example: Investment features require KYC approval
        if ("investment_features".equals(featureName)) {
            return entityManager.createQuery(
                            "SELECT u FROM User u JOIN u.profile p WHERE " +
                                    "p.kycStatus = 'APPROVED' AND " +
                                    "u.isActive = true", User.class)
                    .getResultList();
        }

        // Default: return empty list for unknown features
        log.warn("Unknown feature name: {}", featureName);
        return new ArrayList<>();
    }

    @Override
    public Page<User> findUsersWithIncompleteProfiles(Integer maxCompletionPercentage, Pageable pageable) {
        log.debug("Finding users with incomplete profiles (max completion: {}%)", maxCompletionPercentage);

        // Count query
        Long totalElements = entityManager.createQuery(
                        "SELECT COUNT(u) FROM User u LEFT JOIN u.profile p WHERE " +
                                "(p IS NULL OR p.profileCompletionPercentage < :maxCompletion) AND " +
                                "u.isActive = true", Long.class)
                .setParameter("maxCompletion", maxCompletionPercentage)
                .getSingleResult();

        // Main query
        List<User> users = entityManager.createQuery(
                        "SELECT u FROM User u LEFT JOIN FETCH u.profile p WHERE " +
                                "(p IS NULL OR p.profileCompletionPercentage < :maxCompletion) AND " +
                                "u.isActive = true ORDER BY u.createdAt DESC", User.class)
                .setParameter("maxCompletion", maxCompletionPercentage)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        log.debug("Found {} users with incomplete profiles", users.size());
        return new PageImpl<>(users, pageable, totalElements);
    }
}
