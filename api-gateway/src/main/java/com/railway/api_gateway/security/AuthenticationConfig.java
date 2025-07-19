package com.railway.api_gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

@Configuration
public class AuthenticationConfig {
    private final JwtUtil jwtUtil;
    private final ReactiveUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationConfig(JwtUtil jwtUtil, ReactiveUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager passwordManager = 
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        passwordManager.setPasswordEncoder(passwordEncoder);

        return authentication -> {
            if (authentication.getCredentials() instanceof String token && token.contains(".")) {
                return Mono.just(token)
                        .filter(jwtUtil::validateToken)
                        .flatMap(t -> {
                            String username = jwtUtil.extractUsername(t);
                            String roles = jwtUtil.extractRoles(t);
                            return userDetailsService.findByUsername(username)
                                    .map(userDetails -> new UsernamePasswordAuthenticationToken(
                                            userDetails.getUsername(),
                                            null,
                                            java.util.Collections.singletonList(new SimpleGrantedAuthority(roles))))
                                    .cast(Authentication.class);
                        })
                        .switchIfEmpty(Mono.error(new org.springframework.security.core.AuthenticationException("Invalid JWT") {}));
            }
            return passwordManager.authenticate(authentication);
        };
    }
}