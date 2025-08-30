package in.wealthinker.wealthinker.modules.auth.security.jwt;

import lombok.*;

import java.time.Instant;

/**
 * Blacklist Statistics Data Transfer Object
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlacklistStats {
    private long totalBlacklistedTokens;
    private String storageType;
    private Instant lastUpdated;
    private String additionalInfo;
}
