package com.harbor.calendly.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;

public class EmailService {

    @Value("${spring.mail.username}")
    private String sender;

    @Autowired
    private JavaMailSender javaMailSender;

    // SimpleMailMessage mailMessage = new SimpleMailMessage();
    // mailMessage.setFrom("progix.21@gmail.com");
    // mailMessage.setTo("progix.21@gmail.com");
    // mailMessage.setText("Hello World of Messages");
    // mailMessage.setSubject("This is a test message");
    // javaMailSender.send(mailMessage);
}
