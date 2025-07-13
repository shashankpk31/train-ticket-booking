package com.railway.api_gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Import this
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono; // Import this

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    // Removed direct injection of ReactiveAuthenticationManager from the constructor
    @Autowired
    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Configures the security filter chain for the webflux application.
     * This method defines authorization rules and adds the custom JWT authentication filter.
     *
     * @param http The ServerHttpSecurity instance to configure.
     * @param authenticationManager The ReactiveAuthenticationManager bean, injected here to break the cycle.
     * @return The configured SecurityWebFilterChain.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ReactiveAuthenticationManager authenticationManager) {
        http
            // Disable security context repository as we are using stateless JWTs
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            // Disable CSRF protection for API gateway
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            // Configure exception handling for unauthorized access
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
            // Define authorization rules for different paths
            .authorizeExchange(exchanges -> exchanges
                // Publicly accessible endpoints for authentication
                .pathMatchers("/auth/register", "/auth/login").permitAll()
                // Actuator endpoints for monitoring (also public)
                .pathMatchers("/actuator/**").permitAll()
                // Endpoints requiring USER or ADMIN roles
                .pathMatchers("/trains/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                .pathMatchers("/inventory/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                .pathMatchers("/bookings/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                // All other exchanges require authentication
                .anyExchange().authenticated())
            // Add the custom JWT authentication filter at the specified order
            .addFilterAt(jwtAuthenticationFilter(authenticationManager), SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }

    /**
     * Creates and configures the AuthenticationWebFilter for JWT authentication.
     *
     * @param authenticationManager The ReactiveAuthenticationManager bean, injected here.
     * @return The configured AuthenticationWebFilter.
     */
    @Bean
    public AuthenticationWebFilter jwtAuthenticationFilter(ReactiveAuthenticationManager authenticationManager) {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(authenticationManager);
        // Set the custom converter to extract JWT from the request
        filter.setServerAuthenticationConverter(jwtAuthenticationConverter());
        // Disable security context repository for this filter as well
        filter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());
        return filter;
    }

    /**
     * Defines the ServerAuthenticationConverter to extract and process JWT from the Authorization header.
     * This converter extracts the token, validates it, and sets user/roles in request headers.
     *
     * @return A ServerAuthenticationConverter instance.
     */
    @Bean
    public ServerAuthenticationConverter jwtAuthenticationConverter() {
        return exchange -> {
            String token = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // Remove "Bearer " prefix
                // Extract username and roles from the JWT using JwtUtil
                String username = jwtUtil.extractUsername(token);
                String roles = jwtUtil.extractRoles(token);

                // Mutate the request to add custom headers for downstream services
                exchange.getRequest().mutate()
                    .header("X-Auth-User", username)
                    .header("X-Auth-Roles", roles)
                    .build();
                // Return an authenticated token. The actual authentication manager will validate this token.
                return Mono.just(new UsernamePasswordAuthenticationToken(token, token));
            }
            return Mono.empty(); // No JWT found, proceed without authentication
        };
    }

    /**
     * Provides a BCryptPasswordEncoder bean for password hashing.
     *
     * @return A PasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
