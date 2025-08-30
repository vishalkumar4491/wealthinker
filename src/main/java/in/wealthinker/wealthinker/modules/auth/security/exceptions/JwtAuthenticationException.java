package in.wealthinker.wealthinker.modules.auth.security.exceptions;

import org.springframework.security.core.AuthenticationException;

/**
 * JWT Authentication Exception - Base exception for JWT-related authentication failures
 *
 * PURPOSE:
 * - Represents JWT-specific authentication errors
 * - Extends Spring Security's AuthenticationException
 * - Provides context for JWT validation failures
 * - Enables specific error handling for JWT issues
 *
 * EXTENDS AuthenticationException:
 * - Integrates with Spring Security exception handling
 * - Triggers authentication failure events
 * - Compatible with authentication entry points
 * - Supports security event auditing
 */
public class JwtAuthenticationException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final Object additionalInfo;

    /**
     * Constructor with message only
     */
    public JwtAuthenticationException(String message) {
        super(message);
        this.errorCode = "JWT_AUTH_ERROR";
        this.additionalInfo = null;
    }

    /**
     * Constructor with message and cause
     */
    public JwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "JWT_AUTH_ERROR";
        this.additionalInfo = null;
    }

    /**
     * Constructor with message and error code
     */
    public JwtAuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.additionalInfo = null;
    }

    /**
     * Constructor with message, error code, and cause
     */
    public JwtAuthenticationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.additionalInfo = null;
    }

    /**
     * Constructor with all parameters
     */
    public JwtAuthenticationException(String message, String errorCode, Object additionalInfo, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.additionalInfo = additionalInfo;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object getAdditionalInfo() {
        return additionalInfo;
    }

    @Override
    public String toString() {
        return "JwtAuthenticationException{" +
                "message='" + getMessage() + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", additionalInfo=" + additionalInfo +
                '}';
    }
}