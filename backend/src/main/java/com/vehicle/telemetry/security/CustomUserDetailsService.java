package com.vehicle.telemetry.security;

import com.vehicle.telemetry.entity.User;
import com.vehicle.telemetry.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CustomUserDetailsService - Tells Spring Security how to load user data from our database.
 *
 * WHAT IS UserDetailsService?
 * - Spring Security needs to know how to find a user by their username during authentication.
 * - The UserDetailsService interface has one method: loadUserByUsername(String username).
 * - We implement it to look up the user in our MySQL database (via UserRepository).
 *
 * HOW AUTHENTICATION WORKS (step by step):
 * 1. User submits username + password via POST /api/auth/login.
 * 2. Spring Security's AuthenticationManager calls loadUserByUsername(username).
 * 3. This method queries the database for the User entity.
 * 4. It converts our User entity into Spring Security's UserDetails object.
 * 5. Spring Security compares the submitted password against the stored BCrypt hash.
 * 6. If they match -> authentication succeeds -> JWT token is generated.
 *
 * ROLE AUTHORITY:
 * - "ROLE_" prefix is a Spring Security convention. Our role "OWNER" becomes "ROLE_OWNER".
 * - This is used internally by Spring Security for role-based access control.
 */
@Service
@RequiredArgsConstructor  // Lombok: generates constructor for final fields (dependency injection)
public class CustomUserDetailsService implements UserDetailsService {

    /** Injected by Spring - used to query the users table in the database */
    private final UserRepository userRepository;

    /**
     * Load a user from the database and convert to Spring Security's UserDetails format.
     *
     * @param username The username to look up.
     * @return A UserDetails object containing username, hashed password, and role authority.
     * @throws UsernameNotFoundException if no user exists with that username.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Look up the user in the database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Convert our User entity to Spring Security's User object
        // The "ROLE_" prefix is required by Spring Security's role checking mechanism
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}