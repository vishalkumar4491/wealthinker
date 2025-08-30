package in.wealthinker.wealthinker.modules.auth.security.jwt;

import in.wealthinker.wealthinker.modules.auth.security.exceptions.JwtAuthenticationException;
import in.wealthinker.wealthinker.modules.auth.security.userdetails.UserPrincipal;
import in.wealthinker.wealthinker.shared.constants.JwtConstants;
import in.wealthinker.wealthinker.shared.enums.UserRole;
import in.wealthinker.wealthinker.modules.auth.security.userdetails.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter - Validates JWT tokens on each request
 *
 * PURPOSE:
 * - Intercepts HTTP requests before they reach controllers
 * - Extracts and validates JWT tokens from Authorization header
 * - Creates Spring Security Authentication object
 * - Sets SecurityContext for the current request thread
 *
 * EXTENDS OncePerRequestFilter:
 * - Ensures filter runs only once per request
 * - Handles async requests properly
 * - Provides convenient doFilterInternal method
 *
 * SECURITY FLOW:
 * 1. Extract JWT token from request header
 * 2. Validate token (signature, expiration, blacklist)
 * 3. Extract user information from token claims
 * 4. Create UserPrincipal and Authentication object
 * 5. Set SecurityContext for current thread
 * 6. Continue filter chain to next filter/controller
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Main filter method - processes each HTTP request
     *
     * THREAD SAFETY:
     * - Each request runs in its own thread
     * - SecurityContext is thread-local
     * - Filter instance is shared but method variables are thread-safe
     *
     * ERROR HANDLING:
     * - Invalid tokens are logged but don't stop request processing
     * - Request continues as anonymous user if token validation fails
     * - This allows public endpoints to work without tokens
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.debug("Processing JWT authentication for {} {}", method, requestURI);

        try {
            // Step 1: Extract JWT token from request
            String jwtToken = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwtToken)) {
                log.debug("JWT token found in request header");

                // Step 2: Validate token and extract claims
                if (jwtTokenProvider.validateToken(jwtToken)) {
                    log.debug("JWT token validation successful");

                    // Step 3: Extract user information from token
                    Claims claims = jwtTokenProvider.extractClaims(jwtToken);

                    // Step 4: Create UserPrincipal from token claims
                    UserPrincipal userPrincipal = createUserPrincipalFromClaims(claims);

                    // Step 5: Create Authentication object
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userPrincipal,           // Principal (the user)
                                    null,                    // Credentials (not needed after authentication)
                                    userPrincipal.getAuthorities() // Authorities/Permissions
                            );

                    // Step 6: Set additional authentication details
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Step 7: Set SecurityContext for current request thread
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Authentication set in SecurityContext for user: {}", userPrincipal.getEmail());
                } else {
                    log.debug("JWT token validation failed");
                }
            } else {
                log.debug("No JWT token found in request");
            }
        } catch (JwtAuthenticationException e) {
            // Log authentication errors but don't block request
            log.warn("JWT authentication failed for {} {}: {}", method, requestURI, e.getMessage());

            // Clear any existing authentication
            SecurityContextHolder.clearContext();

        } catch (Exception e) {
            // Log unexpected errors
            log.error("Unexpected error during JWT authentication for {} {}", method, requestURI, e);

            // Clear any existing authentication
            SecurityContextHolder.clearContext();
        }

        // Step 8: Continue filter chain regardless of authentication result
        // This allows:
        // - Public endpoints to work without authentication
        // - Authorization filters to handle access control
        // - Controllers to check SecurityContext if needed
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     *
     * HEADER FORMAT: "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     *
     * SECURITY CONSIDERATIONS:
     * - Only accepts Bearer token format
     * - Trims whitespace to prevent bypass attempts
     * - Returns null if header is missing or malformed
     *
     * @param request HTTP request
     * @return JWT token string or null if not found
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        // Get Authorization header
        String bearerToken = request.getHeader(JwtConstants.AUTHORIZATION_HEADER);

        // Check if header exists and starts with "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtConstants.TOKEN_PREFIX)) {
            // Extract token part (remove "Bearer " prefix)
            String token = bearerToken.substring(JwtConstants.TOKEN_PREFIX.length()).trim();

            // Validate token format (basic check)
            if (token.split("\\.").length == 3) { // JWT has 3 parts separated by dots
                log.debug("JWT token extracted from Authorization header");
                return token;
            } else {
                log.warn("Invalid JWT token format in Authorization header");
                return null;
            }
        }

        // No token found
        return null;
    }

    /**
     * Create UserPrincipal from JWT token claims
     *
     * TOKEN-BASED AUTHENTICATION:
     * - Creates user object from token data (no database lookup needed)
     * - Includes role and permissions from token
     * - Faster than loading from database on each request
     *
     * SECURITY NOTE:
     * - Token data is trusted (already validated signature)
     * - Contains snapshot of user at token creation time
     * - May be slightly stale if user details changed after token issued
     *
     * @param claims JWT token claims
     * @return UserPrincipal object for SecurityContext
     */
    private UserPrincipal createUserPrincipalFromClaims(Claims claims) {
        // Extract required claims
        Long userId = claims.get(JwtConstants.CLAIM_USER_ID, Long.class);
        String email = claims.getSubject(); // Standard JWT subject claim
        String username = claims.get(JwtConstants.CLAIM_USERNAME, String.class);
        String roleString = claims.get(JwtConstants.CLAIM_ROLE, String.class);

        // Extract permissions list
        @SuppressWarnings("unchecked")
        List<String> permissions = claims.get(JwtConstants.CLAIM_PERMISSIONS, List.class);

        // Validate required claims
        if (userId == null || email == null || roleString == null) {
            throw new JwtAuthenticationException("Required claims missing from JWT token");
        }

        // Parse role
        UserRole role;
        try {
            role = UserRole.valueOf(roleString);
        } catch (IllegalArgumentException e) {
            throw new JwtAuthenticationException("Invalid role in JWT token: " + roleString);
        }

        // Create UserPrincipal from token data
        UserPrincipal userPrincipal = UserPrincipal.createFromToken(
                userId,
                username,
                email,
                role,
                permissions
        );

        log.debug("UserPrincipal created from JWT claims: userId={}, email={}, role={}",
                userId, email, role);

        return userPrincipal;
    }

    /**
     * Determine if filter should be applied to this request
     *
     * PERFORMANCE OPTIMIZATION:
     * - Skip JWT processing for public endpoints
     * - Skip for static resources (CSS, JS, images)
     * - Skip for health checks and monitoring endpoints
     *
     * @param request HTTP request
     * @return true if filter should be skipped
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Skip for public authentication endpoints
        if (path.startsWith("/api/v1/auth/login") ||
                path.startsWith("/api/v1/auth/register") ||
                path.startsWith("/api/v1/auth/refresh")) {
            return true;
        }

        // Skip for public utility endpoints
        if (path.startsWith("/api/v1/users/check-email") ||
                path.startsWith("/api/v1/users/check-username")) {
            return true;
        }

        // Skip for health checks and monitoring
        if (path.startsWith("/actuator/health") ||
                path.startsWith("/health") ||
                path.startsWith("/metrics")) {
            return true;
        }

        // Skip for API documentation
        if (path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/webjars")) {
            return true;
        }

        // Skip for static resources
        if (path.startsWith("/static") ||
                path.startsWith("/css") ||
                path.startsWith("/js") ||
                path.startsWith("/images")) {
            return true;
        }

        // Skip for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equals(method)) {
            return true;
        }

        // Process all other requests
        return false;
    }
}
