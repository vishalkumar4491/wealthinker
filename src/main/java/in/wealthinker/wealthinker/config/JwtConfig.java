package in.wealthinker.wealthinker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "wealthinker.security.jwt")
@Data
public class JwtConfig {
    private String secret = "wealthinkerSecretKeyForJwtTokenGenerationAndValidation2024!@#";
    private long expiration = 86400000; // 24 hours
    private long refreshExpiration = 604800000; // 7 days
    private String issuer = "wealthinker";
    private String audience = "wealthinker-users";
}
