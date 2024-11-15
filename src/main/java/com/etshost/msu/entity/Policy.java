package com.etshost.msu.entity;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@Audited
@javax.persistence.Entity
@Configurable
@Transactional
public class Policy extends Entity {
	
	public Policy(String name) {
		this.name = name;
	}
	
	@JsonCreator
	public static Policy factory(
			@JsonProperty("name") String name,
			@JsonProperty("displayName") String displayName,
			@JsonProperty("text") String text) {
		if (name == null) {
			return null;
		}
				
		Policy policy = Policy.findPolicy(name);
    	
		if (displayName != null) {
			policy.setDisplayName(displayName);
		}
		
		if (text != null) {
			policy.setText(text);
		}

		return policy;
	}

    private String name;
    
    private String displayName;

    @Column(columnDefinition = "text")
    private String text;
	

    // Jpa_Entity.aj
    // public new() {
    //     super();
    // }


    // JavaBean.aj
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDisplayName() {
        return this.displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getText() {
        return this.text;
    }
    
    public void setText(String text) {
        this.text = text;
    }

    // ToString.aj
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
    
    public String toJson() {
        return new JSONSerializer()
        		.include("name", "displayName", "text")
        		.exclude("logger")
        		.serialize(this);
    }
    
    public static String toJsonArrayPolicy(Collection<Policy> collection) {
        return new JSONSerializer()
        		.include("*")
        		.exclude("logger")
        		.serialize(collection);
    }

    public static Policy findPolicy(String name) {
    	try {
    		Policy policy = Policy.findPolicysByNameEquals(name).getSingleResult();
    		return policy;
    	} catch (Exception e) {
    		return new Policy(name);
    	}
    }
    
    public static String generateDataTables(final int draw, final int start, final int length,
    		final String orderColumnName, final String orderDir) {
        long count = Policy.countPolicys();
        List<Policy> uList;
        if (length == -1) {
            uList = Policy.findAllPolicys(orderColumnName, orderDir);
        } else {
            uList = Policy.findPolicyEntries(start, length, orderColumnName, orderDir);
        }
        JsonArray data = new JsonArray();
        Iterator<Policy> i = uList.iterator();
        while (i.hasNext()) {
            Policy u = i.next();
            JsonArray uj = new JsonArray();
            uj.add(u.getName());
            uj.add(u.getDisplayName());
            data.add(uj);
        }
        JsonObject obj = new JsonObject();
        obj.addProperty("draw", String.valueOf(draw));
        obj.addProperty("recordsTotal", String.valueOf(count));
        obj.addProperty("recordsFiltered", String.valueOf(count));
        obj.add("data", data);
        return obj.toString();
    }

    // Finder.aj
    public static Long countFindPolicysByNameEquals(String name) {
        if (name == null || name.length() == 0) throw new IllegalArgumentException("The name argument is required");
        EntityManager em = entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM Policy AS o WHERE o.name = :name", Long.class);
        q.setParameter("name", name);
        return ((Long) q.getSingleResult());
    }
    
    public static TypedQuery<Policy> findPolicysByNameEquals(String name) {
        if (name == null || name.length() == 0) throw new IllegalArgumentException("The name argument is required");
        EntityManager em = entityManager();
        TypedQuery<Policy> q = em.createQuery("SELECT o FROM Policy AS o WHERE o.name = :name", Policy.class);
        q.setParameter("name", name);
        return q;
    }
    
    public static TypedQuery<Policy> findPolicysByNameEquals(String name, String sortFieldName, String sortOrder) {
        if (name == null || name.length() == 0) throw new IllegalArgumentException("The name argument is required");
        EntityManager em = entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Policy AS o WHERE o.name = :name");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Policy> q = em.createQuery(queryBuilder.toString(), Policy.class);
        q.setParameter("name", name);
        return q;
    }


    // Jpa_ActiveRecord.aj
    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("name", "displayName", "text");
    
    public static long countPolicys() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Policy o", Long.class).getSingleResult();
    }
    
    public static List<Policy> findAllPolicys() {
        return entityManager().createQuery("SELECT o FROM Policy o", Policy.class).getResultList();
    }
    
    public static List<Policy> findAllPolicys(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Policy o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Policy.class).getResultList();
    }
    
    public static Policy findPolicy(Long id) {
        if (id == null) return null;
        return entityManager().find(Policy.class, id);
    }
    
    public static List<Policy> findPolicyEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Policy o", Policy.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<Policy> findPolicyEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Policy o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Policy.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public Policy merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Policy merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }


    // Json.aj
    public static Policy fromJsonToPolicy(String json) {
        return new JSONDeserializer<Policy>()
        .use(null, Policy.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection) {
        return new JSONSerializer()
        .exclude("*.class").serialize(collection);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(collection);
    }
    
    public static Collection<Policy> fromJsonArrayToPolicys(String json) {
        return new JSONDeserializer<List<Policy>>()
        .use("values", Policy.class).deserialize(json);
    }
}
