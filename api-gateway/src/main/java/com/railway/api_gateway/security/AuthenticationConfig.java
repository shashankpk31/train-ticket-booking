package com.railway.api_gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import reactor.core.publisher.Mono;

@Configuration
public class AuthenticationConfig {

    private final JwtUtil jwtUtil;
    private final ReactiveUserDetailsService userDetailsService;

    @Autowired
    public AuthenticationConfig(JwtUtil jwtUtil, ReactiveUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        return authentication -> {
            String token = (String) authentication.getCredentials();
            return Mono.just(token)
                .filter(t -> {
                    try {
                        return jwtUtil.validateToken(t);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .flatMap(t -> {
                    String username = jwtUtil.extractUsername(t);
                    return userDetailsService.findByUsername(username)
                        .map(userDetails -> (Authentication) new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()))
                        .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)));
                })
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid JWT")));
        };
    }
}
