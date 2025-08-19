package in.wealthinker.wealthinker.shared.exceptions;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends BusinessException {
    public ExternalServiceException(String message) {
        super(message, "EXTERNAL_SERVICE_ERROR", HttpStatus.SERVICE_UNAVAILABLE);
    }

    public ExternalServiceException(String message, String serviceName) {
        super(String.format("External service error from %s: %s", serviceName, message), 
              "EXTERNAL_SERVICE_ERROR", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
