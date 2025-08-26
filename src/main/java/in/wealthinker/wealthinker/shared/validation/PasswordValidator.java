package in.wealthinker.wealthinker.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 25;

    // Regex patterns for password requirements
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[@$!%*?&].*");

    // Common passwords to reject (in production, use a comprehensive list)
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password", "123456", "123456789", "qwerty", "abc123",
            "password123", "admin", "letmein", "welcome", "monkey"
    );

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // Initialization if needed
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        // Check length
        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            addCustomMessage(context,
                    String.format("Password must be between %d and %d characters", MIN_LENGTH, MAX_LENGTH));
            return false;
        }

        // Check for uppercase letter
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            addCustomMessage(context, "Password must contain at least one uppercase letter");
            return false;
        }

        // Check for lowercase letter
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            addCustomMessage(context, "Password must contain at least one lowercase letter");
            return false;
        }

        // Check for digit
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            addCustomMessage(context, "Password must contain at least one digit");
            return false;
        }

        // Check for special character
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            addCustomMessage(context, "Password must contain at least one special character (@$!%*?&)");
            return false;
        }

        // Check against common passwords
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            addCustomMessage(context, "Password is too common, please choose a more secure password");
            return false;
        }

        // Check for repeating characters (basic check)
        if (hasRepeatingCharacters(password)) {
            addCustomMessage(context, "Password cannot have more than 2 consecutive identical characters");
            return false;
        }

        return true;
    }

    private boolean hasRepeatingCharacters(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) &&
                    password.charAt(i + 1) == password.charAt(i + 2)) {
                return true;
            }
        }
        return false;
    }

    private void addCustomMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
