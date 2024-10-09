package com.co4gsl.indentitye2e_lms.controllers;

import com.co4gsl.indentitye2e_lms.models.JwtRequest;
import com.co4gsl.indentitye2e_lms.models.JwtResponse;
import com.co4gsl.indentitye2e_lms.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api/library/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<JwtResponse> signup(@RequestBody JwtRequest request) {
        return ResponseEntity.ok(authenticationService.signup(request));
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> signin(@RequestBody JwtRequest request) {
        return ResponseEntity.ok(authenticationService.signin(request));
    }
}
