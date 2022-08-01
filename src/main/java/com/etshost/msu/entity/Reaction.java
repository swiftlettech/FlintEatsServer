package com.etshost.msu.entity;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.Version;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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

import flexjson.JSONDeserializer;
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
	
    // Jpa_Entity.aj
    @Version
    @Column(name = "version")
    private Integer version;
    
    public Integer getVersion() {
        return this.version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }


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

    // JavaBean.aj
    public ReactionPk getPk() {
        return this.pk;
    }
    
    public void setPk(ReactionPk pk) {
        this.pk = pk;
    }
    
    public User getUsr() {
        return this.usr;
    }
    
    public void setUsr(User usr) {
        this.usr = usr;
    }
    
    public UGC getTarget() {
        return this.target;
    }
    
    public void setTarget(UGC target) {
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
    
    public static Reaction fromJsonToReaction(String json) {
        return new JSONDeserializer<Reaction>()
        .use(null, Reaction.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<Reaction> collection) {
        return new JSONSerializer()
        .exclude("*.class").serialize(collection);
    }
    
    public static String toJsonArray(Collection<Reaction> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(collection);
    }
    
    public static Collection<Reaction> fromJsonArrayToReactions(String json) {
        return new JSONDeserializer<List<Reaction>>()
        .use("values", Reaction.class).deserialize(json);
    }


    // Jpa_ActiveRecord.aj
    @PersistenceContext
    transient EntityManager entityManager;
    
    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("pk", "usr", "target", "endTime", "value");
    
    public static final EntityManager entityManager() {
        EntityManager em = new Reaction().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long countReactions() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Reaction o", Long.class).getSingleResult();
    }
    
    public static List<Reaction> findAllReactions() {
        return entityManager().createQuery("SELECT o FROM Reaction o", Reaction.class).getResultList();
    }
    
    public static List<Reaction> findAllReactions(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Reaction o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Reaction.class).getResultList();
    }
    
    public static Reaction findReaction(ReactionPk pk) {
        if (pk == null) return null;
        return entityManager().find(Reaction.class, pk);
    }
    
    public static List<Reaction> findReactionEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Reaction o", Reaction.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public void persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            Reaction attached = Reaction.findReaction(this.pk);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public Reaction merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Reaction merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }


    // Finder.aj
    public static Long countFindReactionsByTarget(UGC target) {
        if (target == null) throw new IllegalArgumentException("The target argument is required");
        EntityManager em = entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM Reaction AS o WHERE o.target = :target", Long.class);
        q.setParameter("target", target);
        return q.getSingleResult();
    }
    
    public static Long countFindReactionsByUsr(User usr) {
        if (usr == null) throw new IllegalArgumentException("The usr argument is required");
        EntityManager em = entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM Reaction AS o WHERE o.usr = :usr", Long.class);
        q.setParameter("usr", usr);
        return q.getSingleResult();
    }
    
    public static TypedQuery<Reaction> findReactionsByTarget(UGC target) {
        if (target == null) throw new IllegalArgumentException("The target argument is required");
        EntityManager em = entityManager();
        TypedQuery<Reaction> q = em.createQuery("SELECT o FROM Reaction AS o WHERE o.target = :target", Reaction.class);
        q.setParameter("target", target);
        return q;
    }
    
    public static TypedQuery<Reaction> findReactionsByTarget(UGC target, String sortFieldName, String sortOrder) {
        if (target == null) throw new IllegalArgumentException("The target argument is required");
        EntityManager em = entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Reaction AS o WHERE o.target = :target");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Reaction> q = em.createQuery(queryBuilder.toString(), Reaction.class);
        q.setParameter("target", target);
        return q;
    }
    
    public static TypedQuery<Reaction> findReactionsByUsr(User usr) {
        if (usr == null) throw new IllegalArgumentException("The usr argument is required");
        EntityManager em = entityManager();
        TypedQuery<Reaction> q = em.createQuery("SELECT o FROM Reaction AS o WHERE o.usr = :usr", Reaction.class);
        q.setParameter("usr", usr);
        return q;
    }
    
    public static TypedQuery<Reaction> findReactionsByUsr(User usr, String sortFieldName, String sortOrder) {
        if (usr == null) throw new IllegalArgumentException("The usr argument is required");
        EntityManager em = entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Reaction AS o WHERE o.usr = :usr");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Reaction> q = em.createQuery(queryBuilder.toString(), Reaction.class);
        q.setParameter("usr", usr);
        return q;
    }
}
