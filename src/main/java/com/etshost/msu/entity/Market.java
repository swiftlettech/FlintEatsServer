package com.etshost.msu.entity;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import flexjson.JSON;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Represents a physical location selling {@link Food}s.
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
@RooJpaActiveRecord(finders = { "findMarketsByStatus", "findMarketsByNameLike" })
public class Market extends Entity {
	
	@JsonCreator
	public static Market factory(
			@JsonProperty("id") Long id,
			@JsonProperty("name") String name,
			@JsonProperty("email") String email,
			@JsonProperty("address") String address,
			@JsonProperty("hours") String hours,
			@JsonProperty("phone") String phone,
			@JsonProperty("url") String url,
			@JsonProperty("lat") Double lat,
			@JsonProperty("lng") Double lng,
			@JsonProperty("image") String image
			) {
		Market market = null;
		if (id != null) {
			market = Market.findMarket(id);
			if (market == null) {
				return market;
			}
		} else {
			market = new Market();
		}
		if (name != null) {
			market.setName(name);
		}
		if (email != null) {
			market.setEmail(email);
		}
		if (address != null) {
			market.setAddress(address);
		}
		if (hours != null) {
			market.setHours(hours);
		}
		if (phone != null) {
			market.setPhone(phone);
		}
		if (url != null) {
			market.setUrl(url);
		}
		if (lat != null
				&& lng != null) {
			market.setCoordinates(lat, lng);
		}
		if (image != null) {
			market.setImageBase64(image);
		}
		return market;
	}

	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)	
    private String name;

    private String email;

    private String address;

    private String hours;

    private String phone;

    private String url;
    
    private Double lat;
    
    private Double lng;
    
	private byte[] image;
	
	@JSON(name = "dealsCount")
	public Long getDealsCount() {
		Long count = Deal.countLiveDealsByMarket(this);
		return count;
	}
	
    @JSON(name = "image64")
    public String getImageBase64() {
    	if (this.image == null) {
    		return null;
    	}
    	String image64 = Base64.getEncoder().encodeToString(this.image);
        return image64;
    }

    // JavaBean.aj
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return this.email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAddress() {
        return this.address;
    }
    
    public String getHours() {
        return this.hours;
    }
    
    public void setHours(String hours) {
        this.hours = hours;
    }
    
    public String getPhone() {
        return this.phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public Double getLat() {
        return this.lat;
    }
    
    public void setLat(Double lat) {
        this.lat = lat;
    }
    
    public Double getLng() {
        return this.lng;
    }
    
    public void setLng(Double lng) {
        this.lng = lng;
    }
    
    public byte[] getImage() {
        return this.image;
    }
    
    public void setImage(byte[] image) {
        this.image = image;
    }



    // set lat/lng from Google when changing address
    public void setAddress(String addr) {
    	this.address = addr;
    	
		final Geocoder geocoder = new Geocoder();
		if (addr != null && !addr.equals("")) {
			GeocoderRequest geocoderRequest = new GeocoderRequestBuilder()
														.setAddress(addr)
														.setLanguage("en")
														.getGeocoderRequest();
			try {
				GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
				this.logger.debug(geocoderResponse.toString());
				BigDecimal lat = geocoderResponse.getResults().get(0)
										.getGeometry().getLocation().getLat();
				BigDecimal lng = geocoderResponse.getResults().get(0)
										.getGeometry().getLocation().getLng();
				this.setLat(lat.doubleValue());
				this.setLng(lng.doubleValue());

			} catch (Exception e) {
				this.logger.error("Geocoding failed! for " + this.getId(), e);
			}
		}
    }
    
    public void setCoordinates(Double lat, Double lng) {
    	this.lat = lat;
    	this.lng = lng;
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
        		.include("image64").exclude("logger").serialize(this);
    }
    
    public static String toJsonArrayMarket(Collection<Market> collection) {
        return new JSONSerializer()
        		.exclude("logger", "image64", "image", "created", "modified").serialize(collection);
    }

    public static String generateDataTables(final int draw, final int start, final int length,
    		final String orderColumnName, final String orderDir) {
        long count = Market.countMarkets();
        List<Market> uList;
        if (length == -1) {
            uList = Market.findAllMarkets(orderColumnName, orderDir);
        } else {
            uList = Market.findMarketEntries(start, length, orderColumnName, orderDir);
        }
        JsonArray data = new JsonArray();
        Iterator<Market> i = uList.iterator();
        while (i.hasNext()) {
            Market u = i.next();
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
        long count = Market.countMarkets();
        // make a set containing matching elements
        Set<Market> qSet = new HashSet<Market>();
        qSet.addAll(Market.findMarketsByNameLike(query).getResultList());
        List<Market> qList = new ArrayList<Market>();
        qList.addAll(qSet);
        Market.sort(qList, orderColumnName, orderDir);
        Iterator<Market> i;
        if (length == -1) {
            i = qList.iterator();
        } else {
            List<Market> subList = qList.subList(start, Math.min(start + length, qList.size()));
            i = subList.iterator();
        }
        JsonArray data = new JsonArray();
        while (i.hasNext()) {
            Market m = i.next();
            JsonArray mj = new JsonArray();
            mj.add(m.getId());
            mj.add(m.getCreated().toString());
            mj.add(m.getName());
            mj.add(m.getStatus().name());
            data.add(mj);
        }
        JsonObject obj = new JsonObject();
        obj.addProperty("draw", String.valueOf(draw));
        obj.addProperty("recordsTotal", String.valueOf(count));
        obj.addProperty("recordsFiltered", String.valueOf(qList.size()));
        obj.add("data", data);
        return obj.toString();
    }
    
    // pulled from Roo file due to bug [ROO-3570]
    public static List<Market> findMarketEntries(int firstResult, int maxResults,
    		String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Market o";
        if (maxResults < 0) {
            return entityManager().createQuery(jpaQuery, Market.class)
                    .setFirstResult(firstResult).getResultList();
        }
        if (sortFieldName == null || sortOrder == null) {
    		return Market.findMarketEntries(firstResult, maxResults);
    	}
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)
        		|| Entity.fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Market.class)
        		.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public static List<Market> findAllMarkets() {
        return entityManager()
            .createQuery("SELECT o FROM Market o", Market.class)
            .getResultList();
    }

    @Cacheable(value = "marketsCache")
    public String findAllMarketsJson() {
        List<Market> markets = entityManager()
        .createQuery("SELECT o FROM Market o", Market.class)
        .getResultList();
        markets.forEach((x) -> x.image = new byte[0]);
        return toJsonArrayMarket(markets);        
    }

    private void rebuildCache() {
        try {
            //CacheManager cacheManager = ehCacheManager.getObject();
            this.logger.debug("Market Cache: Refreshing");
            List<Object> keys = CacheManager.getInstance().getEhcache("marketsCache").getKeys();
            // CacheManager.getInstance().getEhcache("marketsCache").removeAll();
            Element el = new Element(keys.get(0), findAllMarketsJson());
            CacheManager.getInstance().getEhcache("marketsCache").replace(el);
            this.logger.debug("Market Cache: Finished Refreshing");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void backgroundRefreshMarketCache() {
        new Thread(new Runnable(){  
            @Override   
            public void run(){  
                rebuildCache();
            }   
          }).start();
    }

    // ToString.aj
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }


    // Json.aj
    public static Market fromJsonToMarket(String json) {
        return new JSONDeserializer<Market>()
        .use(null, Market.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection) {
        return new JSONSerializer()
        .exclude("*.class").serialize(collection);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(collection);
    }
    
    public static Collection<Market> fromJsonArrayToMarkets(String json) {
        return new JSONDeserializer<List<Market>>()
        .use("values", Market.class).deserialize(json);
    }


    // Jpa_ActiveRecord.aj
    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("name", "email", "address", "hours", "phone", "url", "lat", "lng", "image");
    
    public static long countMarkets() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Market o", Long.class).getSingleResult();
    }
    
    public static List<Market> findAllMarkets(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Market o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Market.class).getResultList();
    }
    
    public static Market findMarket(Long id) {
        if (id == null) return null;
        return entityManager().find(Market.class, id);
    }
    
    public static List<Market> findMarketEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Market o", Market.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public Market merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Market merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }


    // Finder.aj
    public static Long countFindMarketsByNameLike(String name) {
        if (name == null || name.length() == 0) throw new IllegalArgumentException("The name argument is required");
        name = name.replace('*', '%');
        if (name.charAt(0) != '%') {
            name = "%" + name;
        }
        if (name.charAt(name.length() - 1) != '%') {
            name = name + "%";
        }
        EntityManager em = entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM Market AS o WHERE LOWER(o.name) LIKE LOWER(:name)", Long.class);
        q.setParameter("name", name);
        return q.getSingleResult();
    }
    
    public static Long countFindMarketsByStatus(Status status) {
        if (status == null) throw new IllegalArgumentException("The status argument is required");
        EntityManager em = entityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(o) FROM Market AS o WHERE o.status = :status", Long.class);
        q.setParameter("status", status);
        return q.getSingleResult();
    }
    
    public static TypedQuery<Market> findMarketsByNameLike(String name) {
        if (name == null || name.length() == 0) throw new IllegalArgumentException("The name argument is required");
        name = name.replace('*', '%');
        if (name.charAt(0) != '%') {
            name = "%" + name;
        }
        if (name.charAt(name.length() - 1) != '%') {
            name = name + "%";
        }
        EntityManager em = entityManager();
        TypedQuery<Market> q = em.createQuery("SELECT o FROM Market AS o WHERE LOWER(o.name) LIKE LOWER(:name)", Market.class);
        q.setParameter("name", name);
        return q;
    }
    
    public static TypedQuery<Market> findMarketsByNameLike(String name, String sortFieldName, String sortOrder) {
        if (name == null || name.length() == 0) throw new IllegalArgumentException("The name argument is required");
        name = name.replace('*', '%');
        if (name.charAt(0) != '%') {
            name = "%" + name;
        }
        if (name.charAt(name.length() - 1) != '%') {
            name = name + "%";
        }
        EntityManager em = entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Market AS o WHERE LOWER(o.name) LIKE LOWER(:name)");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Market> q = em.createQuery(queryBuilder.toString(), Market.class);
        q.setParameter("name", name);
        return q;
    }
    
    public static TypedQuery<Market> findMarketsByStatus(Status status) {
        if (status == null) throw new IllegalArgumentException("The status argument is required");
        EntityManager em = entityManager();
        TypedQuery<Market> q = em.createQuery("SELECT o FROM Market AS o WHERE o.status = :status", Market.class);
        q.setParameter("status", status);
        return q;
    }
    
    public static TypedQuery<Market> findMarketsByStatus(Status status, String sortFieldName, String sortOrder) {
        if (status == null) throw new IllegalArgumentException("The status argument is required");
        EntityManager em = entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM Market AS o WHERE o.status = :status");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<Market> q = em.createQuery(queryBuilder.toString(), Market.class);
        q.setParameter("status", status);
        return q;
    }
    
}
