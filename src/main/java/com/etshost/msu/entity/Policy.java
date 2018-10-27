package com.etshost.msu.entity;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;

import org.hibernate.envers.Audited;
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

import flexjson.JSONSerializer;

@Audited
@javax.persistence.Entity
@Configurable
@RooJavaBean
@RooJson
@RooToString
@Transactional
@RooJpaActiveRecord(finders = { "findPolicysByNameEquals" })
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
}
