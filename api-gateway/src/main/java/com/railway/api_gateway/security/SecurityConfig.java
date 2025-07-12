package com.railway.api_gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.web.reactive.function.client.WebClient;

import com.railway.api_gateway.dto.UserDto;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WebClient webClient;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/login", "/register").permitAll()
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/trains/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                .pathMatchers("/inventory/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                .pathMatchers("/bookings/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                .anyExchange().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtDecoder(token -> Mono.just(jwtUtil.decodeJwt(token)))
                    .jwtAuthenticationConverter(jwt -> {
                        String username = jwtUtil.getUsernameFromToken(jwt.getTokenValue());
                        return userDetailsService().findByUsername(username)
                            .map(userDetails -> new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()));
                    })));

        return http.build();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        return username -> webClient.get()
            .uri("lb://booking-service/users/" + username)
            .retrieve()
            .onStatus(HttpStatus.NOT_FOUND::equals, response -> Mono.error(new RuntimeException("User not found")))
            .bodyToMono(UserDto.class)
            .map(user -> User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles().split(","))
                .build())
            .onErrorResume(e -> Mono.empty());
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}