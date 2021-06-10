package com.etshost.msu.service;

import com.etshost.msu.entity.PasswordResetToken;
import com.etshost.msu.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Optional;
import java.time.Instant;
import java.util.Calendar;

@Service
public class PasswordResetTokenService {

    @Autowired
    private JavaMailSender mailSender;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${password.change.url}")
    private String passwordChangeUrl;


    public void sendTokenEmail(final String url, PasswordResetToken token) {


        String resetUrl = url +"/"+token.getToken();
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");
            helper.setSubject("FlintEats password reset request");
            helper.setTo(token.getUsr().getEmail());
            String body = "Hello FlintEats user,"
                    + "<br><br>A password reset request has been initiated on your account."
                    + "To reset your password, please follow the link below:"
                    + "<br><br><a href=\"" + resetUrl + "\">" + resetUrl + "</a>"
                    +"<br><br>If you did not make a password reset request, you can ignore this email.";
            message.setContent(body, "text/html");
            this.logger.info("Configured url is "+passwordChangeUrl);
            mailSender.send(message);
        } catch (MessagingException e) {
            // simply log it and go on...
            this.logger.info(e.getMessage());
        }
    }

    public String validatePasswordResetToken(String token) {
        PasswordResetToken passToken = null;
        try {
            passToken = PasswordResetToken.findPasswordResetTokensByTokenEquals(token).getSingleResult();
        } catch (Exception e) {
            this.logger.info(e.getMessage());
        }
        return !isTokenFound(passToken) ? "invalid"
                : isTokenExpired(passToken) ? "expired"
                : null;
    }

    public void expireToken(String token) {
        PasswordResetToken passToken = null;

        try {
            passToken = PasswordResetToken.findPasswordResetTokensByTokenEquals(token).getSingleResult();
            passToken.setExpiryDate(Instant.now());
            passToken.merge();
        } catch (Exception e) {
            this.logger.info(e.getMessage());
        }

    }

    public Optional<User> getUserByPasswordResetToken(String token) {
        PasswordResetToken passToken = null;
        try {
            passToken = PasswordResetToken.findPasswordResetTokensByTokenEquals(token).getSingleResult();
            return Optional.of(passToken.getUsr());
        } catch (Exception e) {
            this.logger.info(e.getMessage());
        }
        return Optional.empty();
    }

    private boolean isTokenFound(PasswordResetToken passToken) {
        return passToken != null;
    }

    private boolean isTokenExpired(PasswordResetToken passToken) {
        return passToken.getExpiryDate().isBefore(Instant.now());
    }

}
