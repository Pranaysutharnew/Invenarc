package com.ghost.invenarc.controller;

import com.ghost.invenarc.config.JwtConfig;
import com.ghost.invenarc.model.User;
import com.ghost.invenarc.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;

    public AuthController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtConfig jwtConfig) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtConfig = jwtConfig;
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        try {
            if (userRepository.findByEmail(user.getEmail()) != null) {
                return "Email already registered";
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return "Account created successfully";
        } catch (Exception e) {
            return "Error creating account: " + e.getMessage();
        }
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        try {
            User existing = userRepository.findByEmail(user.getEmail());
            if (existing == null || !passwordEncoder.matches(user.getPassword(), existing.getPassword())) {
                return "Invalid credentials";
            }

            String token = jwtConfig.generateToken(existing.getEmail());
            return token;
        } catch (Exception e) {
            return "Error during login: " + e.getMessage();
        }
    }
}
