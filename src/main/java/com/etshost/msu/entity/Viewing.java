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

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

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
	
	public Viewing() {
		this.setPk(new ViewingPk());
	}
	
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
    
	@DateTimeFormat(style = "MM")
    @Column(name = "endtime")
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

        EntityManager em = entityManager();
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
    public ViewingPk getPk() {
        return this.pk;
    }
    
    public void setPk(ViewingPk pk) {
        this.pk = pk;
    }
    
    public User getUsr() {
        return this.usr;
    }
    
    public void setUsr(User usr) {
        this.usr = usr;
    }
    
    public Entity getTarget() {
        return this.target;
    }
    
    public void setTarget(Entity target) {
        this.target = target;
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
    
    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("pk", "usr", "target", "endTime");
    
    public static final EntityManager entityManager() {
        EntityManager em = new Viewing().entityManager;
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
    
    public static Viewing findViewing(ViewingPk pk) {
        if (pk == null) return null;
        return entityManager().find(Viewing.class, pk);
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
            Viewing attached = findViewing(this.pk);
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
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM Viewing AS o WHERE o.target = :target", Long.class);
        q.setParameter("target", target);
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
        TypedQuery<Viewing> q = em.createQuery("SELECT o FROM Viewing AS o WHERE o.target = :target", Viewing.class);
        q.setParameter("target", target);
        return q;
    }
    
    public static TypedQuery<Viewing> findViewingsByTarget(Entity target, String sortFieldName, String sortOrder) {
        if (target == null) throw new IllegalArgumentException("The target argument is required");
        EntityManager em = entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Viewing AS o WHERE o.target = :target");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Viewing> q = em.createQuery(queryBuilder.toString(), Viewing.class);
        q.setParameter("target", target);
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
