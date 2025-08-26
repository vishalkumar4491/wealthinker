package in.wealthinker.wealthinker.modules.user.controller;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdatePasswordRequest;
import in.wealthinker.wealthinker.modules.user.dto.request.UpdateUserEmailRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.UserSessionResponse;
import in.wealthinker.wealthinker.modules.user.service.UserSecurityService;
import in.wealthinker.wealthinker.shared.constants.AppConstants;
import in.wealthinker.wealthinker.shared.response.ApiResponseCustom;
import in.wealthinker.wealthinker.shared.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * User Security Controller - Security-related operations
 *
 * PURPOSE:
 * - Handle sensitive security operations
 * - Password changes, email updates
 * - Two-factor authentication setup
 * - Account security settings
 *
 * SECURITY:
 * - All endpoints require authentication
 * - Additional validation for sensitive operations
 * - Audit logging for all security changes
 * - Rate limiting for password attempts
 */

@RestController
@RequestMapping(AppConstants.USER_ENDPOINT + "/security")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Security", description = "User security and authentication operations")
@SecurityRequirement(name = "bearerAuth")
public class UserSecurityController {

    private final UserSecurityService userSecurityService;

    @PutMapping("/password")
    @Operation(
            summary = "Change password",
            description = "Change the current user's password. Requires current password verification."
    )
    public ResponseEntity<ApiResponseCustom<Void>> changePassword(
            @Valid @RequestBody UpdatePasswordRequest request) {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new SecurityException("No authenticated user found"));

        log.info("Password change request for user: {}", currentUserId);

        userSecurityService.changePassword(currentUserId, request);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "Password changed successfully"));
    }

    @PutMapping("/email")
    @Operation(
            summary = "Change email address",
            description = "Change the current user's email address. Requires password verification and email re-verification."
    )
    public ResponseEntity<ApiResponseCustom<Void>> changeEmail(
            @Valid @RequestBody UpdateUserEmailRequest request) {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new SecurityException("No authenticated user found"));

        log.info("Email change request for user: {}", currentUserId);

        userSecurityService.changeEmail(currentUserId, request);

        return ResponseEntity.ok(ApiResponseCustom.success(null,
                "Email change initiated. Please verify your new email address."));
    }

    @PostMapping("/2fa/enable")
    @Operation(
            summary = "Enable two-factor authentication",
            description = "Enable 2FA for the current user account"
    )
    public ResponseEntity<ApiResponseCustom<Map<String, String>>> enableTwoFactor() {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new SecurityException("No authenticated user found"));

        log.info("2FA enable request for user: {}", currentUserId);

        Map<String, String> qrData = userSecurityService.enableTwoFactor(currentUserId);

        return ResponseEntity.ok(ApiResponseCustom.success(qrData,
                "2FA setup initiated. Please scan the QR code with your authenticator app."));
    }

    @PostMapping("/2fa/verify")
    @Operation(
            summary = "Verify two-factor authentication setup",
            description = "Verify 2FA setup with a code from the authenticator app"
    )
    public ResponseEntity<ApiResponseCustom<Void>> verifyTwoFactor(
            @Parameter(description = "6-digit verification code") @RequestParam String code) {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new SecurityException("No authenticated user found"));

        log.info("2FA verification for user: {}", currentUserId);

        userSecurityService.verifyTwoFactor(currentUserId, code);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "Two-factor authentication enabled successfully"));
    }

    @DeleteMapping("/2fa/disable")
    @Operation(
            summary = "Disable two-factor authentication",
            description = "Disable 2FA for the current user account"
    )
    public ResponseEntity<ApiResponseCustom<Void>> disableTwoFactor(
            @Parameter(description = "Current password for verification") @RequestParam String password) {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new SecurityException("No authenticated user found"));

        log.info("2FA disable request for user: {}", currentUserId);

        userSecurityService.disableTwoFactor(currentUserId, password);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "Two-factor authentication disabled"));
    }

    @GetMapping("/sessions")
    @Operation(
            summary = "Get active sessions",
            description = "Retrieve list of active sessions for the current user"
    )
    public ResponseEntity<ApiResponseCustom<List<UserSessionResponse>>> getActiveSessions() {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new SecurityException("No authenticated user found"));

        log.debug("Getting active sessions for user: {}", currentUserId);

        List<UserSessionResponse> sessions = userSecurityService.getActiveSessions(currentUserId);

        return ResponseEntity.ok(ApiResponseCustom.success(sessions));
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(
            summary = "Revoke session",
            description = "Revoke a specific active session"
    )
    public ResponseEntity<ApiResponseCustom<Void>> revokeSession(
            @Parameter(description = "Session ID to revoke") @PathVariable String sessionId) {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new SecurityException("No authenticated user found"));

        log.info("Session revocation request for user: {}, session: {}", currentUserId, sessionId);

        userSecurityService.revokeSession(currentUserId, sessionId);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "Session revoked successfully"));
    }

    @DeleteMapping("/sessions/all")
    @Operation(
            summary = "Revoke all sessions",
            description = "Revoke all active sessions except the current one"
    )
    public ResponseEntity<ApiResponseCustom<Void>> revokeAllSessions() {

        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new SecurityException("No authenticated user found"));

        log.info("All sessions revocation request for user: {}", currentUserId);

        userSecurityService.revokeAllSessions(currentUserId);

        return ResponseEntity.ok(ApiResponseCustom.success(null, "All sessions revoked successfully"));
    }
}
