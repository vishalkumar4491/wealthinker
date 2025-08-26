package in.wealthinker.wealthinker.config;

import in.wealthinker.wealthinker.shared.audit.AuditAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing Configuration
 *
 * WHY THIS EXISTS:
 * - Financial regulations require knowing WHO changed WHAT and WHEN
 * - SOX compliance mandates audit trails for all data changes
 * - Security investigations need to trace data modifications
 *
 * HOW IT WORKS:
 * - @CreatedBy/@LastModifiedBy automatically filled with current user
 * - @CreatedDate/@LastModifiedDate automatically filled with timestamp
 * - Works across all entities that extend Auditable
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditAware();
    }
}
