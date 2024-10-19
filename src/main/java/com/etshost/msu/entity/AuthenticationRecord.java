package com.etshost.msu.entity;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * Keeps track of wrong password attempts made by a user or IP Address.
 * Implementation allows for ease of account locking and IP tracking.
 */
@Audited
@Configurable
@javax.persistence.Entity
public class AuthenticationRecord extends Entity {

    public AuthenticationRecord() {
        super();
    }

    public AuthenticationRecord(final String ipAddress, final User user) {
        super();
        this.ipAddress = ipAddress;
        this.failedUser = user;
    }

    @ManyToOne
    private User failedUser;

    private int failureCount;

    private String ipAddress;

    public void incrementFailureCount() {
        this.failureCount++;
    }

    public void resetFailureCount() {
        this.failureCount = 0;
    }

    // JavaBean.aj
    public User getFailedUser() {
        return this.failedUser;
    }
    
    public void setFailedUser(User failedUser) {
        this.failedUser = failedUser;
    }
    
    public int getFailureCount() {
        return this.failureCount;
    }
    
    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }
    
    public String getIpAddress() {
        return this.ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }


    // ToString.aj
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }


    // Jpa_ActiveRecord.aj
    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("failedUser", "failureCount", "ipAddress");
    
    public static long countAuthenticationRecords() {
        return entityManager().createQuery("SELECT COUNT(o) FROM AuthenticationRecord o", Long.class).getSingleResult();
    }
    
    public static List<AuthenticationRecord> findAllAuthenticationRecords() {
        return entityManager().createQuery("SELECT o FROM AuthenticationRecord o", AuthenticationRecord.class).getResultList();
    }
    
    public static List<AuthenticationRecord> findAllAuthenticationRecords(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM AuthenticationRecord o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, AuthenticationRecord.class).getResultList();
    }
    
    public static AuthenticationRecord findAuthenticationRecord(Long id) {
        if (id == null) return null;
        return entityManager().find(AuthenticationRecord.class, id);
    }
    
    public static List<AuthenticationRecord> findAuthenticationRecordEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM AuthenticationRecord o", AuthenticationRecord.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<AuthenticationRecord> findAuthenticationRecordEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM AuthenticationRecord o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, AuthenticationRecord.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public AuthenticationRecord merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        AuthenticationRecord merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }


    // Finder.aj
    public static Long countFindAuthenticationRecordsByIpAddressEquals(String ipAddress) {
        if (ipAddress == null || ipAddress.length() == 0) throw new IllegalArgumentException("The ipAddress argument is required");
        EntityManager em = entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM AuthenticationRecord AS o WHERE o.ipAddress = :ipAddress", Long.class);
        q.setParameter("ipAddress", ipAddress);
        return ((Long) q.getSingleResult());
    }
    
    public static TypedQuery<AuthenticationRecord> findAuthenticationRecordsByIpAddressEquals(String ipAddress) {
        if (ipAddress == null || ipAddress.length() == 0) throw new IllegalArgumentException("The ipAddress argument is required");
        EntityManager em = entityManager();
        TypedQuery<AuthenticationRecord> q = em.createQuery("SELECT o FROM AuthenticationRecord AS o WHERE o.ipAddress = :ipAddress", AuthenticationRecord.class);
        q.setParameter("ipAddress", ipAddress);
        return q;
    }
    
    public static TypedQuery<AuthenticationRecord> findAuthenticationRecordsByIpAddressEquals(String ipAddress, String sortFieldName, String sortOrder) {
        if (ipAddress == null || ipAddress.length() == 0) throw new IllegalArgumentException("The ipAddress argument is required");
        EntityManager em = entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM AuthenticationRecord AS o WHERE o.ipAddress = :ipAddress");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<AuthenticationRecord> q = em.createQuery(queryBuilder.toString(), AuthenticationRecord.class);
        q.setParameter("ipAddress", ipAddress);
        return q;
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
    
    public static AuthenticationRecord fromJsonToAuthenticationRecord(String json) {
        return new JSONDeserializer<AuthenticationRecord>()
        .use(null, AuthenticationRecord.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection) {
        return new JSONSerializer()
        .exclude("*.class").serialize(collection);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(collection);
    }
    
    public static Collection<AuthenticationRecord> fromJsonArrayToAuthenticationRecords(String json) {
        return new JSONDeserializer<List<AuthenticationRecord>>()
        .use("values", AuthenticationRecord.class).deserialize(json);
    }
}
