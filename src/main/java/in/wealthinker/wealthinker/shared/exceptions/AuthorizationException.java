package in.wealthinker.wealthinker.shared.exceptions;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends BusinessException {
    public AuthorizationException(String message) {
        super(message, "AUTHORIZATION_ERROR", HttpStatus.FORBIDDEN);
    }
}
