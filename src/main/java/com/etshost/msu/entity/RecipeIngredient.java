package com.etshost.msu.entity;
import java.util.Collection;
import java.util.List;

import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import flexjson.JSON;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@Audited
@javax.persistence.Entity
@Configurable
@Transactional
public class RecipeIngredient extends Entity {

    @ManyToOne
    @JSON(include = false)
    private Recipe recipe;

    @Size(min = 3, max = 255)
    private String name;

    @Size(max = 45)
    private String measurement;

	@JsonCreator
	public static RecipeIngredient factory(
        @JsonProperty("id") Long id,
        @JsonProperty("recipe") Recipe recipe,
        @JsonProperty("name") String name,
        @JsonProperty("measurement") String measurement
        ) {
		RecipeIngredient recipeIngredient = null;
		if (id != null) {
			recipeIngredient = RecipeIngredient.findRecipeIngredient(id);
			if (recipeIngredient == null) {
				return recipeIngredient;
			}
		} else {
			recipeIngredient = new RecipeIngredient();
		}
        if (recipe != null) {
            recipeIngredient.setParent(recipe);
        }
		if (name != null) {
			recipeIngredient.setName(name);
		}
		if (measurement != null) {
			recipeIngredient.setMeasurement(measurement);
		}
		return recipeIngredient;
	}


    // JavaBean.aj
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getMeasurement() {
        return this.measurement;
    }
    
    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public Recipe getParent() {
        return this.recipe;
    }
    
    public void setParent(Recipe recipe) {
        this.recipe = recipe;
    }

    public static RecipeIngredient findRecipeIngredient(Long id) {
        if (id == null) return null;
        return entityManager().find(RecipeIngredient.class, id);
    }

    public static List<RecipeIngredient> findRecipeIngredientsByRecipe(Recipe recipe) {
    	TypedQuery<RecipeIngredient> q = entityManager().createQuery("SELECT o FROM RecipeIngredient o "
        		+ "WHERE o.recipe = :recipe", RecipeIngredient.class);
    	q.setParameter("recipe", recipe);
        return q.getResultList();
    }

    @Transactional
    public RecipeIngredient merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        RecipeIngredient merged = this.entityManager.merge(this);
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
        .exclude("*.class", "parent", "usr", "logger").serialize(this);
    }
    
    public String toJson(String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class", "parent", "usr", "logger").serialize(this);
    }
    
    public static RecipeIngredient fromJsonToRecipe(String json) {
        return new JSONDeserializer<RecipeIngredient>()
        .use(null, RecipeIngredient.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection) {
        return new JSONSerializer()
        .exclude("*.class", "parent", "usr", "logger").serialize(collection);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class", "parent", "usr", "logger").serialize(collection);
    }
    
    public static Collection<RecipeIngredient> fromJsonArrayToRecipes(String json) {
        return new JSONDeserializer<List<RecipeIngredient>>()
        .use("values", Recipe.class).deserialize(json);
    }
}
