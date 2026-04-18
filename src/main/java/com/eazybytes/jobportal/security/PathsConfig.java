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
                "/api/login"
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
        return List.of("/api/admin");
    }

}
