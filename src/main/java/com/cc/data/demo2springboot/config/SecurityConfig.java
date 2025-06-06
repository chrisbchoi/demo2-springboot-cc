package com.cc.data.demo2springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF protection for H2 console and APIs
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    AntPathRequestMatcher.antMatcher("/h2-console/**"),
                    AntPathRequestMatcher.antMatcher("/api/**")
                )
            )
            // Disable X-Frame-Options for H2 console
            .headers(headers -> headers
                .frameOptions().disable())
            // Allow all requests to H2 console and API endpoints
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/**")).permitAll()
                .anyRequest().authenticated())
            .formLogin(form -> form.permitAll());

        return http.build();
    }
}
