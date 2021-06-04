package com.etshost.msu.entity;
import java.time.Instant;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;

/**
 * A token for facilitating secure handling of {@link User} password reset requests.
 */
@javax.persistence.Entity
@RooJavaBean
@RooJpaActiveRecord(finders = { "findPasswordResetTokensByTokenEquals" })
@RooJson
public class PasswordResetToken extends Entity {

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_fk")
    private User usr;

    @Column(name = "expiry_date")
	@DateTimeFormat(style = "MM")
    private Instant expiryDate;



    public void sendTokenEmailRegister(final String url, final String token, final String email) {
        if (email == null) {
            return;
        }
        String resetUrl = url + token;
        MailSender mailSender = new JavaMailSenderImpl();
        MimeMessage message = ((JavaMailSenderImpl) mailSender).createMimeMessage();
        ((JavaMailSenderImpl) mailSender).setHost("imap.etshost.com");
        ((JavaMailSenderImpl) mailSender).setPort(465);
        ((JavaMailSenderImpl) mailSender).setUsername("chass@etshost.com");
        ((JavaMailSenderImpl) mailSender).setPassword("EtS0199!");
        final Properties props = ((JavaMailSenderImpl) mailSender).getJavaMailProperties();
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.send.protocol", "smtps");
        ((JavaMailSenderImpl) mailSender).setJavaMailProperties(props);
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");
            helper.setSubject("Double Up Food Bucks Web Portal");
            helper.setFrom("dufb@etshost.com");
            helper.setTo(email);
            helper.setCc("dufb@etshost.com");
            String body = "Dear Double Up Food Bucks customer,"
		            + "<br>You are one step away from being able to log in to the Double Up web portal."
		            + "Please follow the link below to set your password:"
		            + "<br><br><a href=\"" + resetUrl + "\">" + resetUrl + "</a>";
            message.setContent(body, "text/html");
            ((JavaMailSenderImpl) mailSender).send(message);
        } catch (MessagingException e) {
            // simply log it and go on...
            this.logger.info(e.getMessage());
        }
    }
}
