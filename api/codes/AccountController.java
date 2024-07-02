package com.smtp.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smtp.api.model.SecurityQuestionsRequest;
import com.smtp.api.service.AccountService;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/update-security-questions-account")
    public ResponseEntity<?> updateSecurityQuestions(@RequestBody SecurityQuestionsRequest request) {
        try {
            accountService.updateSecurityQuestions(request);
            return ResponseEntity.ok("Security questions updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
