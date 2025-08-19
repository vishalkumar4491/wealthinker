package in.wealthinker.wealthinker.shared.exceptions;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends BusinessException {
    public AuthenticationException(String message) {
        super(message, "AUTH_ERROR", HttpStatus.UNAUTHORIZED);
    }

    public AuthenticationException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.UNAUTHORIZED);
    }
}
