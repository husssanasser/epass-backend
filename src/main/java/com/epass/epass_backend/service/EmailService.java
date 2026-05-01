package com.epass.epass_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendApprovalEmail(String toEmail, String userName) {
        String subject = "E-Pass Permit Approved ✅";
        String body = "Dear " + userName + ",\n\n"
                + "Your permit request has been APPROVED.\n"
                + "Please login to your dashboard to download your E-Pass.\n\n"
                + "E-Pass System";
        sendEmail(toEmail, subject, body);
    }

    public void sendRejectionEmail(String toEmail, String userName) {
        String subject = "E-Pass Permit Rejected ❌";
        String body = "Dear " + userName + ",\n\n"
                + "Unfortunately your permit request has been REJECTED.\n"
                + "Please contact the admin for more information.\n\n"
                + "E-Pass System";
        sendEmail(toEmail, subject, body);
    }
}