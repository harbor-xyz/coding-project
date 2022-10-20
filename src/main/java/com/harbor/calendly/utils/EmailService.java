package com.harbor.calendly.utils;

import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.harbor.calendly.models.EmailRequest;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String sender;

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmailInvite(EmailRequest emailRequest) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailRequest.getFromAddress());
        mailMessage.setTo(emailRequest.getToAddress());
        mailMessage.setText(getMessageFromTemplate(emailRequest));
        mailMessage.setSubject(getSubjectLineFromTemplate(emailRequest));
        javaMailSender.send(mailMessage);
    }

    public String getMessageFromTemplate(EmailRequest emailRequest) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hi ");
        sb.append(emailRequest.getToName());
        sb.append(",\n\n");
        sb.append("Your interaction is confirmed with ").append(emailRequest.getFromName());
        sb.append(" for ").append(emailRequest.getEventName()).append(" between ");
        sb.append(DateTimeFormatter.ISO_DATE_TIME.format(emailRequest.getSlotStartDate())).append(" and ")
                .append(DateTimeFormatter.ISO_DATE_TIME.format(emailRequest.getSlotEndDate()));
        sb.append(".\n\nThank You, \nTeam Harbor !");
        return sb.toString();
    }

    public String getSubjectLineFromTemplate(EmailRequest emailRequest) {
        return "Confirmed: " + emailRequest.getEventName() + " on "
                + DateTimeFormatter.ISO_DATE_TIME.format(emailRequest.getSlotStartDate());
    }

}
