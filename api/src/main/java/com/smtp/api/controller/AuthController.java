package com.smtp.api.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smtp.api.model.AuthenticationRequest;
import com.smtp.api.model.AuthenticationResponse;
import com.smtp.api.model.Mail;
import com.smtp.api.model.SecurityQuestionsRequest;
import com.smtp.api.model.TwoFARequest;
import com.smtp.api.model.TwoFAResponse;
import com.smtp.api.model.User;
import com.smtp.api.service.MailService;
import com.smtp.api.service.UserService;
import com.smtp.api.util.JwtUtil;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        String loginId = authenticationRequest.getUsername();
        String password = authenticationRequest.getPassword();

        UserDetails userDetails;
        if (loginId.contains("@")) {
            // Login using email
            userDetails = userService.loadUserByEmail(loginId);
        } else {
            // Login using username
            userDetails = userService.loadUserByUsername(loginId);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDetails.getUsername(), password)
            );
        } catch (AuthenticationException e) {
            throw new Exception("Incorrect username/email or password", e);
        }

        final String jwt = jwtUtil.generateToken(userDetails);
        final User user = userService.getUserByUsername(userDetails.getUsername());

        return ResponseEntity.ok(new AuthenticationResponse(jwt, user));
    }

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

    @PostMapping("/generate-2fa")
    public ResponseEntity<TwoFAResponse> generate2FA(@RequestBody Map<String, String> request) {
        try {
            TwoFAResponse response = userService.generate2FA(request.get("username"));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<String> verify2FA(@RequestBody TwoFARequest request) {
        try {
            String response = userService.verify2FA(request);
            return ResponseEntity.ok(response);
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

    @PostMapping("/send")
    public ResponseEntity<String> sendMail(@RequestBody Mail mail) {
        try {
            mailService.saveMail(mail);
            return new ResponseEntity<>("Mail sent successfully!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error sending mail: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/received")
    public ResponseEntity<?> getReceivedMails(@RequestParam String email) {
        try {
            List<Mail> receivedMails = mailService.getReceivedMails(email);
            return new ResponseEntity<>(receivedMails, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching received mails: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/sent")
    public ResponseEntity<?> getSentMails(@RequestParam String email) {
        try {
            List<Mail> sentMails = mailService.getSentMails(email);
            return new ResponseEntity<>(sentMails, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching sent mails: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/verify-login-2fa")
    public ResponseEntity<String> verifyLogin2FA(@RequestBody TwoFARequest request) {
        System.out.println("Verifying 2FA for user: " + request.getUsername());
        boolean isVerified = userService.verifyLogin2FA(request);
        if (isVerified) {
            return ResponseEntity.ok("Two-factor authentication verified successfully.");
        } else {
            return ResponseEntity.status(401).body("Invalid verification code.");
        }
    }    

    @PostMapping("/disable-2fa")
    public ResponseEntity<?> disable2FA(@RequestBody TwoFARequest request) {
        try {
            request.setTwoFAEnabled(false); // Set 2FA to disabled
            userService.update2FA(request);
            return ResponseEntity.ok("Two-factor authentication disabled successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
