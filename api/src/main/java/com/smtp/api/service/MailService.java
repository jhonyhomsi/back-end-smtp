package com.smtp.api.service;

import com.smtp.api.model.Mail;
import com.smtp.api.repository.MailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailService {
    
    @Autowired
    private MailRepository mailRepository;

    public Mail saveMail(Mail mail) {
        return mailRepository.save(mail);
    }

    public List<Mail> getReceivedMails(String email) {
        return mailRepository.findByTo(email);
    }

    public List<Mail> getSentMails(String email) {
        return mailRepository.findByFrom(email);
    }

    public Mail getEmailById(String emailId) {
        return mailRepository.findById(emailId)
                .orElseThrow(() -> new RuntimeException("Email not found"));
    }

    public void deleteMailById(String emailId) {
        mailRepository.deleteById(emailId);
    }    

    public long getUnopenedEmailCount(String email) {
        return mailRepository.countByToAndOpenedFalse(email);
    }    
}
