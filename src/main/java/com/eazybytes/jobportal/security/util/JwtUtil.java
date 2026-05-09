package com.eazybytes.jobportal.security.util;

import com.eazybytes.jobportal.constants.ApplicationConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JwtUtil - Utility class for JWT (JSON Web Token) operations
 *
 * This component provides methods for:
 * - Generating JWT tokens during login with user credentials and role information
 * - Validating JWT tokens on incoming requests (signature, expiration, structure)
 * - Extracting user information (username, role) from valid tokens
 *
 * Security Details:
 * - Uses HMAC-SHA256 algorithm for token signing
 * - Tokens are signed with a secret key for integrity verification
 * - Token includes claims: issuer, subject, username, role, issuedAt, expiration
 * - Tokens expire after 1 hour of issuance
 *
 * Token Format (JWT):
 * Header.Payload.Signature
 * - Header: algorithm and token type
 * - Payload: claims (username, role, timestamps, etc.)
 * - Signature: cryptographic signature using the secret key
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    // Secret key used for signing and validating tokens
    // Stored in ApplicationConstants for centralized configuration
    // This secret must be kept secure and should be moved to external configuration in production
    private final String SECRET = ApplicationConstants.JWT_SECRET_DEFAULT_VALUE;

    /**
     * Converts the secret string into a SecretKey for HMAC-SHA256
     * Called by generateToken(), validateToken(), and extract methods
     *
     * @return SecretKey derived from the configured secret string
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    /**
     * Generates a JWT token after successful user authentication
     *
     * Token includes:
     * - Issuer: "job-portal"
     * - Subject: "JWT TOKEN"
     * - Claims:
     *   - username: extracted from Authentication object
     *   - role: extracted from user authorities and formatted as "USER", "ADMIN", etc.
     * - IssuedAt: current system time
     * - Expiration: 1 hour from now (3600000 milliseconds)
     * - Signature: signed with the secret key for integrity
     *
     * @param authentication Spring Security Authentication object after successful login
     *                       Contains user principal with username and authorities
     * @return Base64-encoded JWT token string ready to be sent to client
     */
    public String generateToken(Authentication authentication) {

        // Extract the principal (user) from authentication object
        User principal = (User) authentication.getPrincipal();

        // Extract role from authorities (e.g., "ROLE_USER" -> "USER")
        String role = principal.getAuthorities()
                .stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("USER");

        // Build and sign the JWT token
        return Jwts.builder()
                .issuer("job-portal")        // Who issued the token
                .subject("JWT TOKEN")        // What the token is for
                .claim("username", principal.getUsername())  // Custom claim: username
                .claim("role", role)        // Custom claim: user's role
                .issuedAt(new Date())       // When token was created
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))  // Expires in 1 hour
                .signWith(getKey())         // Sign with secret key
                .compact();                 // Build final token string
    }

    /**
     * Validates a JWT token's signature, expiration, and structure
     *
     * Verification steps:
     * 1. Check that signature is valid (hasn't been tampered with)
     * 2. Check that token hasn't expired
     * 3. Check that token structure is valid
     *
     * @param token JWT token string from client
     * @return true if token is valid and not expired
     *         false if token is invalid or has expired
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getKey())    // Use secret key to verify signature
                    .build()
                    .parseSignedClaims(token);  // Parse and validate token

            return true;  // Token is valid

        } catch (Exception e) {
            return false;  // Token is invalid or expired
        }
    }

    /**
     * Extracts the username claim from a valid JWT token
     *
     * @param token JWT token string
     * @return Username stored in the "username" claim
     */
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("username").toString();
    }

    /**
     * Extracts the role claim from a valid JWT token
     *
     * @param token JWT token string
     * @return User's role stored in the "role" claim (e.g., "USER", "ADMIN")
     */
    public String extractRole(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

}