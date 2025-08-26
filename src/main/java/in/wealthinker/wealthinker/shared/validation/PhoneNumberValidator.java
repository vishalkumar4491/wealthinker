package in.wealthinker.wealthinker.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    // E.164 format: +[country code][national number]
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:[6-9]\\d{9}|\\+?[1-9]\\d{1,14})$");

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        // Initialization if needed
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return true; // Let @NotBlank handle required validation
        }

        return PHONE_PATTERN.matcher(phoneNumber.trim()).matches();
    }
}
