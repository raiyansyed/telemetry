package com.vehicle.telemetry.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * JwtService - Handles creation and validation of JWT (JSON Web Tokens).
 *
 * WHAT IS JWT?
 * - JWT is a compact, self-contained token format used for authentication.
 * - After login, the server creates a JWT containing the username and expiration time.
 * - The frontend stores this token and sends it with every API request.
 * - The server validates the token to verify the user's identity WITHOUT hitting the database.
 *
 * JWT STRUCTURE (3 parts separated by dots):
 *   Header.Payload.Signature
 *   - Header: algorithm used (HS256) + token type (JWT)
 *   - Payload: username (subject), issue time, expiration time
 *   - Signature: HMAC-SHA256 hash of header+payload using the secret key
 *
 * TOKEN LIFECYCLE:
 *   1. User logs in -> generateToken() creates a JWT -> sent to frontend.
 *   2. Frontend stores token in localStorage.
 *   3. Every API request includes: Authorization: Bearer <token>.
 *   4. JwtAuthenticationFilter calls extractUsername() and isTokenValid() to verify.
 *   5. Token expires after 24 hours (86400000 ms) -> user must log in again.
 *
 * CONFIGURATION (from application.properties):
 *   app.jwt.secret = a 256-bit hex key used to sign/verify tokens
 *   app.jwt.expirationMs = 86400000 (24 hours in milliseconds)
 */
@Service
public class JwtService {

    /**
     * @Value injects the value from application.properties.
     * This is the secret key used to sign JWT tokens.
     * Anyone with this key can create valid tokens, so it must be kept secret!
     */
    @Value("${app.jwt.secret}")
    private String secret;

    /** How long (in ms) before a token expires. Default: 86400000ms = 24 hours. */
    @Value("${app.jwt.expirationMs}")
    private long expirationMs;

    /**
     * Extract the username (called "subject" in JWT terminology) from a token.
     * This is used by JwtAuthenticationFilter to identify who is making the API request.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Check if a token is valid: username matches AND token hasn't expired.
     * Called by JwtAuthenticationFilter for every authenticated API request.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Create a new JWT token for a user after successful authentication.
     * The token contains:
     *   - subject: the username
     *   - issuedAt: current time
     *   - expiration: current time + 24 hours
     * It's signed with HMAC-SHA256 using the secret key.
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())        // Who this token belongs to
                .setIssuedAt(new Date())                     // When the token was created
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))  // When it expires
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // Sign with secret key
                .compact();                                  // Build the final token string
    }

    // ---- PRIVATE HELPER METHODS ----

    /** Check if a token's expiration date is in the past */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Extract a specific claim from the token using a resolver function.
     * Claims are the data stored in the JWT payload (subject, expiration, etc.).
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())  // Use the secret key to verify the signature
                .build()
                .parseClaimsJws(token)           // Parse and validate the token
                .getBody();                      // Get the payload (claims)
        return claimsResolver.apply(claims);     // Extract the specific claim we want
    }

    /**
     * Convert the hex string secret from application.properties into a cryptographic Key object.
     * Keys.hmacShaKeyFor() creates a key suitable for HMAC-SHA256 signing.
     */
    private Key getSigningKey() {
        byte[] keyBytes = hexStringToByteArray(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** Convert a hexadecimal string to a byte array (e.g., "4F28" -> [0x4F, 0x28]) */
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}