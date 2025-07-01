package com.ghost.invenarc.controller;

import com.ghost.invenarc.config.JwtConfig;
import com.ghost.invenarc.model.User;
import com.ghost.invenarc.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ProtectedController {

    private final JwtConfig jwtConfig;
    private final UserRepository userRepository;

    public ProtectedController(JwtConfig jwtConfig, UserRepository userRepository) {
        this.jwtConfig = jwtConfig;
        this.userRepository = userRepository;
    }

    @GetMapping("/api/test")
    public String testEndpoint() {
        System.out.println("=== Test endpoint called ===");
        return "Backend is working!";
    }

    @GetMapping("/api/protected")
    public ResponseEntity<Map<String, Object>> protectedEndpoint(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("error", "Unauthorized");
                return ResponseEntity.status(401).body(response);
            }

            String token = authHeader.substring(7);
            
            if (!jwtConfig.validateToken(token)) {
                response.put("error", "Invalid token");
                return ResponseEntity.status(401).body(response);
            }

            Claims claims = jwtConfig.parseToken(token);
            String email = claims.getSubject();
            
            // Get user data from database
            User user = userRepository.findByEmail(email);
            if (user != null) {
                response.put("email", user.getEmail());
                response.put("fullName", user.getFirstName() + " " + user.getLastName());
                response.put("firstName", user.getFirstName());
                response.put("lastName", user.getLastName());
                response.put("createdAt", "January 2024");
                response.put("status", "Active");
                response.put("message", "Welcome, " + email + "! Token is valid.");
            } else {
                // Fallback data if user not found in database
                response.put("email", email);
                response.put("fullName", "Demo User");
                response.put("firstName", "Demo");
                response.put("lastName", "User");
                response.put("createdAt", "January 2024");
                response.put("status", "Active");
                response.put("message", "Welcome, " + email + "! Token is valid.");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Invalid token: " + e.getMessage());
            return ResponseEntity.status(401).body(response);
        }
    }

    @GetMapping("/api/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        System.out.println("=== Profile endpoint called ===");
        System.out.println("Auth header: " + (authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null"));
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("Error: Invalid auth header format");
                response.put("error", "Unauthorized");
                return ResponseEntity.status(401).body(response);
            }

            String token = authHeader.substring(7);
            System.out.println("Token extracted: " + token.substring(0, Math.min(20, token.length())) + "...");
            
            if (!jwtConfig.validateToken(token)) {
                System.out.println("Error: Invalid token");
                response.put("error", "Invalid token");
                return ResponseEntity.status(401).body(response);
            }

            Claims claims = jwtConfig.parseToken(token);
            String email = claims.getSubject();
            System.out.println("Email from token: " + email);
            
            // Get user data from database
            User user = userRepository.findByEmail(email);
            System.out.println("User found in DB: " + (user != null ? "YES" : "NO"));
            
            if (user != null) {
                response.put("email", user.getEmail());
                response.put("fullName", user.getFirstName() + " " + user.getLastName());
                response.put("firstName", user.getFirstName());
                response.put("lastName", user.getLastName());
                response.put("createdAt", "January 2024");
                response.put("status", "Active");
                response.put("role", "User");
                response.put("lastLogin", "Recently");
                System.out.println("Profile data prepared for: " + user.getEmail());
            } else {
                // Fallback data if user not found in database
                response.put("email", email);
                response.put("fullName", "Demo User");
                response.put("firstName", "Demo");
                response.put("lastName", "User");
                response.put("createdAt", "January 2024");
                response.put("status", "Active");
                response.put("role", "User");
                response.put("lastLogin", "Recently");
                System.out.println("Using fallback data for: " + email);
            }
            
            System.out.println("Response: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Exception in profile endpoint: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Invalid token: " + e.getMessage());
            return ResponseEntity.status(401).body(response);
        }
    }

    @GetMapping("/api/profile/test")
    public ResponseEntity<Map<String, Object>> testProfileEndpoint() {
        System.out.println("=== Profile test endpoint called ===");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profile endpoint is accessible");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/debug")
    public ResponseEntity<Map<String, Object>> debugEndpoint() {
        System.out.println("=== Debug endpoint called ===");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Debug endpoint working");
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }
}
