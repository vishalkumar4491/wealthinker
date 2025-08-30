 package in.wealthinker.wealthinker.config;

 import in.wealthinker.wealthinker.modules.auth.security.jwt.JwtAccessDeniedHandler;
 import in.wealthinker.wealthinker.modules.auth.security.jwt.JwtAuthenticationEntryPoint;
 import in.wealthinker.wealthinker.modules.auth.security.jwt.JwtAuthenticationFilter;
 import in.wealthinker.wealthinker.modules.auth.security.userdetails.CustomUserDetailsService;
 import lombok.RequiredArgsConstructor;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
 import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
 import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
 import org.springframework.security.config.annotation.web.builders.HttpSecurity;
 import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
 import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
 import org.springframework.security.config.http.SessionCreationPolicy;
 import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
 import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
 import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
 import org.springframework.web.cors.CorsConfiguration;
 import org.springframework.web.cors.CorsConfigurationSource;
 import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

 import java.util.Arrays;

 /**
  * Spring Security Configuration with JWT Integration
  *
  * SECURITY ARCHITECTURE:
  * - Stateless authentication using JWT tokens
  * - Custom authentication filter for token validation
  * - Method-level security enabled
  * - CORS and CSRF configuration
  * - Exception handling for authentication errors
  */

 @Configuration
 @EnableWebSecurity
 @EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
 @RequiredArgsConstructor
 public class SecurityConfig {

     private final JwtAuthenticationFilter jwtAuthenticationFilter;
     private final CustomUserDetailsService userDetailsService;

     private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
     private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

     /**
      * Main security filter chain configuration
      *
      * FILTER ORDER (important for security):
      * 1. CORS Filter
      * 2. JWT Authentication Filter (custom filter)
      * 3. Username/Password Authentication Filter (Spring default)
      * 4. Authorization Filter
      */

     @Bean
     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
         return http
                 // Disable CSRF for stateless JWT authentication
                 .csrf(AbstractHttpConfigurer::disable)

                 // Configure CORS
                 .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                 // Configure session management
                 .sessionManagement(session -> session
                         .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                 // Configure authorization rules
                 .authorizeHttpRequests(auth -> auth
                         // Public endpoints - no authentication required
                         .requestMatchers(
                                 "/api/v1/auth/**",
                                 "/api/v1/users/check-email",
                                 "/api/v1/users/check-username",
                                 "/actuator/health",
                                 "/swagger-ui/**",
                                 "/v3/api-docs/**",
                                 "/swagger-resources/**",
                                 "/webjars/**",
                                 "/error"
                         ).permitAll()

                         // Admin endpoints
                         .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                         // All other endpoints require authentication
                         .anyRequest().authenticated())

                 // Configure authentication provider
                 .authenticationProvider(authenticationProvider())

                 // Add JWT filter BEFORE UsernamePasswordAuthenticationFilter
                 .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                 // Configure exception handling
                 .exceptionHandling(exception -> exception
                         .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                         .accessDeniedHandler(jwtAccessDeniedHandler))

                 .build();
     }

     /**
      * CORS configuration for cross-origin requests
      */
     @Bean
     public CorsConfigurationSource corsConfigurationSource() {
         CorsConfiguration configuration = new CorsConfiguration();

         // Allow specific origins (configure for your domains)
         configuration.setAllowedOriginPatterns(Arrays.asList(
                 "http://localhost:3000",    // React dev server
                 "http://localhost:4200",    // Angular dev server
                 "https://*.wealthinker.in", // Production domains
                 "https://wealthinker.in"
         ));

         // Allow specific methods
         configuration.setAllowedMethods(Arrays.asList(
                 "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
         ));

         // Allow specific headers
         configuration.setAllowedHeaders(Arrays.asList(
                 "Authorization",
                 "Content-Type",
                 "X-Requested-With",
                 "Accept",
                 "Origin",
                 "Cache-Control",
                 "Access-Control-Request-Method",
                 "Access-Control-Request-Headers"
         ));

         // Expose specific headers
         configuration.setExposedHeaders(Arrays.asList(
                 "Authorization",
                 "X-Total-Count",
                 "X-Page-Number",
                 "X-Page-Size"
         ));

         // Allow credentials
         configuration.setAllowCredentials(true);

         // Cache CORS preflight for 1 hour
         configuration.setMaxAge(3600L);

         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
         source.registerCorsConfiguration("/**", configuration);

         return source;
     }

     /**
      * Authentication provider for user credential validation
      */
     @Bean
     public DaoAuthenticationProvider authenticationProvider() {
         DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
         authProvider.setUserDetailsService(userDetailsService);
         authProvider.setPasswordEncoder(passwordEncoder());
         authProvider.setHideUserNotFoundExceptions(false);
         return authProvider;
     }

     /**
      * Authentication manager bean
      */
     @Bean
     public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
         return config.getAuthenticationManager();
     }

     /**
      * Password encoder bean
      */
     @Bean
     public PasswordEncoder passwordEncoder() {
         // BCrypt with strength 12 for strong security
         return new BCryptPasswordEncoder(12);
     }

 }

