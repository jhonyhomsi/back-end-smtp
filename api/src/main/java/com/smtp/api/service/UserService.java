package com.smtp.api.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smtp.api.model.SecurityQuestionsRequest;
import com.smtp.api.model.TwoFARequest;
import com.smtp.api.model.TwoFAResponse;
import com.smtp.api.model.User;
import com.smtp.api.repository.UserRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final AccountService accountService;

    private final Argon2PasswordEncoder passwordEncoder;
    private final GoogleAuthenticator gAuth;

    public UserService(UserRepository userRepository, AccountService accountService) {
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.passwordEncoder = new Argon2PasswordEncoder(16, 32, 1, 65536, 3);
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
            .setNumberOfScratchCodes(5)
            .setWindowSize(5)
            .build();
        this.gAuth = new GoogleAuthenticator(config);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())
            .authorities("USER") // Adjust roles/authorities as needed
            .build();
    }

    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())
            .authorities("USER") // Adjust roles/authorities as needed
            .build();
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    public void registerUser(String firstName, String lastName, String username, String password, String email, String phoneNumber) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already taken");
        }
        User user = new User();
        user.setFirst(firstName);
        user.setLast(lastName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setNumber(phoneNumber);
        userRepository.save(user);
    }

    public Optional<User> loginUser(String loginId, String password) {
        Optional<User> userOpt;
        if (loginId.contains("@")) {
            userOpt = userRepository.findByEmail(loginId);
        } else {
            userOpt = userRepository.findByUsername(loginId);
        }

        return userOpt.filter(user -> passwordEncoder.matches(password, user.getPassword()));
    }

    public User updateUser(User user) {
        Optional<User> existingUserOpt = userRepository.findByUsername(user.getUsername());
        if (!existingUserOpt.isPresent()) {
            throw new IllegalArgumentException("User not found");
        }
        User existingUser = existingUserOpt.get();
        existingUser.setFirst(user.getFirst());
        existingUser.setLast(user.getLast());
        existingUser.setEmail(user.getEmail());
        existingUser.setNumber(user.getNumber());
        // If password is being updated
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(existingUser);
    }

    public void updatePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public TwoFAResponse generate2FA(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        GoogleAuthenticatorKey key = gAuth.createCredentials();
        user.setTwoFASecret(key.getKey());
        userRepository.save(user);

        String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL("J-Mail", user.getUsername(), key);
        TwoFAResponse response = new TwoFAResponse();
        response.setQrCodeUrl(qrCodeUrl);
        response.setSecretKey(key.getKey());
        return response;
    }

    public String verify2FA(TwoFARequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isCodeValid = gAuth.authorize(user.getTwoFASecret(), request.getToken());
        if (isCodeValid) {
            user.setTwoFAEnabled(true);
            userRepository.save(user);
            return "Two-factor authentication verified successfully.";
        } else {
            throw new IllegalArgumentException("Invalid verification code.");
        }
    }

    public void updateSecurityQuestions(SecurityQuestionsRequest request) {
        accountService.updateSecurityQuestions(request);
    }

    public boolean verifyLogin2FA(TwoFARequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    
        return gAuth.authorize(user.getTwoFASecret(), request.getToken());
    }

    public void update2FA(TwoFARequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.isTwoFAEnabled() != user.isTwoFAEnabled()) {
            user.setTwoFAEnabled(request.isTwoFAEnabled());
            if (!request.isTwoFAEnabled()) {
                user.setTwoFASecret(null);
            }
            userRepository.save(user);
        }
    }
}
