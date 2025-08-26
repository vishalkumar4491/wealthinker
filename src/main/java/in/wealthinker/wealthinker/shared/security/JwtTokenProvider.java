package in.wealthinker.wealthinker.shared.security;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import in.wealthinker.wealthinker.modules.auth.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import in.wealthinker.wealthinker.config.JwtConfig;
import in.wealthinker.wealthinker.shared.enums.TokenType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;

    // Generate signing key from secret
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
    }

    /**
     * Generate JWT token from Spring Security Authentication object
     */
    public String generateToken(Authentication authentication, TokenType tokenType) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        return generateTokenFromUserDetails(
                userPrincipal.getEmail(),
                userPrincipal.getActualUsername(),
                userPrincipal.getPhoneNumber(),
                userPrincipal.getId(),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet()),
                tokenType
        );
    }

    /**
     * Generate JWT token from user details (useful for registration, etc.)
     */
    public String generateTokenFromUserDetails(String email, String username, String phoneNumber, 
                                             Long userId, Set<String> authorities, TokenType tokenType) {
        Instant now = Instant.now();
        Instant expiryDate = tokenType == TokenType.ACCESS 
            ? now.plus(jwtConfig.getExpiration(), ChronoUnit.MILLIS)
            : now.plus(jwtConfig.getRefreshExpiration(), ChronoUnit.MILLIS);

        return Jwts.builder()
                .setSubject(email) // Email is the primary subject
                .setIssuer(jwtConfig.getIssuer())
                .setAudience(jwtConfig.getAudience())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .claim("userId", userId)
                .claim("username", username)
                .claim("phoneNumber", phoneNumber)
                .claim("authorities", authorities)
                .claim("tokenType", tokenType.name())
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extract email from JWT token
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * Extract username from JWT token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("username", String.class);
    }

    /**
     * Extract phone number from JWT token
     */
    public String getPhoneNumberFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("phoneNumber", String.class);
    }

    /**
     * Extract user ID from JWT token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Extract authorities from JWT token
     */
    @SuppressWarnings("unchecked")
    public Set<String> getAuthoritiesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return (Set<String>) claims.get("authorities");
    }

    /**
     * Get expiration date from JWT token
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Check if JWT token is expired
     */
    public boolean isTokenExpired(String token) {
        Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.before(new Date());
    }

    /**
     * Extract all claims from JWT token
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}