package com.etshost.msu.entity;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.springframework.beans.factory.annotation.Configurable;
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
import flexjson.JSONSerializer;

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
        		.include("image64").exclude("logger").serialize(collection);
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
    	if (sortFieldName == null || sortOrder == null) {
    		return Market.findMarketEntries(firstResult, maxResults);
    	}
        String jpaQuery = "SELECT o FROM Market o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)
        		|| Entity.fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        if (maxResults < 0) {
        	return entityManager().createQuery(jpaQuery, Market.class)
        			.setFirstResult(firstResult).getResultList();
        }
        return entityManager().createQuery(jpaQuery, Market.class)
        		.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
