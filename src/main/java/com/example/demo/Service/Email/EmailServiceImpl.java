package com.example.demo.Service.Email;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService{
    
    @Autowired
    JavaMailSender emailSender;

    @Autowired
    ThymeleafService thymeleafService;

    private final String ADMIN_EMAIL = "admin@chic.com";
    private final String ADMIN_PERSONAL = "Admin ChicPants";

    
    @Override
    public void sendAccountInformation(String email, String username,String password) {
        try {

            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper message = new MimeMessageHelper(
                    mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            message.setFrom(ADMIN_EMAIL, ADMIN_PERSONAL);
            message.setTo(email);
            message.setSubject("Berhasil Register");

            Map<String, Object> variables = new HashMap<>();

            variables.put("email", email);
            variables.put("password", password);
            variables.put("username", username);

            message.setText(thymeleafService.createContext("mail/send-mail.html", variables), true);
            emailSender.send(mimeMessage);
        } catch (MessagingException me) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, me.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}
