package com.railway.api_gateway.security;

import com.railway.api_gateway.model.User;
import com.railway.api_gateway.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import java.util.Collections;

@Service
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Mono<UserDetails> findByUsername(String username) {
        return Mono.fromCallable(() -> userRepository.findByUsername(username))
                .flatMap(user -> user != null ? Mono.just(user) : Mono.empty())
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority(user.getRoles()))));
    }

    @Transactional
    public Mono<User> registerUser(User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return Mono.fromCallable(() -> userRepository.save(user))
                    .onErrorMap(e -> new RuntimeException("Failed to register user: " + e.getMessage(), e));
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Failed to register user: " + e.getMessage(), e));
        }
    }
}