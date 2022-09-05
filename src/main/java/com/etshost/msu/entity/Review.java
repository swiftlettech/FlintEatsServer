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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
    private Set<ReviewProperty> properties = new HashSet<ReviewProperty>();

    private String text;
    
    public String toJson() {
        return new JSONSerializer()
        		.exclude("logger").serialize(this);
    }
    
    public static String toJsonArrayReview(Collection<Review> collection) {
        return new JSONSerializer()
        		.include("class", "usr.id", "usr.name", "usr.avatar")
		        .exclude("*.class", "*.logger", "usr")
		        .serialize(collection);
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
}
