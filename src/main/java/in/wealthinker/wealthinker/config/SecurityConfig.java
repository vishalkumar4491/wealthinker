 package in.wealthinker.wealthinker.config;

 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
 import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


 @Configuration
 public class SecurityConfig {
     @Bean
     public PasswordEncoder passwordEncoder() {
        System.out.println("mypasss " + new BCryptPasswordEncoder().encode("password"));
         return new BCryptPasswordEncoder(12);
     }

     @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // disable CSRF
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // allow all endpoints
            );
        return http.build();
    }
 }

