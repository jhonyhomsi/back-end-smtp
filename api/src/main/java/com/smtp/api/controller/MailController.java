package com.smtp.api.controller;

import com.smtp.api.model.Mail;
import com.smtp.api.model.User;
import com.smtp.api.repository.UserRepository;
import com.smtp.api.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/mail")
public class MailController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService;

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

    @GetMapping("/{emailId}")
    public ResponseEntity<?> getEmailById(@PathVariable String emailId, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Mail email = mailService.getEmailById(emailId);
    
        if (!email.getTo().equals(user.getEmail())) {
            return new ResponseEntity<>("You are not authorized to view this email.", HttpStatus.FORBIDDEN);
        }
    
        email.setOpened(true);
        mailService.saveMail(email);
    
        return ResponseEntity.ok(email);
    }
    
    @DeleteMapping("/{emailId}")
    public ResponseEntity<?> deleteEmailById(@PathVariable String emailId, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Mail email = mailService.getEmailById(emailId);
    
        if (!email.getTo().equals(user.getEmail()) && !email.getFrom().equals(user.getEmail())) {
            return new ResponseEntity<>("You are not authorized to delete this email.", HttpStatus.FORBIDDEN);
        }
    
        mailService.deleteMailById(emailId);
    
        return new ResponseEntity<>("Email deleted successfully", HttpStatus.OK);
    }

    @GetMapping("/unopened-count")
    public ResponseEntity<?> getUnopenedEmailCount(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        long unopenedCount = mailService.getUnopenedEmailCount(user.getEmail());
        return ResponseEntity.ok(unopenedCount);
    }
    
}
