package com.etshost.msu.entity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * Represents a comment a {@link User} leaves on an {@link Entity} (usually {@link UGC}).
 * @author kschemmel
 *
 */
@Audited
@javax.persistence.Entity
@Configurable
@Transactional
public class Comment extends UGC {
    
    @ManyToOne
    private Entity target;

    private String text;

    
    // JavaBean.aj
    public Entity getTarget() {
        return this.target;
    }
    
    public void setTarget(Entity target) {
        this.target = target;
    }
    
    public String getText() {
        return this.text;
    }
    
    public void setText(String text) {
        this.text = text;
    }

    // Json.aj
    public static Comment fromJsonToComment(String json) {
        return new JSONDeserializer<Comment>()
        .use(null, Comment.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection) {
        return new JSONSerializer()
        .exclude("*.class", "usr.email", "usr.phone").serialize(collection);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class", "usr.email", "usr.phone").serialize(collection);
    }
    
    public static Collection<Comment> fromJsonArrayToComments(String json) {
        return new JSONDeserializer<List<Comment>>()
        .use("values", Comment.class).deserialize(json);
    }


    public String toJson() {
        return new JSONSerializer()
        		.exclude("logger", "usr.email", "usr.phone").serialize(this);
    }
    
    public static String toJsonArrayComment(Collection<Comment> collection) {
        return new JSONSerializer()
        		.include("class")
		        .exclude("*.class", "*.logger", "usr.email", "usr.phone")
		        .serialize(collection);
    }
    
    public static String generateDataTables(final int draw, final int start, final int length,
    		final String orderColumnName, final String orderDir) {
        long count = Comment.countComments();
        List<Comment> uList;
        if (length == -1) {
            uList = Comment.findAllComments(orderColumnName, orderDir);
        } else {
            uList = Comment.findCommentEntries(start, length, orderColumnName, orderDir);
        }
        JsonArray data = new JsonArray();
        Iterator<Comment> i = uList.iterator();
        while (i.hasNext()) {
            Comment u = i.next();
            JsonArray uj = new JsonArray();
            uj.add(u.getId());
            uj.add(u.getCreated().toString());
            uj.add(u.getTarget().getId());
            uj.add(u.getStatus().name());
            data.add(uj);
        }
        JsonObject obj = new JsonObject();
        obj.addProperty("draw", String.valueOf(draw));
        obj.addProperty("recordsTotal", String.valueOf(count));
        obj.addProperty("recordsFiltered", String.valueOf(count));
        obj.add("data", data);
        return obj.toString();
    }

    public static String generateDataTables(final int draw, final int start, final int length,
    		final String orderColumnName, final String orderDir, final String query) {
        // if no query string, don't filter
        if (query == null || query.equals("")) {
            return generateDataTables(draw, start, length, orderColumnName, orderDir);
        }
        long count = Comment.countComments();
        // make a set containing matching elements
        Set<Comment> qSet = new HashSet<Comment>();
        qSet.addAll(Comment.findCommentsByTextLike(query).getResultList());
        List<Comment> qList = new ArrayList<Comment>();
        qList.addAll(qSet);
        Comment.sort(qList, orderColumnName, orderDir);
        Iterator<Comment> i;
        if (length == -1) {
            i = qList.iterator();
        } else {
            List<Comment> subList = qList.subList(start, Math.min(start + length, qList.size()));
            i = subList.iterator();
        }
        JsonArray data = new JsonArray();
        while (i.hasNext()) {
            Comment u = i.next();
            JsonArray uj = new JsonArray();
            uj.add(u.getId());
            uj.add(u.getCreated().toString());
            uj.add(u.getTarget().getId());
            uj.add(u.getStatus().name());
            data.add(uj);
        }
        JsonObject obj = new JsonObject();
        obj.addProperty("draw", String.valueOf(draw));
        obj.addProperty("recordsTotal", String.valueOf(count));
        obj.addProperty("recordsFiltered", String.valueOf(qList.size()));
        obj.add("data", data);
        return obj.toString();
    }
    
    // pulled from Roo file due to bug [ROO-3570]
    public static List<Comment> findCommentEntries(int firstResult, int maxResults,
    		String sortFieldName, String sortOrder) {
    	if (sortFieldName == null || sortOrder == null) {
    		return Comment.findCommentEntries(firstResult, maxResults);
    	}
        String jpaQuery = "SELECT o FROM Comment o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)
        		|| Entity.fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        if (maxResults < 0) {
            return entityManager().createQuery(jpaQuery, Comment.class)
            		.setFirstResult(firstResult).getResultList();
        }
        return entityManager().createQuery(jpaQuery, Comment.class)
        		.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    // ToString.aj
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }


    // Jpa_ActiveRecord.aj
    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("target", "text");
    
    public static long countComments() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Comment o", Long.class).getSingleResult();
    }
    
    public static List<Comment> findAllComments() {
        return entityManager().createQuery("SELECT o FROM Comment o", Comment.class).getResultList();
    }
    
    public static List<Comment> findAllComments(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Comment o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Comment.class).getResultList();
    }
    
    public static Comment findComment(Long id) {
        if (id == null) return null;
        return entityManager().find(Comment.class, id);
    }
    
    public static List<Comment> findCommentEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Comment o", Comment.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public Comment merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Comment merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }


    // Finder.aj
    public static Long countFindCommentsByTextLike(String text) {
        if (text == null || text.length() == 0) throw new IllegalArgumentException("The text argument is required");
        text = text.replace('*', '%');
        if (text.charAt(0) != '%') {
            text = "%" + text;
        }
        if (text.charAt(text.length() - 1) != '%') {
            text = text + "%";
        }
        EntityManager em = entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM Comment AS o WHERE LOWER(o.text) LIKE LOWER(:text)", Long.class);
        q.setParameter("text", text);
        return q.getSingleResult();
    }
    
    public static TypedQuery<Comment> findCommentsByTextLike(String text) {
        if (text == null || text.length() == 0) throw new IllegalArgumentException("The text argument is required");
        text = text.replace('*', '%');
        if (text.charAt(0) != '%') {
            text = "%" + text;
        }
        if (text.charAt(text.length() - 1) != '%') {
            text = text + "%";
        }
        EntityManager em = entityManager();
        TypedQuery<Comment> q = em.createQuery("SELECT o FROM Comment AS o WHERE LOWER(o.text) LIKE LOWER(:text)", Comment.class);
        q.setParameter("text", text);
        return q;
    }
    
    public static TypedQuery<Comment> findCommentsByTextLike(String text, String sortFieldName, String sortOrder) {
        if (text == null || text.length() == 0) throw new IllegalArgumentException("The text argument is required");
        text = text.replace('*', '%');
        if (text.charAt(0) != '%') {
            text = "%" + text;
        }
        if (text.charAt(text.length() - 1) != '%') {
            text = text + "%";
        }
        EntityManager em = entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Comment AS o WHERE LOWER(o.text) LIKE LOWER(:text)");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Comment> q = em.createQuery(queryBuilder.toString(), Comment.class);
        q.setParameter("text", text);
        return q;
    }
}
