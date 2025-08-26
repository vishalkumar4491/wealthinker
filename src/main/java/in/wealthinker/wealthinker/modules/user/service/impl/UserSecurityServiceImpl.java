package in.wealthinker.wealthinker.modules.user.service.impl;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdatePasswordRequest;
import in.wealthinker.wealthinker.modules.user.dto.request.UpdateUserEmailRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.UserSessionResponse;
import in.wealthinker.wealthinker.modules.user.entity.User;
import in.wealthinker.wealthinker.modules.user.entity.UserSession;
import in.wealthinker.wealthinker.modules.user.repository.UserRepository;
import in.wealthinker.wealthinker.modules.user.repository.UserSessionRepository;
import in.wealthinker.wealthinker.modules.user.service.UserSecurityService;
import in.wealthinker.wealthinker.shared.exceptions.AuthenticationException;
import in.wealthinker.wealthinker.shared.exceptions.BusinessException;
import in.wealthinker.wealthinker.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User Security Service Implementation
 *
 * RESPONSIBILITIES:
 * - Password management and changes
 * - Email address changes with verification
 * - Two-factor authentication setup and management
 * - Session management and tracking
 * - Security-related user operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserSecurityServiceImpl implements UserSecurityService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;

    // TODO: Inject when implemented
    // private final EmailService emailService;
    // private final TotpService totpService;

    // =================== PASSWORD MANAGEMENT ===================

    @Override
    @Transactional
    public void changePassword(Long userId, UpdatePasswordRequest request) {
        log.info("Password change request for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            log.warn("Invalid current password provided for user: {}", userId);
            throw new AuthenticationException("Current password is incorrect");
        }

        // Check if new password is different
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException("New password must be different from current password",
                    "SAME_PASSWORD");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all existing sessions except current one
        revokeAllSessionsExceptCurrent(userId);

        // TODO: Send password change notification
        // emailService.sendPasswordChangeNotification(user.getEmail());

        log.info("Password changed successfully for user: {}", userId);
    }

    @Override
    @Transactional
    public void changeEmail(Long userId, UpdateUserEmailRequest request) {
        log.info("Email change request for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid password provided for email change, user: {}", userId);
            throw new AuthenticationException("Password is incorrect");
        }

        // Check if email is already in use
        if (userRepository.existsByEmail(request.getNewEmail())) {
            throw new BusinessException("Email address is already in use", "EMAIL_ALREADY_EXISTS");
        }

        String oldEmail = user.getEmail();

        // Update email and reset verification status
        user.setEmail(request.getNewEmail());
        user.setEmailVerified(false);
        userRepository.save(user);

        // TODO: Send verification email to new address
        // TODO: Send notification to old address
        // emailService.sendEmailChangeVerification(request.getNewEmail(), user);
        // emailService.sendEmailChangeNotification(oldEmail, request.getNewEmail());

        log.info("Email changed for user: {} from {} to {}", userId, oldEmail, request.getNewEmail());
    }

    // =================== TWO-FACTOR AUTHENTICATION ===================

    @Override
    @Transactional
    public Map<String, String> enableTwoFactor(Long userId) {
        log.info("Enabling 2FA for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getPreference() != null && user.getPreference().getTwoFactorEnabled()) {
            throw new BusinessException("Two-factor authentication is already enabled", "2FA_ALREADY_ENABLED");
        }

        // TODO: Generate TOTP secret and QR code
        // String secret = totpService.generateSecret();
        // String qrCodeUrl = totpService.generateQrCodeUrl(user.getEmail(), secret);

        // For now, simulate the process
        String secret = "JBSWY3DPEHPK3PXP"; // Base32 encoded secret
        String qrCodeUrl = "https://chart.googleapis.com/chart?chs=200x200&chld=M|0&cht=qr&chl=" +
                "otpauth://totp/Wealthinker:" + user.getEmail() + "?secret=" + secret + "&issuer=Wealthinker";

        // Store secret temporarily (in real implementation, store encrypted)
        // user.setTotpSecret(encryptionService.encrypt(secret));

        Map<String, String> result = Map.of(
                "secret", secret,
                "qrCodeUrl", qrCodeUrl,
                "backupCodes", generateBackupCodes()
        );

        log.info("2FA setup initiated for user: {}", userId);
        return result;
    }

    @Override
    @Transactional
    public void verifyTwoFactor(Long userId, String code) {
        log.info("2FA verification for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // TODO: Verify TOTP code
        // if (!totpService.verifyCode(user.getTotpSecret(), code)) {
        //     throw new AuthenticationException("Invalid verification code");
        // }

        // For now, accept any 6-digit code
        if (code == null || !code.matches("\\d{6}")) {
            throw new AuthenticationException("Invalid verification code");
        }

        // Enable 2FA
        if (user.getPreference() == null) {
            // This shouldn't happen, but handle gracefully
            throw new BusinessException("User preferences not found", "PREFERENCES_NOT_FOUND");
        }

        user.getPreference().setTwoFactorEnabled(true);
        userRepository.save(user);

        log.info("2FA enabled successfully for user: {}", userId);
    }

    @Override
    @Transactional
    public void disableTwoFactor(Long userId, String password) {
        log.info("Disabling 2FA for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Verify password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Invalid password provided for 2FA disable, user: {}", userId);
            throw new AuthenticationException("Password is incorrect");
        }

        if (user.getPreference() == null || !user.getPreference().getTwoFactorEnabled()) {
            throw new BusinessException("Two-factor authentication is not enabled", "2FA_NOT_ENABLED");
        }

        // Disable 2FA
        user.getPreference().setTwoFactorEnabled(false);
        // user.setTotpSecret(null); // Clear secret
        userRepository.save(user);

        log.info("2FA disabled for user: {}", userId);
    }

    // =================== SESSION MANAGEMENT ===================

    @Override
    public List<UserSessionResponse> getActiveSessions(Long userId) {
        log.debug("Getting active sessions for user: {}", userId);

        List<UserSession> sessions = userSessionRepository.findActiveSessionsByUserId(userId);

        return sessions.stream()
                .map(this::toSessionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void revokeSession(Long userId, String sessionId) {
        log.info("Revoking session {} for user: {}", sessionId, userId);

        UserSession session = userSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserSession", "sessionId", sessionId));

        session.setActive(false);
        session.setRevokedAt(LocalDateTime.now());
        userSessionRepository.save(session);

        log.info("Session revoked successfully: {}", sessionId);
    }

    @Override
    @Transactional
    public void revokeAllSessions(Long userId) {
        log.info("Revoking all sessions for user: {}", userId);

        List<UserSession> activeSessions = userSessionRepository.findActiveSessionsByUserId(userId);

        for (UserSession session : activeSessions) {
            session.setActive(false);
            session.setRevokedAt(LocalDateTime.now());
        }

        userSessionRepository.saveAll(activeSessions);

        log.info("All sessions revoked for user: {} (count: {})", userId, activeSessions.size());
    }

    // =================== PRIVATE HELPER METHODS ===================

    private void revokeAllSessionsExceptCurrent(Long userId) {
        // TODO: Get current session ID from security context
        // String currentSessionId = SecurityContextHolder.getContext().getAuthentication().getDetails();

        List<UserSession> activeSessions = userSessionRepository.findActiveSessionsByUserId(userId);

        for (UserSession session : activeSessions) {
            // Skip current session
            // if (!session.getSessionId().equals(currentSessionId)) {
            session.setActive(false);
            session.setRevokedAt(LocalDateTime.now());
            // }
        }

        userSessionRepository.saveAll(activeSessions);
    }

    private String generateBackupCodes() {
        // Generate 10 backup codes
        StringBuilder codes = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            if (i > 0) codes.append(",");
            codes.append(UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        }
        return codes.toString();
    }

    private UserSessionResponse toSessionResponse(UserSession session) {
        return UserSessionResponse.builder()
                .sessionId(session.getSessionId())
                .deviceInfo(session.getDeviceInfo())
                .ipAddress(session.getIpAddress())
                .location(session.getLocation())
                .userAgent(session.getUserAgent())
                .createdAt(session.getCreatedAt())
                .lastAccessedAt(session.getLastAccessedAt())
                .active(session.getActive())
                .current(false) // TODO: Compare with current session
                .build();
    }
}
