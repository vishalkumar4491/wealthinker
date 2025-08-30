package in.wealthinker.wealthinker.modules.auth.security.exceptions;

import java.util.List;
import java.util.Map;

/**
 * JWT Validation Exception - Specific exception for token validation failures
 *
 * PURPOSE:
 * - Detailed validation error information
 * - Support for multiple validation failures
 * - Structured error data for debugging
 * - Integration with validation frameworks
 */
public class JwtValidationException extends JwtAuthenticationException {

    private static final long serialVersionUID = 1L;

    private final List<String> validationErrors;
    private final Map<String, Object> tokenClaims;
    private final String validationStage;

    /**
     * Constructor with single validation error
     */
    public JwtValidationException(String message, String validationStage) {
        super(message, "JWT_VALIDATION_FAILED");
        this.validationErrors = List.of(message);
        this.validationStage = validationStage;
        this.tokenClaims = null;
    }

    /**
     * Constructor with multiple validation errors
     */
    public JwtValidationException(String message, List<String> validationErrors, String validationStage) {
        super(message, "JWT_VALIDATION_FAILED");
        this.validationErrors = validationErrors;
        this.validationStage = validationStage;
        this.tokenClaims = null;
    }

    /**
     * Constructor with token claims for debugging
     */
    public JwtValidationException(String message, List<String> validationErrors,
                                  String validationStage, Map<String, Object> tokenClaims) {
        super(message, "JWT_VALIDATION_FAILED");
        this.validationErrors = validationErrors;
        this.validationStage = validationStage;
        this.tokenClaims = tokenClaims;
    }

    /**
     * Constructor with cause
     */
    public JwtValidationException(String message, String validationStage, Throwable cause) {
        super(message, "JWT_VALIDATION_FAILED", cause);
        this.validationErrors = List.of(message);
        this.validationStage = validationStage;
        this.tokenClaims = null;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public String getValidationStage() {
        return validationStage;
    }

    public Map<String, Object> getTokenClaims() {
        return tokenClaims;
    }

    public boolean hasMultipleErrors() {
        return validationErrors.size() > 1;
    }

    @Override
    public String toString() {
        return "JwtValidationException{" +
                "message='" + getMessage() + '\'' +
                ", validationStage='" + validationStage + '\'' +
                ", errorCount=" + validationErrors.size() +
                ", validationErrors=" + validationErrors +
                '}';
    }
}
