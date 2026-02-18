package com.example.demo.controllers;

import com.example.demo.dtos.AuthResponse;
import com.example.demo.dtos.LogInRequest;
import com.example.demo.dtos.RegisterRequest;
import com.example.demo.models.User;
import com.example.demo.service.CustomUserDetailsService;
import com.example.demo.service.UserService;
import com.example.demo.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            CustomUserDetailsService userDetailsService
    ) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LogInRequest request){
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        System.out.println("Register request received: " + request);

        if(userService.findByEmail(request.getEmail()).isPresent()){
            return ResponseEntity.badRequest().body("User already exist");
        }

        if(request.getEmail() == null || request.getEmail().isEmpty() ||
           request.getPassword() == null || request.getPassword().isEmpty() ||
           request.getFirstName() == null || request.getFirstName().isEmpty() ||
           request.getLastName() == null || request.getLastName().isEmpty()) {
            return ResponseEntity.badRequest().body("All fields are required");
        }

        userService.saveUser(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName(),
                request.getLastName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body("Account created successfully");
    }
}
