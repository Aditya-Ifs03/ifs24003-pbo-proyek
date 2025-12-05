package org.delcom.app.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    // Log error ke console agar kita tahu kenapa redirect terjadi
                    System.out.println("Unauthorized request to: " + req.getRequestURI());
                    res.sendRedirect("/auth/login");
                }))
            .authorizeHttpRequests(auth -> auth
                // WAJIB: Tambahkan "/error" di sini agar tidak looping saat ada error sistem
                .requestMatchers("/auth/**", "/assets/**", "/api/**", "/css/**", "/js/**", "/error", "/uploads/**")
                .permitAll()
                .anyRequest().authenticated())

            .formLogin(form -> form.disable())
            .logout(logout -> logout
                .logoutSuccessUrl("/auth/login")
                .permitAll())
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400)
            );

        return http.build();
    }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        
}