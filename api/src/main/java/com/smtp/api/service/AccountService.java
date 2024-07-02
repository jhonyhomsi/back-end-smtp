package com.smtp.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smtp.api.model.SecurityQuestionsRequest;
import com.smtp.api.model.User;
import com.smtp.api.repository.UserRepository;

@Service
public class AccountService {

    @Autowired
    private UserRepository userRepository;

    public void updateSecurityQuestions(SecurityQuestionsRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setSecurityQuestions(request.getSecurityQuestions());
        userRepository.save(user);
    }
}
