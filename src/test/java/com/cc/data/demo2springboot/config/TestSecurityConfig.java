package com.cc.data.demo2springboot.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CSRF protection for our tests
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/users/**"))

            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Allow read operations for all users
                .requestMatchers("GET", "/api/users", "/api/users/**").permitAll()

                // Require ADMIN role for write operations
                .requestMatchers("POST", "/api/users").hasRole("ADMIN")
                .requestMatchers("PUT", "/api/users/**").hasRole("ADMIN")
                .requestMatchers("DELETE", "/api/users/**").hasRole("ADMIN")

                // All other requests need authentication
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
