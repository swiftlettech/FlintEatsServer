package com.etshost.msu.entity;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;

import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;

import com.fasterxml.jackson.annotation.JsonCreator;

import flexjson.JSON;
import flexjson.JSONSerializer;

/**
 * Represents {@link User} generated content.
 */
@Audited
@Configurable
@javax.persistence.Entity
@Inheritance(strategy = InheritanceType.JOINED)
@RooJavaBean
@RooJson
@RooToString
@RooJpaActiveRecord(finders = { "findUGCSByUsr" })
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
        		.include("class", "market.id", "market.name", "usr.id", "usr.username", "usr.avatar64","tags.name","tags.id")
		        .exclude("*.class", "*.logger", "market.*", "usr.*","tags.*")
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
}
