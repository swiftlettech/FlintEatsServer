package com.etshost.msu.entity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Indexed;
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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import flexjson.JSON;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * Represents a review of a {@link Market} or {@link Recipe}.
 */
@Analyzer(impl = org.apache.lucene.analysis.standard.StandardAnalyzer.class)
@Indexed
@Audited
@javax.persistence.Entity
@Configurable
@RooJavaBean
@RooJson
@RooToString
@Transactional
@RooJpaActiveRecord(finders = { "findReviewsByTextLike" })
public class Review extends UGC {

    @ManyToOne
    private Entity target;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "review")
    @JSON(name = "properties")
    private Set<ReviewProperty> properties = new HashSet<ReviewProperty>();

    private String text;
    
	@JSON(name = "rating")
	public Double getRating() {
		Double rating = averageValueOfPropertiesByReview(this);
		return rating;
	}
	
	@JsonCreator
	public static Review factory(
        @JsonProperty("id") Long id,
        @JsonProperty("targetId") Long targetId,
        @JsonProperty("text") String text,
        @JsonProperty("properties") Set<ReviewProperty> properties,
        @JsonProperty("tags") Set<Tag> tags
        ) {
		Review review = null;
		if (id != null) {
			review = Review.findReview(id);
			if (review == null) {
				return review;
			}
		} else {
			review = new Review();
		}
		if (text != null) {
			review.setText(text);
		}
		if (targetId != null) {
			review.setTarget(UGC.findEntity(targetId));
		}
        if (properties != null) {
            review.setProperties(properties);
        }
        if (tags != null) {
            review.setTags(tags);
        }
		return review;
	}

    public static String generateDataTables(final int draw, final int start, final int length,
    		final String orderColumnName, final String orderDir) {
        long count = Review.countReviews();
        List<Review> uList;
        if (length == -1) {
            uList = Review.findAllReviews(orderColumnName, orderDir);
        } else {
            uList = Review.findReviewEntries(start, length, orderColumnName, orderDir);
        }
        JsonArray data = new JsonArray();
        Iterator<Review> i = uList.iterator();
        while (i.hasNext()) {
            Review u = i.next();
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
        long count = Review.countReviews();
        // make a set containing matching elements
        Set<Review> qSet = new HashSet<Review>();
        qSet.addAll(Review.findReviewsByTextLike(query).getResultList());
        List<Review> qList = new ArrayList<Review>();
        qList.addAll(qSet);
        Review.sort(qList, orderColumnName, orderDir);
        Iterator<Review> i;
        if (length == -1) {
            i = qList.iterator();
        } else {
            List<Review> subList = qList.subList(start, Math.min(start + length, qList.size()));
            i = subList.iterator();
        }
        JsonArray data = new JsonArray();
        while (i.hasNext()) {
            Review u = i.next();
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
    
    @SuppressWarnings("unchecked")
	public static List<Review> search(String q) {
        Logger logger = LoggerFactory.getLogger(Review.class);
        EntityManager em = Entity.entityManager();
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);

        logger.debug("searching Reviews for: {}", q);

        QueryBuilder qb = fullTextEntityManager
                .getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Review.class)
                .get();
        org.apache.lucene.search.Query luceneQuery = qb
                .keyword()
                .fuzzy()
                .withPrefixLength(3)
                .onFields("title", "description")
                .matching(q)
                .createQuery();

        logger.debug("luceneQuery: {}", luceneQuery.toString());

        //the is for debugging only
//        org.hibernate.search.jpa.FullTextQuery jpaQuery =
//                fullTextEntityManager.createFullTextQuery(luceneQuery, Deal.class).setProjection(
//                        FullTextQuery.DOCUMENT_ID,
//                        FullTextQuery.EXPLANATION,
//                        FullTextQuery.THIS
//                );

        // wrap Lucene query in a javax.persistence.Query
		javax.persistence.Query jpaQuery =
		    fullTextEntityManager.createFullTextQuery(luceneQuery, Review.class);
        logger.debug("jpaQuery: {}", jpaQuery.toString());
//        @SuppressWarnings("unchecked") List<Object[]> results = jpaQuery.getResultList();
//        for (Object[] result : results) {
//            Explanation e = (Explanation) result[1];
//            logger.debug(e.toString());
//        }


        // execute searc
        logger.debug("results size: {}", jpaQuery.getResultList().size());
        List<Review> result = jpaQuery.getResultList();
        return result;
    }


    // pulled from Roo file due to bug [ROO-3570]
    public static List<Review> findReviewEntries(int firstResult, int maxResults,
    		String sortFieldName, String sortOrder) {
    	if (sortFieldName == null || sortOrder == null) {
    		return Review.findReviewEntries(firstResult, maxResults);
    	}
        String jpaQuery = "SELECT o FROM Review o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)
        		|| Entity.fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        if (maxResults < 0) {
            return entityManager().createQuery(jpaQuery, Review.class)
            		.setFirstResult(firstResult).getResultList();
        }
        return entityManager().createQuery(jpaQuery, Review.class)
        		.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }


    // JavaBean.aj
    public Entity getTarget() {
        return this.target;
    }
    
    public void setTarget(Entity target) {
        this.target = target;
    }
    
    public Set<ReviewProperty> getProperties() {
        return this.properties;
    }
    
    public void setProperties(Set<ReviewProperty> properties) {
        this.properties = properties;
    }
    
    public String getText() {
        return this.text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    

    // Json.aj
    public String toJson() {
        return new JSONSerializer()
            .include("class")
        	.exclude("logger", "*.logger", "target.image", "usr.email", "usr.phone").serialize(this);
    }
    
    public static Review fromJsonToReview(String json) {
        return new JSONDeserializer<Review>()
        .use(null, Review.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection) {
        return new JSONSerializer()
            .include("class")
            .exclude("*.logger", "usr", "target.image", "usr.email", "usr.phone")
            .serialize(collection);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude().serialize(collection);
    }
    
    public static Collection<Review> fromJsonArrayToReviews(String json) {
        return new JSONDeserializer<List<Review>>()
        .use("values", Review.class).deserialize(json);
    }
    

    // ToString.aj
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
    

    // Jpa_ActiveRecord.aj
    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("target", "properties", "text");
    
    public static long countReviews() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Review o", Long.class).getSingleResult();
    }
    
    public static List<Review> findAllReviews() {
        return entityManager().createQuery("SELECT o FROM Review o", Review.class).getResultList();
    }
    
    public static List<Review> findAllReviews(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Review o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Review.class).getResultList();
    }
    
    public static Review findReview(Long id) {
        if (id == null) return null;
        return entityManager().find(Review.class, id);
    }
    
    public static Double averageValueOfPropertiesByReview(Review review) {
        if (review == null) throw new IllegalArgumentException("The review argument is required");
        EntityManager em = Review.entityManager();
        TypedQuery<Double> q = em.createQuery("SELECT AVG(o.value) FROM ReviewProperty AS o"
        		+ " WHERE o.review = :review", Double.class);
        q.setParameter("review", review);
        return q.getSingleResult();
    }
    
    public static Double averageValueOfReviewsByTarget(Entity target) {
        if (target == null) throw new IllegalArgumentException("The target argument is required");
        EntityManager em = Review.entityManager();
        TypedQuery<Double> q = em.createQuery("SELECT AVG(rp.value) FROM Review r JOIN r.properties rp"
                + " WHERE r.target = :entity"
                + " GROUP BY r", Double.class);
        q.setParameter("entity", target);
        List<Double> averages = q.getResultList();
        return averages.stream().mapToDouble(a -> a).average().orElse(0);
    }
    
    public static List<Review> findReviewEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Review o", Review.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public Review merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Review merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    

    // Finder.aj
    public static Long countFindReviewsByTextLike(String text) {
        if (text == null || text.length() == 0) throw new IllegalArgumentException("The text argument is required");
        text = text.replace('*', '%');
        if (text.charAt(0) != '%') {
            text = "%" + text;
        }
        if (text.charAt(text.length() - 1) != '%') {
            text = text + "%";
        }
        EntityManager em = Review.entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM Review AS o WHERE LOWER(o.text) LIKE LOWER(:text)", Long.class);
        q.setParameter("text", text);
        return q.getSingleResult();
    }
    
    public static Long countReviewsByTarget(Entity target) {
        if (target == null) throw new IllegalArgumentException("The target argument is required");
        EntityManager em = Review.entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM Review AS o"
        		+ " WHERE o.target = :entity", Long.class);
        q.setParameter("entity", target);
        return q.getSingleResult();
    }
    
    public static TypedQuery<Review> findReviewsByTextLike(String text) {
        if (text == null || text.length() == 0) throw new IllegalArgumentException("The text argument is required");
        text = text.replace('*', '%');
        if (text.charAt(0) != '%') {
            text = "%" + text;
        }
        if (text.charAt(text.length() - 1) != '%') {
            text = text + "%";
        }
        EntityManager em = Review.entityManager();
        TypedQuery<Review> q = em.createQuery("SELECT o FROM Review AS o WHERE LOWER(o.text) LIKE LOWER(:text)", Review.class);
        q.setParameter("text", text);
        return q;
    }
    
    public static List<Review> findReviewsByTarget(Entity target) {
        if (target == null) throw new IllegalArgumentException("The target argument is required");
        EntityManager em = Review.entityManager();
        TypedQuery<Review> q = em.createQuery("SELECT o FROM Review o WHERE o.target = :ugc", Review.class);
        q.setParameter("ugc", target);
        return q.getResultList();
    }
    
    public static TypedQuery<Review> findReviewsByTextLike(String text, String sortFieldName, String sortOrder) {
        if (text == null || text.length() == 0) throw new IllegalArgumentException("The text argument is required");
        text = text.replace('*', '%');
        if (text.charAt(0) != '%') {
            text = "%" + text;
        }
        if (text.charAt(text.length() - 1) != '%') {
            text = text + "%";
        }
        EntityManager em = Review.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Review AS o WHERE LOWER(o.text) LIKE LOWER(:text)");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Review> q = em.createQuery(queryBuilder.toString(), Review.class);
        q.setParameter("text", text);
        return q;
    }
    

}
