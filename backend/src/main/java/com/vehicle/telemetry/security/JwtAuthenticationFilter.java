package com.vehicle.telemetry.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter - Intercepts every HTTP request and validates the JWT token.
 *
 * HOW FILTERS WORK IN SPRING:
 * - A filter is a piece of code that runs BEFORE your controller method.
 * - Every incoming HTTP request passes through a chain of filters.
 * - This filter checks if the request has a valid JWT token and, if so, authenticates the user.
 * - OncePerRequestFilter ensures this filter runs exactly once per request (not multiple times).
 *
 * REQUEST FLOW:
 *   1. Angular sends: GET /api/owner/vehicles with Header: "Authorization: Bearer eyJhbG..."
 *   2. This filter extracts the token from the header.
 *   3. It extracts the username from the token.
 *   4. It validates the token (correct signature, not expired).
 *   5. If valid, it sets up Spring Security's authentication context.
 *   6. The request continues to the controller method.
 *   7. If invalid/missing, the request continues unauthenticated (SecurityConfig will block it).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Used to extract username and validate JWT tokens */
    private final JwtService jwtService;

    /** Used to load user details from the database for authentication */
    private final UserDetailsService userDetailsService;

    /**
     * This method runs for EVERY HTTP request that comes into the server.
     *
     * @param request  The incoming HTTP request (contains headers, URL, body, etc.)
     * @param response The outgoing HTTP response (we don't modify it here)
     * @param filterChain The chain of filters - we call filterChain.doFilter() to pass to the next filter
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Step 1: Look for the "Authorization" header in the request
        final String authHeader = request.getHeader("Authorization");

        // Step 2: If no header or doesn't start with "Bearer ", skip JWT validation
        // (the request will continue but won't be authenticated - SecurityConfig will handle access)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);  // Pass to next filter
            return;
        }

        // Step 3: Extract the token (everything after "Bearer ")
        final String jwt = authHeader.substring(7);

        // Step 4: Extract the username from the JWT token
        final String username = jwtService.extractUsername(jwt);

        // Step 5: If we got a username AND user is not already authenticated in this request
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Step 6: Load the user's details from the database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Step 7: Validate the token (signature check + expiration check)
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Step 8: Create an authentication token and set it in Security Context
                // This tells Spring Security "this user is authenticated for this request"
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 9: Store authentication in the SecurityContext
                // After this, any @GetMapping/@PostMapping can access the authenticated user via Principal
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step 10: Continue to the next filter (and eventually to the controller)
        filterChain.doFilter(request, response);
    }
}