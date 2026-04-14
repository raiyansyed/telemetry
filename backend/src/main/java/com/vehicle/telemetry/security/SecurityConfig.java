package com.vehicle.telemetry.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * SecurityConfig - Configures Spring Security for the entire application.
 *
 * THIS IS THE MOST IMPORTANT SECURITY FILE. It controls:
 * 1. Which API endpoints are public (no login needed) vs. protected (JWT required).
 * 2. How passwords are hashed (BCrypt).
 * 3. CORS settings (allows the Angular frontend at localhost:4200 to call the backend at localhost:9090).
 * 4. Session management (stateless - no server-side sessions, only JWT tokens).
 * 5. Where the JWT filter is inserted in the filter chain.
 *
 * WHAT IS @Configuration?
 * - Marks this class as a source of Spring bean definitions.
 * - @Bean methods return objects that Spring manages and injects where needed.
 *
 * WHAT IS @EnableWebSecurity?
 * - Activates Spring Security's web security features for this application.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** Our custom JWT filter that validates tokens on every request */
    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * SecurityFilterChain - The core security configuration.
     * This @Bean method defines all the security rules for the application.
     *
     * @param http The HttpSecurity builder provided by Spring Security.
     * @return A configured SecurityFilterChain that Spring uses to secure all requests.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF (Cross-Site Request Forgery) protection is disabled because we use JWT tokens.
            // CSRF protection is for browser-based cookie sessions, which we don't use.
            .csrf(csrf -> csrf.disable())

            // CORS configuration - allows the Angular frontend to make requests to this backend.
            // Without this, the browser would block requests from localhost:4200 to localhost:9090.
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // URL-based authorization rules:
            .authorizeHttpRequests(auth -> auth
                // These endpoints are PUBLIC (no login required):
                // - /api/auth/** includes login, register, and locations endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // ALL other endpoints require authentication (valid JWT token):
                .anyRequest().authenticated()
            )

            // Session management: STATELESS means the server does NOT create HTTP sessions.
            // Each request is authenticated independently via the JWT token.
            // This is typical for REST APIs consumed by single-page applications (like Angular).
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Insert our JWT filter BEFORE Spring's default username/password filter.
            // This ensures our JWT validation runs first for every request.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS Configuration - Controls which origins (domains) can access this API.
     *
     * WHAT IS CORS?
     * - Cross-Origin Resource Sharing is a browser security feature.
     * - By default, a web page at localhost:4200 CANNOT make requests to localhost:9090
     *   because they're different "origins" (different ports).
     * - CORS headers tell the browser "it's OK, allow this cross-origin request."
     *
     * This configuration allows:
     * - Origin: localhost:4200 (Angular dev server)
     * - All HTTP methods (GET, POST, PUT, DELETE, etc.)
     * - All headers (including Authorization for JWT)
     * - Credentials (cookies, though we don't use them)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));  // Angular dev server
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));  // Allow all headers
        configuration.setAllowCredentials(true);         // Allow credentials

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // Apply to all URL paths
        return source;
    }

    /**
     * PasswordEncoder Bean - Defines how passwords are hashed.
     *
     * BCrypt is a strong, slow hashing algorithm designed for passwords.
     * "Slow" is intentional: it makes brute-force attacks much harder.
     * Each hash includes a random salt, so identical passwords produce different hashes.
     *
     * Used during:
     * - Registration: raw password -> BCrypt hash -> stored in database
     * - Login: raw password -> compared against stored BCrypt hash
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager Bean - Spring Security's central authentication coordinator.
     *
     * This is the object that AuthController uses to validate username + password.
     * It delegates to CustomUserDetailsService to load the user, then uses
     * PasswordEncoder to compare the submitted password against the stored hash.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}