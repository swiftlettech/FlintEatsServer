package com.etshost.msu.entity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.ManyToMany;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import flexjson.JSONSerializer;

/**
 * A string attached to an {@link Entity} for searching purposes.
 */
@Analyzer(impl = org.apache.lucene.analysis.standard.StandardAnalyzer.class)
@Audited
@javax.persistence.Entity
@Configurable
@Indexed
@RooJavaBean
@RooJson
@RooToString
@Transactional
@RooJpaActiveRecord(finders = { "findTagsByNameEquals", "findTagsByNameLike" })
public class Tag extends UGC {
	
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)	
	String name;
	
//	@IndexedEmbedded
    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "tags")
    private Set<Entity> targets = new HashSet<Entity>();

	@JsonCreator
	public static Tag factory(String name) {
		Logger logger = LoggerFactory.getLogger(Tag.class);
		logger.debug("Tag factory. tag name: {}", name);
		Tag tag = null;
		try {
			tag = Tag.findTagsByNameEquals(name).getSingleResult();
		} catch (NoResultException e) {
			tag = new Tag();
			tag.setUsr(User.getLoggedInUser());
			tag.setName(name.toLowerCase());
			tag.persist();
		}
		return tag;
	}
	
	public static Set<Tag> fetchTags(Set<String> tagNames) {
		Set<Tag> tags = new HashSet<Tag>();
		tagNames.forEach(tagName -> {
			Tag tag = null;
			try {
				tag = Tag.findTagsByNameEquals(tagName).getSingleResult();
			} catch (NoResultException e) {
				tag = new Tag();
				tag.setUsr(User.getLoggedInUser());
				tag.setName(tagName.toLowerCase());
				tag.persist();
			}
			tags.add(tag);
		});
		return tags;
	}
    
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Id: ").append(this.getId()).append(", ");
		sb.append("Name: ").append(this.getName()).append(", ");
		sb.append("Status: ").append(this.getStatus());
		return sb.toString();
	}
    
    public String toJson() {
        return new JSONSerializer()
        		.exclude("logger").serialize(this);
    }
    
    public static String toJsonArrayTag(Collection<Tag> collection) {
        return new JSONSerializer()
        		.include("class", "name", "usr.id", "usr.name")
		        .exclude("*.class", "*.logger", "usr")
		        .serialize(collection);
    }
    
    public static String generateDataTables(final int draw, final int start, final int length,
    		final String orderColumnName, final String orderDir) {
        long count = Tag.countTags();
        List<Tag> uList;
        if (length == -1) {
            uList = Tag.findAllTags(orderColumnName, orderDir);
        } else {
            uList = Tag.findTagEntries(start, length, orderColumnName, orderDir);
        }
        JsonArray data = new JsonArray();
        Iterator<Tag> i = uList.iterator();
        while (i.hasNext()) {
            Tag u = i.next();
            JsonArray uj = new JsonArray();
            uj.add(u.getId());
            uj.add(u.getCreated().toString());
            uj.add(u.getName());
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
        long count = Tag.countTags();
        // make a set containing matching elements
        Set<Tag> qSet = new HashSet<Tag>();
        qSet.addAll(Tag.findTagsByNameLike(query).getResultList());
        List<Tag> qList = new ArrayList<Tag>();
        qList.addAll(qSet);
        Tag.sort(qList, orderColumnName, orderDir);
        Iterator<Tag> i;
        if (length == -1) {
            i = qList.iterator();
        } else {
            List<Tag> subList = qList.subList(start, Math.min(start + length, qList.size()));
            i = subList.iterator();
        }
        JsonArray data = new JsonArray();
        while (i.hasNext()) {
            Tag u = i.next();
            JsonArray uj = new JsonArray();
            uj.add(u.getId());
            uj.add(u.getCreated().toString());
            uj.add(u.getName());
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
    public static List<Tag> findTagEntries(int firstResult, int maxResults,
    		String sortFieldName, String sortOrder) {
    	if (sortFieldName == null || sortOrder == null) {
    		return Tag.findTagEntries(firstResult, maxResults);
    	}
        String jpaQuery = "SELECT o FROM Tag o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)
        		|| Entity.fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        if (maxResults < 0) {
            return entityManager().createQuery(jpaQuery, Tag.class)
            		.setFirstResult(firstResult).getResultList();
        }
        return entityManager().createQuery(jpaQuery, Tag.class)
        		.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static TypedQuery<Tag> findTagsByNameEquals(String name) {
        if (name == null || name.length() == 0) throw new IllegalArgumentException("The name argument is required");
        EntityManager em = Tag.entityManager();
        TypedQuery<Tag> q = em.createQuery("SELECT o FROM Tag AS o WHERE LOWER(o.name) = LOWER(:name)", Tag.class);
        q.setParameter("name", name);
        return q;
    }
    
    public static TypedQuery<Tag> findTagsByPrefix(String prefix) {
        if (prefix == null || prefix.length() == 0) throw new IllegalArgumentException("The prefix argument is required");
        if (prefix.charAt(prefix.length() - 1) != '%') {
            prefix = prefix + "%";
        }
        EntityManager em = Tag.entityManager();
        TypedQuery<Tag> q = em.createQuery("SELECT o FROM Tag AS o WHERE LOWER(o.name) LIKE LOWER(:prefix)", Tag.class);
        q.setParameter("prefix", prefix);
        return q;
    }
    
    @SuppressWarnings("unchecked")
	public static List<Tag> search(String q) {
    	Logger logger = LoggerFactory.getLogger(Tag.class);
		EntityManager em = Entity.entityManager();
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);

		logger.debug("searching Tags for: {}", q);

		QueryBuilder qb = fullTextEntityManager
				.getSearchFactory()
				.buildQueryBuilder()
				.forEntity(Tag.class)
				.get();
		org.apache.lucene.search.Query luceneQuery = qb
			    .keyword()
			    .fuzzy()
			    .onField("name")
				.matching(q)
				.createQuery();
		
//		logger.debug("luceneQuery: {}", luceneQuery.toString());


		// wrap Lucene query in a javax.persistence.Query
		javax.persistence.Query jpaQuery =
		    fullTextEntityManager.createFullTextQuery(luceneQuery, Tag.class);
//		logger.debug("jpaQuery: {}", jpaQuery.toString());
		

		// execute search
		logger.debug("results size: {}", jpaQuery.getResultList().size());
		List<?> result = jpaQuery.getResultList();
		return (List<Tag>)result;
	}
}
