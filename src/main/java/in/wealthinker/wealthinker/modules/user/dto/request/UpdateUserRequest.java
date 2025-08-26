package in.wealthinker.wealthinker.modules.user.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Update User Request DTO - Core user information updates
 *
 * PURPOSE:
 * - Update basic user account information (email, username, phone)
 * - Separate from profile updates for better security and validation
 * - Support partial updates (all fields optional)
 * - Prevent unauthorized field updates
 *
 * SECURITY CONSIDERATIONS:
 * - No password updates (separate endpoint for security)
 * - No role/status updates (admin-only operations)
 * - No sensitive audit fields exposed
 * - Email changes may trigger re-verification
 *
 * DESIGN DECISIONS:
 * - All fields optional for partial updates
 * - Validation only applies when field is provided
 * - Business validation in service layer (uniqueness checks)
 */

@Data
public class UpdateUserRequest {

    // Core Identity Fields
    @Email(message = "Email must be valid")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    private String email;

    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$",
            message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    private String username;

    @Pattern(
            regexp = "^(?:[6-9]\\d{9}|\\+?[1-9]\\d{1,14})$",
            message = "Phone number must be valid Indian or international format"
    )
    @Size(min = 10, max = 20)
    private String phoneNumber;

    // Account Preferences (non-sensitive)
    @Size(min = 2, max = 10, message = "Language code must be between 2 and 10 characters")
    @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "Language must be a valid ISO language code")
    private String language;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;

    // Notification Preferences
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean smsNotifications;
    private Boolean marketingEmails;

    // Custom Validation Methods

    /**
     * Check if any field is being updated
     *
     * BUSINESS LOGIC: Prevent empty update requests
     */
    @AssertTrue(message = "At least one field must be provided for update")
    public boolean hasUpdates() {
        return email != null ||
                username != null ||
                phoneNumber != null ||
                language != null ||
                timezone != null ||
                emailNotifications != null ||
                pushNotifications != null ||
                smsNotifications != null ||
                marketingEmails != null;
    }

    /**
     * Validate email format when provided
     *
     * BUSINESS RULE: Email must be properly formatted if provided
     */
    public boolean isEmailValid() {
        if (email == null || email.trim().isEmpty()) {
            return true; // Optional field
        }
        return email.contains("@") && email.contains(".") && email.length() >= 5;
    }

    /**
     * Validate username format when provided
     */
    public boolean isUsernameValid() {
        if (username == null || username.trim().isEmpty()) {
            return true; // Optional field
        }
        return username.matches("^[a-zA-Z0-9_.-]+$") && username.length() >= 4;
    }

    /**
     * Check if critical fields are being updated (require additional verification)
     *
     * SECURITY: Email and phone changes are considered critical
     */
    public boolean hasCriticalUpdates() {
        return email != null || phoneNumber != null;
    }

    /**
     * Get list of fields being updated for audit logging
     */
    public List<String> getUpdatedFields() {
        List<String> fields = new ArrayList<>();

        if (email != null) fields.add("email");
        if (username != null) fields.add("username");
        if (phoneNumber != null) fields.add("phoneNumber");
        if (language != null) fields.add("language");
        if (timezone != null) fields.add("timezone");
        if (emailNotifications != null) fields.add("emailNotifications");
        if (pushNotifications != null) fields.add("pushNotifications");
        if (smsNotifications != null) fields.add("smsNotifications");
        if (marketingEmails != null) fields.add("marketingEmails");

        return fields;
    }

}
