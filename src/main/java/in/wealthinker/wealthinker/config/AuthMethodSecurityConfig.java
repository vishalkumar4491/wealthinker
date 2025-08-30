package in.wealthinker.wealthinker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Method Security Configuration
 *
 * PURPOSE:
 * - Enable method-level security annotations
 * - Configure custom security expression handlers
 * - Support for @PreAuthorize, @PostAuthorize annotations
 * - Enable JSR-250 and Secured annotations
 *
 * ANNOTATIONS ENABLED:
 * - @PreAuthorize: Check authorization before method execution
 * - @PostAuthorize: Check authorization after method execution
 * - @Secured: Simple role-based authorization
 * - @RolesAllowed: JSR-250 role-based authorization
 */
@Configuration
@EnableMethodSecurity(
        prePostEnabled = true,   // Enables @PreAuthorize / @PostAuthorize
        securedEnabled = true,   // Enables @Secured
        jsr250Enabled = true     // Enables @RolesAllowed / @PermitAll
)
public class AuthMethodSecurityConfig {

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();

        // Optional: plug in custom PermissionEvaluator
        // handler.setPermissionEvaluator(customPermissionEvaluator);

        // Optional: plug in custom RoleHierarchy
        // handler.setRoleHierarchy(roleHierarchy);

        return handler;
    }
}

