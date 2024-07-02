package com.smtp.api.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smtp.api.model.SecurityQuestionsRequest;
import com.smtp.api.model.TwoFARequest;
import com.smtp.api.model.User;
import com.smtp.api.service.UserService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody User user) {
        try {
            userService.registerUser(user.getFirst(), user.getLast(), user.getUsername(), user.getPassword(), user.getEmail(), user.getNumber());
            return ResponseEntity.ok("User registered successfully. Please log in.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user, HttpSession session) {
        Optional<User> optionalUser = userService.loginUser(user.getUsername(), user.getPassword());

        if (optionalUser.isPresent()) {
            User loggedInUser = optionalUser.get();
            session.setAttribute("user", loggedInUser);
            return ResponseEntity.ok(loggedInUser);
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody User user, HttpSession session) {
        try {
            User updatedUser = userService.updateUser(user);
            session.setAttribute("user", updatedUser);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwords) {
        try {
            userService.updatePassword(passwords.get("username"), passwords.get("currentPassword"), passwords.get("newPassword"));
            return ResponseEntity.ok("Password updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/update-2fa")
    public ResponseEntity<?> update2FA(@RequestBody TwoFARequest request) {
        try {
            userService.update2FA(request);
            return ResponseEntity.ok("Two-factor authentication settings updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/update-security-questions-user")
    public ResponseEntity<?> updateSecurityQuestions(@RequestBody SecurityQuestionsRequest request) {
        try {
            userService.updateSecurityQuestions(request);
            return ResponseEntity.ok("Security questions updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
