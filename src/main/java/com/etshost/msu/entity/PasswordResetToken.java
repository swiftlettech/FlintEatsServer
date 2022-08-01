package com.etshost.msu.entity;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.transaction.annotation.Transactional;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * A token for facilitating secure handling of {@link User} password reset requests.
 */
@javax.persistence.Entity
@RooJavaBean
@RooJpaActiveRecord(finders = { "findPasswordResetTokensByTokenEquals" })
@RooJson
@Configurable
public class PasswordResetToken extends Entity {

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_fk")
    private User usr;

    @Column(name = "expiry_date")
	@DateTimeFormat(style = "MM")
    private Instant expiryDate;

    // JavaBean.aj
    public String getToken() {
        return this.token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public User getUsr() {
        return this.usr;
    }
    
    public void setUsr(User usr) {
        this.usr = usr;
    }


    public Instant getExpiryDate() {
        return this.expiryDate;
    }
    
    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }



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

    // Json.aj
    public String toJson() {
        return new JSONSerializer()
        .exclude("*.class").serialize(this);
    }
    
    public String toJson(String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(this);
    }
    
    public static PasswordResetToken fromJsonToPasswordResetToken(String json) {
        return new JSONDeserializer<PasswordResetToken>()
        .use(null, PasswordResetToken.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection) {
        return new JSONSerializer()
        .exclude("*.class").serialize(collection);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(collection);
    }
    
    public static Collection<PasswordResetToken> fromJsonArrayToPasswordResetTokens(String json) {
        return new JSONDeserializer<List<PasswordResetToken>>()
        .use("values", PasswordResetToken.class).deserialize(json);
    }


    // Jpa_ActiveRecord.aj
    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("token", "usr", "expiryDate");
    
    public static long countPasswordResetTokens() {
        return entityManager().createQuery("SELECT COUNT(o) FROM PasswordResetToken o", Long.class).getSingleResult();
    }
    
    public static List<PasswordResetToken> findAllPasswordResetTokens() {
        return entityManager().createQuery("SELECT o FROM PasswordResetToken o", PasswordResetToken.class).getResultList();
    }
    
    public static List<PasswordResetToken> findAllPasswordResetTokens(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PasswordResetToken o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, PasswordResetToken.class).getResultList();
    }
    
    public static PasswordResetToken findPasswordResetToken(Long id) {
        if (id == null) return null;
        return entityManager().find(PasswordResetToken.class, id);
    }
    
    public static List<PasswordResetToken> findPasswordResetTokenEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM PasswordResetToken o", PasswordResetToken.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<PasswordResetToken> findPasswordResetTokenEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PasswordResetToken o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, PasswordResetToken.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public PasswordResetToken merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        PasswordResetToken merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }


    // Finder.aj
    public static Long countFindPasswordResetTokensByTokenEquals(String token) {
        if (token == null || token.length() == 0) throw new IllegalArgumentException("The token argument is required");
        EntityManager em = entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM PasswordResetToken AS o WHERE o.token = :token", Long.class);
        q.setParameter("token", token);
        return q.getSingleResult();
    }
    
    public static TypedQuery<PasswordResetToken> findPasswordResetTokensByTokenEquals(String token) {
        if (token == null || token.length() == 0) throw new IllegalArgumentException("The token argument is required");
        EntityManager em = entityManager();
        TypedQuery<PasswordResetToken> q = em.createQuery("SELECT o FROM PasswordResetToken AS o WHERE o.token = :token", PasswordResetToken.class);
        q.setParameter("token", token);
        return q;
    }
    
    public static TypedQuery<PasswordResetToken> findPasswordResetTokensByTokenEquals(String token, String sortFieldName, String sortOrder) {
        if (token == null || token.length() == 0) throw new IllegalArgumentException("The token argument is required");
        EntityManager em = entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PasswordResetToken AS o WHERE o.token = :token");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<PasswordResetToken> q = em.createQuery(queryBuilder.toString(), PasswordResetToken.class);
        q.setParameter("token", token);
        return q;
    }

}
