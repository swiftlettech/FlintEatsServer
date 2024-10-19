package com.etshost.msu.entity;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.Version;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.envers.Audited;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * Represents an instance of a {@link User} visiting a page of an {@link Entity}.
 */
@Audited
@Configurable
@javax.persistence.Entity
@Transactional
public class Viewing {
    @Transient
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public Viewing(Long userId, long targetId) {
        this.setUserId(userId);
		this.setTargetId(targetId);
        this.setStartTime(Instant.now());
        this.logger.debug("Viewing created: {} {}", targetId, userId);
	}

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
	
    private Long user_id;

    private long target_id;
    
	@DateTimeFormat(style = "MM")
    @Column(name = "starttime")
    private Instant startTime;
    
	@DateTimeFormat(style = "MM")
    @Column(name = "endtime")
	private Instant endTime;

    public static List<Viewing> findRecentViewings(Instant when) {
        TypedQuery<Viewing> q = entityManager().createQuery("SELECT o FROM Viewing AS o "
        		+ "WHERE o.startTime > :when", Viewing.class);
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

        EntityManager em = entityManager();
        TypedQuery<Viewing> q = em.createQuery("SELECT o FROM Viewing AS o "
        		+ "WHERE o.usr = :usr AND o.target_id = :target "
        		+ "ORDER BY o.startTime DESC", Viewing.class);
        q.setParameter("usr", usr);
        q.setParameter("target", target.getId());
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


    // JavaBean.aj
    public long getId() {
        return this.id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return this.user_id;
    }
    
    public void setUserId(Long user_id) {
        this.user_id = user_id;
    }
    
    public long getTargetId() {
        return this.target_id;
    }
    
    public void setTargetId(long target_id) {
        this.target_id = target_id;
    }
    
    public Instant getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }
    
    public Instant getEndTime() {
        return this.endTime;
    }
    
    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
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
    
    public static Viewing fromJsonToViewing(String json) {
        return new JSONDeserializer<Viewing>()
        .use(null, Viewing.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<Viewing> collection) {
        return new JSONSerializer()
        .exclude("*.class").serialize(collection);
    }
    
    public static String toJsonArray(Collection<Viewing> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(collection);
    }
    
    public static Collection<Viewing> fromJsonArrayToViewings(String json) {
        return new JSONDeserializer<List<Viewing>>()
        .use("values", Viewing.class).deserialize(json);
    }


    // Jpa_ActiveRecord.aj
    @PersistenceContext
    transient EntityManager entityManager;
    
    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("id", "usr", "target", "endTime");
    
    public static final EntityManager entityManager() {
        EntityManager em = Entity.entityManager();
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long countViewings() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Viewing o", Long.class).getSingleResult();
    }
    
    public static List<Viewing> findAllViewings() {
        return entityManager().createQuery("SELECT o FROM Viewing o", Viewing.class).getResultList();
    }
    
    public static List<Viewing> findAllViewings(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Viewing o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Viewing.class).getResultList();
    }
    
    public static Viewing findViewing(long id) {
        return entityManager().find(Viewing.class, id);
    }
    
    public static List<Viewing> findViewingEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Viewing o", Viewing.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            Viewing attached = findViewing(this.id);
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
    public Viewing merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Viewing merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }


    // Finder.aj
    public static Long countFindViewingsByTarget(Entity target) {
        if (target == null) throw new IllegalArgumentException("The target argument is required");
        EntityManager em = entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM Viewing AS o WHERE o.target_id = :target", Long.class);
        q.setParameter("target", target.getId());
        return q.getSingleResult();
    }
    
    public static Long countFindViewingsByUsr(User usr) {
        if (usr == null) throw new IllegalArgumentException("The usr argument is required");
        EntityManager em = entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM Viewing AS o WHERE o.usr = :usr", Long.class);
        q.setParameter("usr", usr);
        return q.getSingleResult();
    }
    
    public static TypedQuery<Viewing> findViewingsByTarget(Entity target) {
        if (target == null) throw new IllegalArgumentException("The target argument is required");
        EntityManager em = entityManager();
        TypedQuery<Viewing> q = em.createQuery("SELECT o FROM Viewing AS o WHERE o.target_id = :target", Viewing.class);
        q.setParameter("target", target.getId());
        return q;
    }
    
    public static TypedQuery<Viewing> findViewingsByTarget(Entity target, String sortFieldName, String sortOrder) {
        if (target == null) throw new IllegalArgumentException("The target argument is required");
        EntityManager em = entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Viewing AS o WHERE o.target_id = :target");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Viewing> q = em.createQuery(queryBuilder.toString(), Viewing.class);
        q.setParameter("target", target.getId());
        return q;
    }
    
    public static TypedQuery<Viewing> findViewingsByUsr(User usr) {
        if (usr == null) throw new IllegalArgumentException("The usr argument is required");
        EntityManager em = entityManager();
        TypedQuery<Viewing> q = em.createQuery("SELECT o FROM Viewing AS o WHERE o.usr = :usr", Viewing.class);
        q.setParameter("usr", usr);
        return q;
    }
    
    public static TypedQuery<Viewing> findViewingsByUsr(User usr, String sortFieldName, String sortOrder) {
        if (usr == null) throw new IllegalArgumentException("The usr argument is required");
        EntityManager em = entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Viewing AS o WHERE o.usr = :usr");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Viewing> q = em.createQuery(queryBuilder.toString(), Viewing.class);
        q.setParameter("usr", usr);
        return q;
    }
}
