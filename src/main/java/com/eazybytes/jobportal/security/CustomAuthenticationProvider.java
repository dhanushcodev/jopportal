package com.eazybytes.jobportal.security;

import com.eazybytes.jobportal.entity.JobPortalUser;
import com.eazybytes.jobportal.user.repository.JobPortalUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final JobPortalUserRepository jobPortalUserRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * returns authentication object with principal = jobPortalUser after sucessful authentication
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        JobPortalUser jobPortalUser = jobPortalUserRepository.findByEmail(username).orElseThrow(
                ()-> new UsernameNotFoundException("Email not found"));

        var authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + jobPortalUser.getRole().getName())
        );

        if(passwordEncoder.matches(password,jobPortalUser.getPasswordHash())) {
            return new UsernamePasswordAuthenticationToken(jobPortalUser,null, authorities);
            // when authorities is passed into UsernamePasswordAuthenticationToken it makes .setAuthenticated(true)
            // we return jobPortalUser object
        }else{
            throw new BadCredentialsException("Invalid Username or password");
        }

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
