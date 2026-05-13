package com.sitevisit.smartfieldoperations.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {

        try {

            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setFrom("nommypre@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            System.out.println("Email sent to: " + to);

        } catch (Exception e) {

            System.out.println("Failed to send email");
            e.printStackTrace();
        }
    }
}