package in.wealthinker.wealthinker.modules.user.service;

import in.wealthinker.wealthinker.modules.user.dto.request.UpdatePasswordRequest;
import in.wealthinker.wealthinker.modules.user.dto.request.UpdateUserEmailRequest;
import in.wealthinker.wealthinker.modules.user.dto.response.UserSessionResponse;

import java.util.List;
import java.util.Map;

public interface UserSecurityService {
    // Password management
    void changePassword(Long userId, UpdatePasswordRequest request);

    // Email management
    void changeEmail(Long userId, UpdateUserEmailRequest request);

    // Two-factor authentication
    Map<String, String> enableTwoFactor(Long userId);
    void verifyTwoFactor(Long userId, String code);
    void disableTwoFactor(Long userId, String password);

    // Session management
    List<UserSessionResponse> getActiveSessions(Long userId);
    void revokeSession(Long userId, String sessionId);
    void revokeAllSessions(Long userId);
}
