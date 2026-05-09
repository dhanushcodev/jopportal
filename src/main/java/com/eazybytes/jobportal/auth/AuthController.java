package com.eazybytes.jobportal.auth;

import com.eazybytes.jobportal.dto.LoginRequestDto;
import com.eazybytes.jobportal.dto.LoginResponseDto;
import com.eazybytes.jobportal.dto.UserDto;
import com.eazybytes.jobportal.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AuthController - REST API endpoint for user authentication
 *
 * This controller handles user login requests. The authentication flow:
 * 1. Receives username and password from client via /api/login endpoint
 * 2. Uses Spring Security's AuthenticationManager to authenticate the user
 * 3. Generates a JWT token upon successful authentication using JwtUtil
 * 4. Returns the token to the client for subsequent authenticated requests
 * 5. Handles invalid credentials with UNAUTHORIZED response
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    // Spring Security component that processes authentication requests
    private final AuthenticationManager authenticationManager;
    // Utility class for JWT token generation and validation
    private final JwtUtil jwtUtil;

    /**
     * Handles user login requests
     *
     * @param loginRequestDto Contains username and password from the client
     * @return LoginResponseDto with HTTP status, user details, and JWT token on success
     *         or UNAUTHORIZED status with error message on authentication failure
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {

        try {
            // Step 1: Create authentication token with provided username and password
            // The AuthenticationManager validates credentials against the configured UserDetailsService
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.username(),
                            loginRequestDto.password()
                    )
            );

            // Step 2: Generate JWT token containing user credentials and role information
            // Token expires in 1 hour and includes claims for username and role
            String jwtToken = jwtUtil.generateToken(authentication);

            // Step 3: Create response object with user details and token
            var userDto = new UserDto();
            // TODO: Remove debug logging in production
            System.out.println(loginRequestDto.username() + " " + loginRequestDto.password());
            
            // Step 4: Return successful response with HTTP 200 OK and JWT token
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new LoginResponseDto(HttpStatus.OK.getReasonPhrase(),
                            userDto, jwtToken));

        } catch (BadCredentialsException e) {
            // Authentication failed - invalid username or password
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

    }

}
