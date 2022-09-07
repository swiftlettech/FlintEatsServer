package com.etshost.msu.entity;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonCreator;

import flexjson.JSON;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * Represents {@link User} generated content.
 */
@Audited
@Configurable
@javax.persistence.Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Transactional
public abstract class UGC extends Entity {
	
	@JsonCreator
	public static UGC factory(long id) {
		return UGC.findUGC(id);
	}

    @ManyToOne
    private User usr;

	@JSON(name = "reactionCount")
	public Long getReactionCount() {
		return Reaction.countOpenReactionsByTarget(this);
	}
	
	@JSON(name = "iLike")
	public boolean getILike() {
		User me = null;
		try {
			me = User.getLoggedInUser();
		} catch (Exception e) { 
			return false; 
		}
		if (me == null) {
			return false;
		}
		List<Reaction> reactions = Reaction.findReactions(me, this).getResultList();
		if (reactions.isEmpty()) {

			return false;
		}
		Reaction lastReaction = reactions.get(0);
		if (lastReaction.getEndTime() == null) {
			// if last reaction has not ended, user still likes it
			return true;
		}
		return false;
	}
    
    public static String toJsonArrayUGC(Collection<? extends UGC> collection) {
        return new JSONSerializer()
        		.include("class", "market.id", "market.name","tags.name","tags.id")
		        .exclude("*.logger", "market.*", "usr.email", "usr.phone","tags.*")
		        .serialize(collection);
    }
    
    public static Long countFeedUGCs() {
    	TypedQuery<Long> q = entityManager().createQuery("SELECT COUNT(o) FROM UGC o "
        		+ "WHERE TYPE(o) <> :tag "
        		+ "AND TYPE(o) <> :comment", Long.class);
        q.setParameter("tag", Tag.class);
        q.setParameter("comment", Comment.class);
        return q.getSingleResult();
    }
    
    public static List<UGC> findAllFeedUGCs() {
    	TypedQuery<UGC> q = entityManager().createQuery("SELECT o FROM UGC o "
        		+ "WHERE TYPE(o) <> :tag "
        		+ "AND TYPE(o) <> :comment "
        		+ "ORDER BY o.created DESC", UGC.class);
        q.setParameter("tag", Tag.class);
        q.setParameter("comment", Comment.class);
        return q.getResultList();
    }
    
    public static List<UGC> findAllFeedUGCs(int start, int length) {
    	TypedQuery<UGC> q = entityManager().createQuery("SELECT o FROM UGC o "
        		+ "WHERE TYPE(o) <> :tag "
        		+ "AND TYPE(o) <> :comment "
        		+ "ORDER BY o.created DESC", UGC.class);
        q.setParameter("tag", Tag.class);
        q.setParameter("comment", Comment.class);
        return q.setFirstResult(start).setMaxResults(length).getResultList();
    }
    
    public static List<UGC> findAllFeedUGCs(User user) {
    	TypedQuery<UGC> q = entityManager().createQuery("SELECT o FROM UGC o "
        		+ "WHERE o.usr = :user "
        		+ "AND TYPE(o) <> :tag "
        		+ "AND TYPE(o) <> :comment "
        		+ "ORDER BY o.created DESC", UGC.class);
    	q.setParameter("user", user);
        q.setParameter("tag", Tag.class);
        q.setParameter("comment", Comment.class);
        return q.getResultList();
    }
    
    public static List<UGC> findRecentUGCs(Instant when) {
    	TypedQuery<UGC> q = entityManager().createQuery("SELECT o FROM UGC o "
        		+ "WHERE o.modified > :when", UGC.class);
        q.setParameter("when", when);
        return q.getResultList();
    }

	// JavaBean.aj
	public User getUsr() {
        return this.usr;
    }
    
    public void setUsr(User usr) {
        this.usr = usr;
    }

	// ToString.aj
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	// Json.aj
    public String toJson() {
        return new JSONSerializer()
        .exclude("usr.email", "usr.phone").serialize(this);
    }
    
    public String toJson(String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("usr.email", "usr.phone").serialize(this);
    }
    
    public static UGC fromJsonToUGC(String json) {
        return new JSONDeserializer<UGC>()
        .use(null, UGC.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection) {
        return new JSONSerializer()
        .exclude("usr.email", "usr.phone").serialize(collection);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("usr.email", "usr.phone").serialize(collection);
    }
    
    public static Collection<UGC> fromJsonArrayToUGCS(String json) {
        return new JSONDeserializer<List<UGC>>()
        .use("values", UGC.class).deserialize(json);
    }

	// Jpa_ActiveRecord.aj
    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("usr");
    
    public static long countUGCS() {
        return entityManager().createQuery("SELECT COUNT(o) FROM UGC o", Long.class).getSingleResult();
    }
    
    public static List<UGC> findAllUGCS() {
        return entityManager().createQuery("SELECT o FROM UGC o", UGC.class).getResultList();
    }
    
    public static List<UGC> findAllUGCS(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM UGC o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, UGC.class).getResultList();
    }
    
    public static UGC findUGC(Long id) {
        if (id == null) return null;
        return entityManager().find(UGC.class, id);
    }
    
    public static List<UGC> findUGCEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM UGC o", UGC.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<UGC> findUGCEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM UGC o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, UGC.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public UGC merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        UGC merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }


	// Finder.aj
    public static Long countFindUGCSByUsr(User usr) {
        if (usr == null) throw new IllegalArgumentException("The usr argument is required");
        EntityManager em = entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM UGC AS o WHERE o.usr = :usr", Long.class);
        q.setParameter("usr", usr);
        return q.getSingleResult();
    }
    
    public static TypedQuery<UGC> findUGCSByUsr(User usr) {
        if (usr == null) throw new IllegalArgumentException("The usr argument is required");
        EntityManager em = entityManager();
        TypedQuery<UGC> q = em.createQuery("SELECT o FROM UGC AS o WHERE o.usr = :usr", UGC.class);
        q.setParameter("usr", usr);
        return q;
    }
    
    public static TypedQuery<UGC> findUGCSByUsr(User usr, String sortFieldName, String sortOrder) {
        if (usr == null) throw new IllegalArgumentException("The usr argument is required");
        EntityManager em = entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM UGC AS o WHERE o.usr = :usr");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<UGC> q = em.createQuery(queryBuilder.toString(), UGC.class);
        q.setParameter("usr", usr);
        return q;
    }

}
