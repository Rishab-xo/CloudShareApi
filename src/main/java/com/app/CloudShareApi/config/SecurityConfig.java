package com.app.CloudShareApi.config;

import com.app.CloudShareApi.security.ClerkJwtAuthFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ClerkJwtAuthFilter clerkJwtAuthFilter;

    /**
     * ZONE 1: Webhooks
     * This chain ONLY applies to URLs starting with /webhooks/
     * It has no JWT filter and permits all traffic (validation happens in the controller).
     */
    @Bean
    @Order(1) // Highest priority: Spring checks this chain first
    public SecurityFilterChain webhookFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/webhooks/**", "/files/public/**", "/files/download/**") // This chain exclusively handles webhooks
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        // Notice: We intentionally DO NOT add the clerkJwtAuthFilter here.

        return http.build();
    }

    /**
     * ZONE 2: Default API
     * This chain applies to everything else in your application.
     * It enforces CORS, requires authentication, and runs your JWT filter.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Explicitly wire the CORS configuration instead of relying on defaults
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
                        .requestMatchers("/error").permitAll()
                        // 2. EXPLICITLY allow all OPTIONS (preflight) requests to pass without authentication
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated() )
                .addFilterBefore(clerkJwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Prevent Spring Boot from auto-registering the JWT filter globally.
     * This ensures it ONLY runs when our API SecurityFilterChain explicitly calls it.
     */
    @Bean
    public FilterRegistrationBean<ClerkJwtAuthFilter> disableAutoRegistration(ClerkJwtAuthFilter filter) {
        FilterRegistrationBean<ClerkJwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        // This is the magic line. It disables the global Tomcat registration.
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.List.of("http://localhost:3000","http://localhost:5173", "https://cloud-share-project.vercel.app"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}