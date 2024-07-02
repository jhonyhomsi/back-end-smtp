package com.smtp.api.repository;

import com.smtp.api.model.Mail;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MailRepository extends MongoRepository<Mail, String> {
    List<Mail> findByTo(String to);
    List<Mail> findByFrom(String from);
    long countByToAndOpenedFalse(String email);
}
