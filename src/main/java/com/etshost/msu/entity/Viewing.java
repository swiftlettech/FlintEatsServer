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

/**
 * Represents an instance of a {@link User} visiting a page of an {@link Entity}.
 */
@Audited
@Configurable
@javax.persistence.Entity
@RooJavaBean
@RooJpaActiveRecord(finders = { "findViewingsByUsr", "findViewingsByTarget" })
@RooJson
@RooToString
@Transactional
public class Viewing {
	
	@EmbeddedId
	private ViewingPk pk;
	
	public Viewing(User user, Entity target) {
		this.pk = new ViewingPk();
		this.usr = user;
		this.target = target;
	}
	
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User usr;

    @ManyToOne
    @MapsId("targetId")
    @JoinColumn(name = "target_id")
    private Entity target;
    
//	@DateTimeFormat(style = "MM")
//	private Instant startTime;
	
	@DateTimeFormat(style = "MM")
	private Instant endTime;

    public static List<Viewing> findRecentViewings(Instant when) {
        TypedQuery<Viewing> q = entityManager().createQuery("SELECT o FROM Viewing AS o "
        		+ "WHERE o.pk.startTime > :when", Viewing.class);
        q.setParameter("when", when);
        return q.getResultList();
    }
    
	/**
	 * Returns a User's reactions to targeted UGC. Returns newest first for convenience.
	 * @param usr
	 * @param target
	 * @return
	 */
    public static TypedQuery<Viewing> findViewings(User usr, Entity target) {
        if (usr == null) throw new IllegalArgumentException("The usr argument is required");
        if (target == null) throw new IllegalArgumentException("The target argument is required");

        EntityManager em = Viewing.entityManager();
        TypedQuery<Viewing> q = em.createQuery("SELECT o FROM Viewing AS o "
        		+ "WHERE o.usr = :usr AND o.target = :target "
        		+ "ORDER BY o.pk.startTime DESC", Viewing.class);
        q.setParameter("usr", usr);
        q.setParameter("target", target);
        return q;
    }
    
    public static List<Viewing> findViewingEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Viewing o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        if (maxResults < 0) {
            return entityManager().createQuery(jpaQuery, Viewing.class)
            		.setFirstResult(firstResult).getResultList();
        }
        return entityManager().createQuery(jpaQuery, Viewing.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
