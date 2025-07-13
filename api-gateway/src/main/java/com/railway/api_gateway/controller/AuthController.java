package com.railway.api_gateway.controller;

import com.railway.api_gateway.model.User;
import com.railway.api_gateway.security.CustomReactiveUserDetailsService;
import com.railway.api_gateway.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AuthController {

    private JwtUtil jwtUtil;
    private ReactiveAuthenticationManager authenticationManager;
    private CustomReactiveUserDetailsService userDetailsService;

    
    @Autowired
    public AuthController(JwtUtil jwtUtil, ReactiveAuthenticationManager authenticationManager,
			CustomReactiveUserDetailsService userDetailsService) {
		super();
		this.jwtUtil = jwtUtil;
		this.authenticationManager = authenticationManager;
		this.userDetailsService = userDetailsService;
	}

	@PostMapping("/auth/register")
    public Mono<User> register(@RequestBody User user) {
        return userDetailsService.registerUser(user.getUsername(), user.getPassword(), user.getRoles());
    }

    @PostMapping("/auth/login")
    public Mono<String> login(@RequestBody LoginRequest request) {
        return authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()))
            .flatMap(auth -> Mono.just(jwtUtil.generateToken(auth.getName(), jwtUtil.extractRoles((String) auth.getCredentials()))))
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
