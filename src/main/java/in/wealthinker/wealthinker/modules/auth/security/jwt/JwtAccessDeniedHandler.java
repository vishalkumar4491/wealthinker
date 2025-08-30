package in.wealthinker.wealthinker.modules.auth.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.wealthinker.wealthinker.shared.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
/**
 * JWT Access Denied Handler - Handles authorization failures
 *
 * PURPOSE:
 * - Handle requests from authenticated users who lack required permissions
 * - Return structured JSON error responses for access denied scenarios
 * - Log authorization violations for security monitoring
 * - Differentiate between authentication and authorization failures
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String remoteAddr = getClientIpAddress(request);

        // Get current user info for logging
        String userEmail = SecurityUtils.getCurrentUserEmail().orElse("unknown");
        String userRole = SecurityUtils.getCurrentUserRole()
                .map(role -> role.name())
                .orElse("unknown");

        log.warn("Access denied for {} {} from user: {} (role: {}) at IP: {} - Reason: {}",
                method, requestURI, userEmail, userRole, remoteAddr, accessDeniedException.getMessage());

        // Build error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "Forbidden");
        errorResponse.put("message", "Access denied - insufficient permissions");
        errorResponse.put("details", accessDeniedException.getMessage());
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("path", requestURI);
        errorResponse.put("status", HttpServletResponse.SC_FORBIDDEN);

        // Set response properties
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding("UTF-8");

        // Write JSON response
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
