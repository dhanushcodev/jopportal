package com.eazybytes.jobportal.security.filter;

import com.eazybytes.jobportal.security.util.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JwtTokenValidatorFilter - Spring Security filter for JWT token validation
 *
 * This filter is executed for every HTTP request and is responsible for:
 * 1. Extracting JWT tokens from the Authorization header (Bearer scheme)
 * 2. Validating the token's signature and expiration
 * 3. Extracting user information (username, role) from the token
 * 4. Setting the Spring Security context with authenticated user details
 * 5. Allowing public paths to bypass token validation
 *
 * Filter execution order:
 * - Applied before BasicAuthenticationFilter in the security filter chain
 * - Runs once per request (OncePerRequestFilter)
 * - Skips validation for public paths configured in PathsConfig
 *
 * Token validation process:
 * - If token is valid: security context is set with user authentication
 * - If token is expired: returns 401 UNAUTHORIZED
 * - If token is invalid: throws BadCredentialsException
 * - If no token provided: request proceeds to next filter (may be denied later if route requires auth)
 */
public class JwtTokenValidatorFilter extends OncePerRequestFilter {

    // Used to match request paths against configured public paths (supports wildcards like /api/**)
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    // Utility class for JWT validation and claim extraction
    private final JwtUtil jwtUtil;
    // List of paths that don't require JWT authentication
    private final List<String> publicPaths;


    /**
     * Constructor for JwtTokenValidatorFilter
     *
     * @param jwtUtil Utility for JWT token operations
     * @param publicPaths Paths that don't require authentication
     */
    public JwtTokenValidatorFilter(JwtUtil jwtUtil, List<String> publicPaths) {
        this.jwtUtil = jwtUtil;
        this.publicPaths = publicPaths;
    }

    /**
     * Main filter method that validates JWT tokens on each request
     *
     * @param request HTTP request containing optional Authorization header
     * @param response HTTP response (used to send error status if token is invalid)
     * @param filterChain Chain of filters to continue processing
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: Extract Authorization header from the request
        // Expected format: "Bearer <jwt_token>"
        String header = request.getHeader("Authorization");

        // Step 2: Check if Authorization header exists and contains Bearer token
        if (header != null && header.startsWith("Bearer ")) {
            // Remove "Bearer " prefix to get the actual JWT token
            String token = header.substring(7);

            try {
                // Step 3: Validate token signature and structure
                if (jwtUtil.validateToken(token)) {
                    // Step 4: Extract user information from the valid token
                    // Claims embedded in token: username, role, issuedAt, expiration
                    String username = jwtUtil.extractUsername(token);
                    String role = jwtUtil.extractRole(token);

                    // Step 5: Create authorities list from user's role
                    // Spring Security requires roles to be prefixed with "ROLE_"
                    var authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + role)
                    );

                    // Step 6: Create authentication object
                    // First parameter: username (principal)
                    // Second parameter: null (no credentials needed for JWT)
                    // Third parameter: authorities/roles
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    username, null, authorities);

                    // Step 7: Set authentication in Spring Security context
                    // This makes the user "logged in" for the current request
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            catch (ExpiredJwtException expiredJwtException) {
                // Token was valid but has expired
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("Token is expired");
                return; // Don't continue to next filter
            }
            catch (Exception ignored) {
                // Token validation failed (invalid signature, malformed, etc.)
                throw new BadCredentialsException("Invalid Token");
            }
        }

        // Step 8: Continue processing with next filter in chain
        // If no token was provided, request will be denied later if the route requires authentication
        filterChain.doFilter(request, response);
    }

    /**
     * Determines if this filter should be applied to the current request
     * Returns true to skip filter for public paths (paths that don't require authentication)
     *
     * @param request HTTP request
     * @return true if request path matches a public path (filter will be skipped)
     *         false to apply the filter
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return publicPaths.stream().anyMatch(publicPath ->
                pathMatcher.match(publicPath, path));
    }
}
