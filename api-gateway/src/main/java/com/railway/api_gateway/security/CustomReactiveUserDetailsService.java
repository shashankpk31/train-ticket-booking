
package com.railway.api_gateway.security;

import com.railway.api_gateway.model.User;
import com.railway.api_gateway.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CustomReactiveUserDetailsService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return Mono.fromCallable(() -> userRepository.findById(username)
            .map(user -> new CustomUserDetails(user.getUsername(), user.getPassword(), user.getRoles()))
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username)));
    }

    public Mono<User> registerUser(String username, String password, String roles) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);
        return Mono.fromCallable(() -> userRepository.save(user));
    }
}
