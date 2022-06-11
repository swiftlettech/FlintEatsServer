package com.etshost.msu.entity;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvNumber;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@Analyzer(impl = org.apache.lucene.analysis.standard.StandardAnalyzer.class)
@Audited
@javax.persistence.Entity
@Configurable
@Indexed
//@RooJavaBean
//@RooJson
//@RooToString
@Transactional
public class FoodPantrySite extends Entity {

	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
    @CsvBindByName(column = "Name", required = true)
    private String name;

    @CsvBindByName(column = "Address", required = true)
    private String address;

    @CsvBindByName(column = "Phone_1")
    private String phone;
    
    @CsvBindByName(column = "Schedule")
    private String schedule;

    @CsvBindByName(column = "Additional")
    private String notes;

    @CsvBindByName(column = "Y", required = true)
    @CsvNumber("#.##########")
    private Double lat;
    
    @CsvBindByName(column = "X", required = true)
    @CsvNumber("#.##########")
    private Double lng;


    // Setters and Getters
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return this.address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getSchedule() {
        return this.schedule;
    }
    
    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }
    
    public String getNotes() {
        return this.notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getPhone() {
        return this.phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
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

    public void setCoordinates(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
    }

	@JsonCreator
	public static FoodPantrySite factory(
        @JsonProperty("id") Long id,
        @JsonProperty("name") String name,
        @JsonProperty("address") String address,
        @JsonProperty("phone") String phone,
        @JsonProperty("schedule") String schedule,
        @JsonProperty("notes") String notes,
        @JsonProperty("lat") Double lat,
        @JsonProperty("lng") Double lng
    ) {
        FoodPantrySite foodPantrySite = null;
        if (id != null) {
            foodPantrySite = FoodPantrySite.findFoodPantrySite(id);
            if (foodPantrySite == null) {
                return foodPantrySite;
            }
        } else {
            foodPantrySite = new FoodPantrySite();
        }
        if (name != null) {
            foodPantrySite.setName(name);
        }
        if (address != null) {
            foodPantrySite.setAddress(address);
        }
        if (schedule != null) {
            foodPantrySite.setSchedule(schedule);
        }
        if (notes != null) {
            foodPantrySite.setNotes(notes);
        }
        if (phone != null) {
            foodPantrySite.setPhone(phone);
        }
        if (lat != null
                && lng != null) {
            foodPantrySite.setCoordinates(lat, lng);
        }
        return foodPantrySite;
    }

    // To String
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
    

    // Active Record
    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("name", "address", "phone", "lat", "lng");
    
    public static long countFoodPantrySites() {
        return entityManager().createQuery("SELECT COUNT(o) FROM FoodPantrySite o", Long.class).getSingleResult();
    }
    
    public static List<FoodPantrySite> findAllFoodPantrySites() {
        return entityManager().createQuery("SELECT o FROM FoodPantrySite o", FoodPantrySite.class).getResultList();
    }
    
    public static List<FoodPantrySite> findAllFoodPantrySites(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM FoodPantrySite o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, FoodPantrySite.class).getResultList();
    }
    
    public static FoodPantrySite findFoodPantrySite(Long id) {
        if (id == null) return null;
        return entityManager().find(FoodPantrySite.class, id);
    }
    
    public static List<FoodPantrySite> findFoodPantrySiteEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM FoodPantrySite o", FoodPantrySite.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public FoodPantrySite merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        FoodPantrySite merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

    @Transactional
    public static List<FoodPantrySite> replaceFoodPantrySiteEntries(List<FoodPantrySite> sites) {
        Logger logger = LoggerFactory.getLogger(FoodPantrySite.class);
        List<FoodPantrySite> existing = findAllFoodPantrySites();
        existing.forEach((site) -> {
            logger.debug(site.toString());
            site.delete();
            //site.persist();
        });
        sites.forEach((site) -> {
            logger.debug(site.toString());
            site.persist();
        });
        return findAllFoodPantrySites();
    }


    // Finders
    public static List<FoodPantrySite> findFoodPantrySiteEntries(int firstResult, int maxResults,
    		String sortFieldName, String sortOrder) {
        Logger logger = LoggerFactory.getLogger(FoodPantrySite.class);
        String jpaQuery = "SELECT o FROM FoodPantrySite o";
        if (maxResults < 0) {
        	return entityManager().createQuery(jpaQuery, FoodPantrySite.class)
                .setFirstResult(firstResult).getResultList();
        }
        if (sortFieldName == null || sortOrder == null) {
    		return FoodPantrySite.findFoodPantrySiteEntries(firstResult, maxResults);
    	}
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)
        		|| Entity.fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, FoodPantrySite.class)
            .setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    

    // JSON
    public static FoodPantrySite fromJsonToFoodPantrySite(String json) {
        return new JSONDeserializer<FoodPantrySite>()
        .use(null, FoodPantrySite.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection) {
        return new JSONSerializer()
        .include("name", "address", "phone", "schedule", "notes", "lat", "lng")
        .exclude("*.class", "*.logger").serialize(collection);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(collection);
    }
    
    public static Collection<FoodPantrySite> fromJsonArrayToFoodPantrySites(String json) {
        return new JSONDeserializer<List<FoodPantrySite>>()
        .use("values", FoodPantrySite.class).deserialize(json);
    }

}
