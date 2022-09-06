package com.etshost.msu.entity;
import java.util.Collection;
import java.util.List;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * A property of a {@link Review}.
 * @author kschemmel
 *
 */
@Audited
@javax.persistence.Entity
@Configurable
@RooJavaBean
@RooJpaActiveRecord
@RooJson
@RooToString
@Transactional
public class ReviewProperty extends Entity {
	
	enum PropertyType {
		ACCESSIBILITY,
		CLEANLINESS,
		FRIENDLINESS,
		SAFETY,
		SELECTION
	}
	
    @ManyToOne
    private Review review;
    
	@Enumerated(EnumType.STRING)
    private PropertyType propertyType;
    
    private int value;


	// JavaBean.aj
    public Review getReview() {
        return this.review;
    }
    
    public void setReview(Review review) {
        this.review = review;
    }
    
    public PropertyType getPropertyType() {
        return this.propertyType;
    }
    
    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }
    
    public int getValue() {
        return this.value;
    }
    
    public void setValue(int value) {
        this.value = value;
    }
    

	// ToString.aj
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
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
    
    public static ReviewProperty fromJsonToReviewProperty(String json) {
        return new JSONDeserializer<ReviewProperty>()
        .use(null, ReviewProperty.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection) {
        return new JSONSerializer()
        .exclude("*.class").serialize(collection);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(collection);
    }
    
    public static Collection<ReviewProperty> fromJsonArrayToReviewPropertys(String json) {
        return new JSONDeserializer<List<ReviewProperty>>()
        .use("values", ReviewProperty.class).deserialize(json);
    }
    

	// Jpa_ActiveRecord.aj
    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("review", "propertyType", "value");
    
    public static long countReviewPropertys() {
        return entityManager().createQuery("SELECT COUNT(o) FROM ReviewProperty o", Long.class).getSingleResult();
    }
    
    public static List<ReviewProperty> findAllReviewPropertys() {
        return entityManager().createQuery("SELECT o FROM ReviewProperty o", ReviewProperty.class).getResultList();
    }
    
    public static List<ReviewProperty> findAllReviewPropertys(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ReviewProperty o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, ReviewProperty.class).getResultList();
    }
    
    public static ReviewProperty findReviewProperty(Long id) {
        if (id == null) return null;
        return entityManager().find(ReviewProperty.class, id);
    }
    
    public static List<ReviewProperty> findReviewPropertyEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM ReviewProperty o", ReviewProperty.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<ReviewProperty> findReviewPropertyEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM ReviewProperty o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, ReviewProperty.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public ReviewProperty merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        ReviewProperty merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}
