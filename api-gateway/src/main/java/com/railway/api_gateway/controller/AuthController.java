package com.railway.api_gateway.controller;

import com.railway.api_gateway.model.User;
import com.railway.api_gateway.security.CustomReactiveUserDetailsService;
import com.railway.api_gateway.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AuthController {
	private ReactiveAuthenticationManager authenticationManager;
	private CustomReactiveUserDetailsService userDetailsService;
	private JwtUtil jwtUtil;
	
	@Autowired
	public AuthController(ReactiveAuthenticationManager authenticationManager,
			CustomReactiveUserDetailsService userDetailsService, JwtUtil jwtUtil) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@GetMapping("/auth/home")
	public Mono<ResponseEntity<String>> home() {
		return Mono.just(ResponseEntity.ok("Welcome to the authentication home page!"));
	}

	@PostMapping("/auth/register")
	public Mono<ResponseEntity<String>> register(@RequestBody User user) {
		return userDetailsService.registerUser(user)
				.map(savedUser -> ResponseEntity.ok("User registered successfully"));
	}

	@PostMapping("/auth/login")
	public Mono<ResponseEntity<String>> login(@RequestBody LoginRequest loginRequest) {
		return authenticationManager
				.authenticate(
						new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()))
				.flatMap(auth -> userDetailsService.findByUsername(loginRequest.getUsername())
						.map(userDetails -> ResponseEntity.ok(jwtUtil.generateToken(userDetails.getUsername(),
								userDetails.getAuthorities().iterator().next().getAuthority()))));
	}
}

class LoginRequest {
	private String username;
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}