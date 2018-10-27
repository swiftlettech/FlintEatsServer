package com.etshost.msu.entity;

import java.time.Instant;
import java.util.List;

import javax.persistence.EmbeddedId;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
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
@RooJpaActiveRecord(finders = { "findReactionsByUsr", "findReactionsByTarget" })
public class Reaction {
	
	public Reaction() {
		this.setPk(new ReactionPk());
	}
	
	@JsonCreator
	public static Reaction factory(@JsonProperty("target") long target, @JsonProperty("value") int value) {
		Reaction reaction = new Reaction();
		UGC ugc = UGC.findUGC(target);
		reaction.setTarget(ugc);
		reaction.setValue(value);
		return reaction;
	}
	
	@EmbeddedId
	private ReactionPk pk;
	
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User usr;

    @ManyToOne
    @MapsId("targetId")
    @JoinColumn(name = "target_id")
    private UGC target;
    
//	@DateTimeFormat(style = "MM")
//	private Instant startTime;
	
	@DateTimeFormat(style = "MM")
	private Instant endTime;
	
	private int value;
	
	/**
	 * Returns a User's reactions to targeted UGC. Returns newest first for convenience.
	 * @param usr
	 * @param target
	 * @return
	 */
    public static TypedQuery<Reaction> findReactions(User usr, UGC target) {
        if (usr == null) throw new IllegalArgumentException("The usr argument is required");
        if (target == null) throw new IllegalArgumentException("The target argument is required");

        EntityManager em = Reaction.entityManager();
        TypedQuery<Reaction> q = em.createQuery("SELECT o FROM Reaction AS o "
        		+ "WHERE o.usr = :usr AND o.target = :target "
        		+ "ORDER BY o.pk.startTime DESC", Reaction.class);
        q.setParameter("usr", usr);
        q.setParameter("target", target);
        return q;
    }
    
    public static List<Reaction> findReactionEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Reaction o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        if (maxResults < 0) {
            return entityManager().createQuery(jpaQuery, Reaction.class)
            		.setFirstResult(firstResult).getResultList();
        }
        return entityManager().createQuery(jpaQuery, Reaction.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<Reaction> findRecentReactions(Instant when) {
        TypedQuery<Reaction> q = entityManager().createQuery("SELECT o FROM Reaction AS o "
        		+ "WHERE o.pk.startTime > :when", Reaction.class);
        q.setParameter("when", when);
        return q.getResultList();
    }
    
    public static Long countOpenReactionsByTarget(UGC target) {
        if (target == null) throw new IllegalArgumentException("The target argument is required");
        EntityManager em = Reaction.entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM Reaction AS o "
        		+ "WHERE o.target = :target AND o.endTime IS NULL) ", Long.class);
        q.setParameter("target", target);
        return ((Long) q.getSingleResult());
    }
}
