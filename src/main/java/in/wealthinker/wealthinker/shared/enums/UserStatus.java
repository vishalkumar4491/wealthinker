package in.wealthinker.wealthinker.shared.enums;

public enum UserStatus {
    PENDING_VERIFICATION,  // Just registered, email not verified
    ACTIVE,               // Fully active user
    INACTIVE,            // User deactivated their account
    SUSPENDED,           // Temporarily suspended by admin
    BLOCKED,             // Permanently blocked
    PENDING_KYC,         // Waiting for KYC verification (fintech requirement)
    KYC_REJECTED,        // KYC documents rejected
    DELETED              // Soft deleted (GDPR compliance)
}
