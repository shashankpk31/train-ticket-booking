package com.railway.api_gateway.controller;

import com.railway.api_gateway.model.User;
import com.railway.api_gateway.security.CustomReactiveUserDetailsService;
import com.railway.api_gateway.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AuthController {

    private final JwtUtil jwtUtil;
    private final CustomReactiveUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(JwtUtil jwtUtil, CustomReactiveUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/register")
    public Mono<User> register(@RequestBody User user) {
        return userDetailsService.registerUser(user.getUsername(), user.getPassword(), user.getRoles());
    }

    @PostMapping("/auth/login")
    public Mono<String> login(@RequestBody LoginRequest request) {
        return userDetailsService.findByUsername(request.getUsername())
            .filter(userDetails -> passwordEncoder.matches(request.getPassword(), userDetails.getPassword()))
            .map(userDetails -> jwtUtil.generateToken(userDetails.getUsername(), userDetails.getAuthorities().toString()))
            .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid credentials")));
    }
}

class LoginRequest {
    private String username;
    private String password;
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
