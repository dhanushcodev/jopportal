package com.eazybytes.jobportal.security;

import com.eazybytes.jobportal.security.filter.JwtTokenValidatorFilter;
import com.eazybytes.jobportal.security.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * SecurityConfig - Spring Security configuration for the Job Portal application
 *
 * This configuration class sets up:
 * - JWT-based authentication filter (JwtTokenValidatorFilter)
 * - CORS (Cross-Origin Resource Sharing) settings for frontend communication
 * - Path-based authorization: public, authenticated, and admin-only routes
 * - Password encoder using BCrypt
 * - In-memory user details service (for development; replace with database in production)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // List of paths that don't require authentication (e.g., /api/login, /api/register)
    @Autowired
    @Qualifier(value = "publicPaths")
    public List<String> publicPaths;

    // List of paths that require authentication (e.g., /api/jobs, /api/companies)
    @Autowired
    @Qualifier(value = "privatePaths")
    public List<String> privatePaths;

    // List of paths that require admin role (e.g., /api/admin/**)
    @Autowired
    @Qualifier(value = "adminPaths")
    public List<String> adminPaths;



    /**
     * Configures the main security filter chain
     *
     * Authorization flow for each request:
     * 1. CSRF protection disabled (using JWT instead)
     * 2. CORS enabled to allow frontend (http://localhost:5173) to communicate
     * 3. Request authorization based on configured paths:
     *    - Public paths: no authentication required
     *    - Private paths: JWT token required
     *    - Admin paths: JWT token with ADMIN role required
     * 4. Custom JWT filter (JwtTokenValidatorFilter) validates Bearer tokens
     * 5. Form login and HTTP basic auth disabled (JWT-only authentication)
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection since we're using stateless JWT authentication
                .csrf(AbstractHttpConfigurer::disable)
                // Configure CORS to allow requests from frontend application
                .cors(corsConfig ->
                        corsConfig.configurationSource(corsConfigurationSource()))
                // Define authorization rules based on request paths
                .authorizeHttpRequests(requests -> {
                            // Allow public paths without authentication
                            publicPaths.forEach(path -> requests.requestMatchers(path).permitAll());
                            // Require authentication for private paths
                            privatePaths.forEach(path -> requests.requestMatchers(path).authenticated());
                            // Require ADMIN role for admin paths, and authentication for all other requests
                            adminPaths.forEach(path -> requests.requestMatchers(path).hasRole("ADMIN").anyRequest().authenticated());
                        }
                )
                // Add JWT validation filter before basic authentication filter
                // This filter extracts JWT from Authorization header and sets security context
                .addFilterBefore(new JwtTokenValidatorFilter(new JwtUtil(),publicPaths), BasicAuthenticationFilter.class)
                // Disable form-based login (we're using JWT)
                .formLogin(AbstractHttpConfigurer::disable)
                // Enable HTTP Basic authentication as fallback
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    /**
     * Provides the AuthenticationManager bean used by AuthController
     * This manager is responsible for processing authentication requests
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings
     *
     * Allows the frontend application running on http://localhost:5173 to:
     * - Make requests to this backend API
     * - Access response headers
     * - Use credentials in requests
     *
     * TODO: In production, restrict origins to specific domain instead of "*"
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // Allow requests from any origin (development only - restrict in production)
        corsConfiguration.addAllowedOrigin("*");
        // Allow all headers in request
        corsConfiguration.addAllowedHeader("*");
        // Allow all HTTP methods (GET, POST, PUT, DELETE, etc.)
        corsConfiguration.addAllowedMethod("*");
        // Allow credentials (cookies, authorization headers) in cross-origin requests
        corsConfiguration.setAllowCredentials(true);

        // Register CORS configuration for all routes
        UrlBasedCorsConfigurationSource source = new  UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }

    /**
     * Provides user details for authentication
     *
     * Currently uses in-memory user storage with two test users:
     * - Username: "dhanush", Password: "1234", Role: USER
     * - Username: "admin", Password: "admin123", Role: ADMIN
     *
     * TODO: Replace with database-backed UserDetailsService for production
     *       Implement a service that queries the JobPortalUser entity from database
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("dhanush")
                .password(passwordEncoder().encode("1234"))
                .roles("USER")
                .build();

        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user,admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
