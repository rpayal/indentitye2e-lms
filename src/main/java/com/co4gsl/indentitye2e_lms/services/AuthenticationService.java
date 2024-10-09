package com.co4gsl.indentitye2e_lms.services;

import com.co4gsl.indentitye2e_lms.domain.User;
import com.co4gsl.indentitye2e_lms.models.JwtRequest;
import com.co4gsl.indentitye2e_lms.models.JwtResponse;
import com.co4gsl.indentitye2e_lms.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public JwtResponse signup(JwtRequest request) {
        var user = User.builder().username(request.getUsername()).password(passwordEncoder.encode(request.getPassword()))
                .build();
        userRepository.save(user);
        var jwt = jwtService.generateToken(user);
        return JwtResponse.builder().token(jwt).build();
    }

    public JwtResponse signin(JwtRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        var jwt = jwtService.generateToken(user);
        return JwtResponse.builder().token(jwt).build();
    }
}
