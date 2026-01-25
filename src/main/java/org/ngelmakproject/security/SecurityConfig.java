package org.ngelmakproject.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final GatewayAuthenticationFilter gatewayAuthenticationFilter;

    public SecurityConfig(GatewayAuthenticationFilter gatewayAuthenticationFilter) {
        this.gatewayAuthenticationFilter = gatewayAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.disable()) // Disable Spring Security CORS so Gateway handles it
                .csrf(csrf -> csrf.disable()) // Disable CSRF protection (not needed for token-based APIs)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/**").authenticated().requestMatchers(HttpMethod.PUT, "/**")
                        .authenticated().requestMatchers(HttpMethod.DELETE, "/**").authenticated().anyRequest()
                        .permitAll() // GET, OPTIONS, HEAD remain open
                )
                .addFilterBefore(gatewayAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
