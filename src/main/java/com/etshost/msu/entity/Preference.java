package com.etshost.msu.entity;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;

import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import flexjson.JSONSerializer;

/**
 * Represents a {@link User}'s disposition to {@link UGC}, for use in tailoring the User's feed.
 */
@Audited
@javax.persistence.Entity
@Configurable
@RooJavaBean
@RooJson
@RooToString
@Transactional
@RooJpaActiveRecord(finders = { "findPreferencesByUsr", "findPreferencesByTargetLike" })
public class Preference extends Entity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User usr;

    private String target;

	@DateTimeFormat(style = "MM")
	private Instant endTime;
	
	private int value;
	
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
}
