package com.eazybytes.jobportal.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PathsConfig {

    @Bean("publicPaths")
    public List<String> publicPaths() {
        return List.of(
                "/api/contact",
                "/api/auth/login",
                "/api/auth/register",
                "/api/csrf-token"
        );
    }

    @Bean("privatePaths")
    public List<String> privatePaths() {
        return List.of(
                "/api/companies"
        );
    }

    @Bean("adminPaths")
    public List<String> adminPaths() {
        return List.of(
                "/api/contact/admin",
                "/api/contact/sort/admin"
        );
    }

}
