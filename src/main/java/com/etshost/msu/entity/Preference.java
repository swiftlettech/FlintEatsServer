package com.etshost.msu.entity;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * Represents a {@link User}'s disposition to {@link UGC}, for use in tailoring the User's feed.
 */
@Audited
@javax.persistence.Entity
@Configurable
@Transactional
public class Preference extends Entity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User usr;

    private String target;

	@DateTimeFormat(style = "MM")
	private Instant endTime;
	
	private int value;


    // JavaBean.aj
    public User getUsr() {
        return this.usr;
    }
    
    public void setUsr(User usr) {
        this.usr = usr;
    }
    
    public String getTarget() {
        return this.target;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }
    
    public Instant getEndTime() {
        return this.endTime;
    }
    
    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }
    
    public int getValue() {
        return this.value;
    }
    
    public void setValue(int value) {
        this.value = value;
    }

	@JsonCreator
	public static Preference factory(@JsonProperty("target") String target, @JsonProperty("value") int value) {
		Preference preference = new Preference();
		preference.setTarget(target);
		preference.setValue(value);
		return preference;
	}
	
    public String toJson() {
        return new JSONSerializer()
        		.include("class", "target", "value", "created", "endTime", "usr.id")
        		.exclude("*")
        		.serialize(this);
    }
	
    public static String toJsonArrayPreference(Collection<Preference> collection) {
        return new JSONSerializer()
        		.include("class", "target", "value", "created", "endTime", "usr.id")
        		.exclude("*")
        		.serialize(collection);
    }
	
    public static TypedQuery<Preference> findPreferences(User usr, String target) {
        if (usr == null) throw new IllegalArgumentException("The usr argument is required");
        if (target == null) throw new IllegalArgumentException("The target argument is required");

        EntityManager em = Preference.entityManager();
        TypedQuery<Preference> q = em.createQuery("SELECT o FROM Preference AS o"
        		+ " WHERE o.usr = :usr AND o.target = :target", Preference.class);
        q.setParameter("usr", usr);
        q.setParameter("target", target);
        return q;
    }
    
    // pulled from Roo file due to bug [ROO-3570]
    public static List<Preference> findPreferenceEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Preference o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)
        		|| Entity.fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        if (maxResults < 0) {
            return entityManager().createQuery(jpaQuery, Preference.class)
            		.setFirstResult(firstResult).getResultList();
        }
        return entityManager().createQuery(jpaQuery, Preference.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }


    // Finder.aj
    public static Long countFindPreferencesByTargetLike(String target) {
        if (target == null || target.length() == 0) throw new IllegalArgumentException("The target argument is required");
        target = target.replace('*', '%');
        if (target.charAt(0) != '%') {
            target = "%" + target;
        }
        if (target.charAt(target.length() - 1) != '%') {
            target = target + "%";
        }
        EntityManager em = entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM Preference AS o WHERE LOWER(o.target) LIKE LOWER(:target)", Long.class);
        q.setParameter("target", target);
        return ((Long) q.getSingleResult());
    }
    
    public static Long countFindPreferencesByUsr(User usr) {
        if (usr == null) throw new IllegalArgumentException("The usr argument is required");
        EntityManager em = entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM Preference AS o WHERE o.usr = :usr", Long.class);
        q.setParameter("usr", usr);
        return ((Long) q.getSingleResult());
    }
    
    public static TypedQuery<Preference> findPreferencesByTargetLike(String target) {
        if (target == null || target.length() == 0) throw new IllegalArgumentException("The target argument is required");
        target = target.replace('*', '%');
        if (target.charAt(0) != '%') {
            target = "%" + target;
        }
        if (target.charAt(target.length() - 1) != '%') {
            target = target + "%";
        }
        EntityManager em = entityManager();
        TypedQuery<Preference> q = em.createQuery("SELECT o FROM Preference AS o WHERE LOWER(o.target) LIKE LOWER(:target)", Preference.class);
        q.setParameter("target", target);
        return q;
    }
    
    public static TypedQuery<Preference> findPreferencesByTargetLike(String target, String sortFieldName, String sortOrder) {
        if (target == null || target.length() == 0) throw new IllegalArgumentException("The target argument is required");
        target = target.replace('*', '%');
        if (target.charAt(0) != '%') {
            target = "%" + target;
        }
        if (target.charAt(target.length() - 1) != '%') {
            target = target + "%";
        }
        EntityManager em = entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Preference AS o WHERE LOWER(o.target) LIKE LOWER(:target)");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Preference> q = em.createQuery(queryBuilder.toString(), Preference.class);
        q.setParameter("target", target);
        return q;
    }
    
    public static TypedQuery<Preference> findPreferencesByUsr(User usr) {
        if (usr == null) throw new IllegalArgumentException("The usr argument is required");
        EntityManager em = entityManager();
        TypedQuery<Preference> q = em.createQuery("SELECT o FROM Preference AS o WHERE o.usr = :usr", Preference.class);
        q.setParameter("usr", usr);
        return q;
    }
    
    public static TypedQuery<Preference> findPreferencesByUsr(User usr, String sortFieldName, String sortOrder) {
        if (usr == null) throw new IllegalArgumentException("The usr argument is required");
        EntityManager em = entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Preference AS o WHERE o.usr = :usr");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Preference> q = em.createQuery(queryBuilder.toString(), Preference.class);
        q.setParameter("usr", usr);
        return q;
    }


    // Jpa_ActiveRecord.aj
    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("usr", "target", "endTime", "value");
    
    public static long countPreferences() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Preference o", Long.class).getSingleResult();
    }
    
    public static List<Preference> findAllPreferences() {
        return entityManager().createQuery("SELECT o FROM Preference o", Preference.class).getResultList();
    }
    
    public static List<Preference> findAllPreferences(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Preference o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Preference.class).getResultList();
    }
    
    public static Preference findPreference(Long id) {
        if (id == null) return null;
        return entityManager().find(Preference.class, id);
    }
    
    public static List<Preference> findPreferenceEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Preference o", Preference.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public Preference merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Preference merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }


    // Json.aj
    public static Preference fromJsonToPreference(String json) {
        return new JSONDeserializer<Preference>()
        .use(null, Preference.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection) {
        return new JSONSerializer()
        .exclude("*.class").serialize(collection);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(collection);
    }
    
    public static Collection<Preference> fromJsonArrayToPreferences(String json) {
        return new JSONDeserializer<List<Preference>>()
        .use("values", Preference.class).deserialize(json);
    }
    

    // ToString.aj
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
