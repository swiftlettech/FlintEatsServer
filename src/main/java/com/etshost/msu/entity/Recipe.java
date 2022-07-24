package com.etshost.msu.entity;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;

import flexjson.JSON;

/**
 * Represents a recipe for a culinary dish.
 */
@Audited
@javax.persistence.Entity
@Configurable
@RooJavaBean
@RooJpaActiveRecord
@RooJson
@RooToString
@Transactional
public class Recipe extends UGC {
    
    private String title;

    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
    private String description;

    @JSON(include = false)
    private byte[] image;
	
    private int servings;

    private boolean published;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<RecipeStep> steps = new HashSet<RecipeStep>();

	@JsonCreator
	public static Recipe factory(
        @JsonProperty("id") Long id,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("image") String image,
        @JsonProperty("servings") Integer servings,
        @JsonProperty("published") Boolean published,
        @JsonProperty("tags") Set<Tag> tags
        ) {
		Recipe recipe = null;
		if (id != null) {
			recipe = Recipe.findRecipe(id);
			if (recipe == null) {
				return recipe;
			}
		} else {
			recipe = new Recipe();
		}
		if (title != null) {
			recipe.setTitle(title);
		}
		if (description != null) {
			recipe.setDescription(description);
		}
		if (image != null) {
			recipe.setImageBase64(image);
		}
		if (servings != null) {
			recipe.setServings(servings);
		}
		if (published != null) {
			recipe.setPublished(published);
		}
        if (tags != null) {
            recipe.setTags(tags);
        }
		return recipe;
	}


    // JavaBean.aj
    public int getServings() {
        return this.servings;
    }
    
    public void setServings(int servings) {
        this.servings = servings;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getImage() {
        return this.image;
    }
    
    public void setImage(byte[] image) {
        this.image = image;
    }
    
    @JSON(name = "image64")
    public String getHeaderImageBase64() {
    	if (this.image == null) {
    		return null;
    	}
    	String image64 = Base64.getEncoder().encodeToString(this.image);
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

    public boolean getPublished() {
        return this.published;
    }
    
    public void setPublished(Boolean published) {
        this.published = published;
    }

    // Jpa_ActiveRecord.aj
    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("title","servings");
    
    public static long countRecipes() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Recipe o", Long.class).getSingleResult();
    }
    
    public static List<Recipe> findAllRecipes() {
        return entityManager().createQuery("SELECT o FROM Recipe o", Recipe.class).getResultList();
    }
    
    public static List<Recipe> findAllRecipes(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Recipe o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Recipe.class).getResultList();
    }
    
    public static Recipe findRecipe(Long id) {
        if (id == null) return null;
        return entityManager().find(Recipe.class, id);
    }
    
    public static List<Recipe> findRecipeEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Recipe o", Recipe.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<Recipe> findRecipeEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Recipe o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Recipe.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public Recipe merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Recipe merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

    // ToString.aj
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    // Json.aj
    public String toJson() {
        return new JSONSerializer()
        .exclude("*.class").serialize(this);
    }
    
    public String toJson(String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(this);
    }
    
    public static Recipe fromJsonToRecipe(String json) {
        return new JSONDeserializer<Recipe>()
        .use(null, Recipe.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection) {
        return new JSONSerializer()
        .exclude("*.class").serialize(collection);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class").serialize(collection);
    }
    
    public static Collection<Recipe> fromJsonArrayToRecipes(String json) {
        return new JSONDeserializer<List<Recipe>>()
        .use("values", Recipe.class).deserialize(json);
    }
    
}
