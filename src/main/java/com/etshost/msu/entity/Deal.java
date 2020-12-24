package com.etshost.msu.entity;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;

import org.apache.lucene.search.Explanation;
import org.hibernate.envers.Audited;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.imgscalr.Scalr;
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
import flexjson.JSONSerializer;


/**
 * Represents a deal taking place at a {@link Market}.
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
@RooJpaActiveRecord(finders = { "findDealsByTextLike", "findDealsByMarket" })
public class Deal extends UGC {

    @ManyToOne
    private Market market;

    private Instant startDate;
    
    private Instant endDate;
    
	@JSON(include = false)
	private byte[] image;
    
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)	
    private String title;
    
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)	
    private String price;
    
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)	
    private String text;
    
	@JSON(include = false)
	public Market getTarget() {
    	return this.getMarket();
    }
    
    public void setTarget(Market market) {
    	this.setMarket(market);
    }
    
	@JsonCreator
	public static Deal factory(
			@JsonProperty("startDate") long startEpoch,
			@JsonProperty("endDate") long endEpoch,
			@JsonProperty("image") String image64,
			@JsonProperty("title") String title,
			@JsonProperty("price") String price,
			@JsonProperty("text") String text,
			@JsonProperty("market") Market market,
			@JsonProperty("tags") Set<Tag> tags) {
		Logger logger = LoggerFactory.getLogger(Deal.class);
		logger.debug("Deal factory. tags: {}", tags);
		Deal deal = new Deal();
		deal.setStartDate(Instant.ofEpochMilli(startEpoch));
		deal.setEndDate(Instant.ofEpochMilli(endEpoch));
		deal.setImageBase64(image64);
		deal.setTitle(title);
		deal.setPrice(price);
		deal.setText(text);
		deal.setMarket(market);
		deal.setTags(tags);
		return deal;
	}
	
    @JSON(include = false)
    public byte[] getImage() {
    	return this.image;
    }
    
    @JSON(include = false)
    public String getImageBase64() {
    	if (this.image == null) {
    		return null;
    	}
    	String image64 = Base64.getEncoder().encodeToString(this.image);
        return image64;
    }
    
    @JSON(name = "image64")
	public String getImageBase64Scaled() {
    	if (this.image == null) {
    		return null;
    	}
		final InputStream in = new ByteArrayInputStream(this.image);
		BufferedImage image;
		BufferedImage imageScaled;

		// read in the image
		try {
			image = ImageIO.read(in);
		} catch (final IOException e) {
			// TODO Auto-generated catch block. Come up with better return.
			e.printStackTrace();
			return null;
		}
		if (image == null) {
			return null;
		}
		
		imageScaled = Scalr.resize(image, 360);

		// write the image
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			ImageIO.write(imageScaled, "png", output);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// Come up with better return
			return null;
		}
    	String image64 = Base64.getEncoder().encodeToString(output.toByteArray());
		return image64;
	}
	
    public void setImageBase64(String image64) {
    	try {
    		byte[] image = Base64.getMimeDecoder().decode(image64);
            this.setImage(image);
    	} catch (Exception e) {
    		this.logger.error(e.toString());
    	}
    }
    
    public String toJson() {
        return new JSONSerializer()
				.include("tags.id", "tags.name")
        		.exclude("logger", "tags.*").serialize(this);
    }
    
    public static String toJsonArrayDeal(Collection<Deal> collection) {
        return new JSONSerializer()
        		.include("class", "market.id", "market.name", 
        				"usr.id", "usr.username", "usr.name", "usr.avatar64", "tags.id", "tags.name")
		        .exclude("*.class", "*.logger", "market.*", "tags.*")
		        .serialize(collection);
    }
    
    public static String generateDataTables(final int draw, final int start, final int length,
    		final String orderColumnName, final String orderDir) {
        long count = Deal.countDeals();
        List<Deal> uList;
        if (length == -1) {
            uList = Deal.findAllDeals(orderColumnName, orderDir);
        } else {
            uList = Deal.findDealEntries(start, length, orderColumnName, orderDir);
        }
        JsonArray data = new JsonArray();
        Iterator<Deal> i = uList.iterator();
        while (i.hasNext()) {
            Deal u = i.next();
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
        long count = Deal.countDeals();
        // make a set containing matching elements
        Set<Deal> qSet = new HashSet<Deal>();
        qSet.addAll(Deal.findDealsByTextLike(query).getResultList());
        List<Deal> qList = new ArrayList<Deal>();
        qList.addAll(qSet);
        Deal.sort(qList, orderColumnName, orderDir);
        Iterator<Deal> i;
        if (length == -1) {
            i = qList.iterator();
        } else {
            List<Deal> subList = qList.subList(start, Math.min(start + length, qList.size()));
            i = subList.iterator();
        }
        JsonArray data = new JsonArray();
        while (i.hasNext()) {
            Deal u = i.next();
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
    
    public static Long countLiveDealsByMarket(Market market) {
        if (market == null) throw new IllegalArgumentException("The market argument is required");
        EntityManager em = Deal.entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM Deal AS o"
        		+ " WHERE o.endDate < :endDate"
        		+ " AND o.market = :market", Long.class);
        q.setParameter("endDate", Instant.now());
        q.setParameter("market", market);
        return (Long) q.getSingleResult();
    }
    
    public static TypedQuery<Deal> findLiveDealsByMarket(Market market) {
        if (market == null) throw new IllegalArgumentException("The market argument is required");
        EntityManager em = Deal.entityManager();
        TypedQuery<Deal> q = em.createQuery("SELECT o FROM Deal AS o"
        		+ " WHERE o.endDate < :endDate"
        		+ " AND o.market = :market"
        		+ " ORDER BY o.endDate ASC", Deal.class);
        q.setParameter("endDate", Instant.now());
        q.setParameter("market", market);
        return q;
    }
    
    // pulled from Roo file due to bug [ROO-3570]
    public static List<Deal> findDealEntries(int firstResult, int maxResults,
    		String sortFieldName, String sortOrder) {
    	if (sortFieldName == null || sortOrder == null) {
    		return Deal.findDealEntries(firstResult, maxResults);
    	}
        String jpaQuery = "SELECT o FROM Deal o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)
        		|| Entity.fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        if (maxResults < 0) {
            return entityManager().createQuery(jpaQuery, Deal.class)
            		.setFirstResult(firstResult).getResultList();
        }
        return entityManager().createQuery(jpaQuery, Deal.class)
        		.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @SuppressWarnings("unchecked")
	public static List<Deal> search(String q) {
        Logger logger = LoggerFactory.getLogger(Deal.class);
        EntityManager em = Entity.entityManager();
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);

        logger.debug("searching Deals for: {}", q);

        QueryBuilder qb = fullTextEntityManager
                .getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Deal.class)
                .get();
        org.apache.lucene.search.Query luceneQuery = qb
                .keyword()
                .fuzzy()
                .withPrefixLength(3)
                .onFields("title", "text")
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
		    fullTextEntityManager.createFullTextQuery(luceneQuery, Deal.class);
        logger.debug("jpaQuery: {}", jpaQuery.toString());
        @SuppressWarnings("unchecked") List<Object[]> results = jpaQuery.getResultList();
//        for (Object[] result : results) {
//            Explanation e = (Explanation) result[1];
//            logger.debug(e.toString());
//        }


        // execute searc
        logger.debug("results size: {}", jpaQuery.getResultList().size());
        List<Deal> result = jpaQuery.getResultList();
        return result;
    }
}
