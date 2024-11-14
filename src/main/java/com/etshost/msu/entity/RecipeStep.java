package com.etshost.msu.entity;
import java.io.IOException;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.etshost.msu.bean.BASE64DecodedMultipartFile;
import com.etshost.msu.service.ImageStorageService;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import flexjson.JSON;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@Audited
@javax.persistence.Entity
@Configurable
@Transactional
public class RecipeStep extends Entity {
    
	@Autowired
    @Transient
	ImageStorageService storage;

    @ManyToOne
    @JSON(include = false)
    private Recipe recipe;

    private Integer step_order;

    @Size(min = 3, max = 45)
    private String title;

    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
    private String instructions;

    @JSON(include = false)
    private byte[] image;

    private Integer time_minutes;

	@JsonCreator
	public static RecipeStep factory(
        @JsonProperty("id") Long id,
        @JsonProperty("recipe") Recipe recipe,
        @JsonProperty("step_order") Integer step_order,
        @JsonProperty("title") String title,
        @JsonProperty("instructions") String instructions,
        @JsonProperty("image") String image,
        @JsonProperty("time_minutes") Integer time_minutes
        ) {
		RecipeStep recipeStep = null;
		if (id != null) {
			recipeStep = RecipeStep.findRecipeStep(id);
			if (recipeStep == null) {
				return recipeStep;
			}
		} else {
			recipeStep = new RecipeStep();
		}
        if (recipe != null) {
            recipeStep.setParent(recipe);
        }
		if (title != null) {
			recipeStep.setTitle(title);
		}
		if (instructions != null) {
			recipeStep.setInstructions(instructions);
		}
		if (image != null) {
			recipeStep.setImageBase64(image);
		}
		if (time_minutes != null) {
			recipeStep.setTimeMinutes(time_minutes);
		}
		if (step_order != null) {
			recipeStep.setStepOrder(step_order);
		}
		return recipeStep;
	}


    // JavaBean.aj
    public int getTimeMinutes() {
        return this.time_minutes;
    }
    
    public void setTimeMinutes(int time_minutes) {
        this.time_minutes = time_minutes;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getInstructions() {
        return this.instructions;
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public byte[] getImage() {
        return this.image;
    }
    
    public void setImage(byte[] image) {
        this.image = image;
        MultipartFile f = new BASE64DecodedMultipartFile(image, "photo.jpg");
        try {
            String path = storage.saveImageToServer(f, "recipestep_" + Long.toString(this.getId()) + "_" + System.currentTimeMillis() + ".png");
            this.setImagePath(path);
        } catch (IOException e) {
            this.logger.error(e.toString());
        }
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

    public int getStepOrder() {
        return this.step_order;
    }
    
    public void setStepOrder(int step_order) {
        this.step_order = step_order;
    }

    public Recipe getParent() {
        return this.recipe;
    }
    
    public void setParent(Recipe recipe) {
        this.recipe = recipe;
    }

    public static RecipeStep findRecipeStep(Long id) {
        if (id == null) return null;
        return entityManager().find(RecipeStep.class, id);
    }

    public static List<RecipeStep> findRecipeStepsByRecipe(Recipe recipe) {
    	TypedQuery<RecipeStep> q = entityManager().createQuery("SELECT o FROM RecipeStep o "
        		+ "WHERE o.recipe = :recipe "
        		+ "ORDER BY o.step_order ASC", RecipeStep.class);
    	q.setParameter("recipe", recipe);
        return q.getResultList();
    }

    @Transactional
    public RecipeStep merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        RecipeStep merged = this.entityManager.merge(this);
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
    
    public static RecipeStep fromJsonToRecipe(String json) {
        return new JSONDeserializer<RecipeStep>()
        .use(null, RecipeStep.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection) {
        return new JSONSerializer()
        .exclude("*.class", "parent", "usr", "logger").serialize(collection);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class", "parent", "usr", "logger").serialize(collection);
    }
    
    public static Collection<RecipeStep> fromJsonArrayToRecipes(String json) {
        return new JSONDeserializer<List<RecipeStep>>()
        .use("values", Recipe.class).deserialize(json);
    }

    private String imagePath;
    public String getImagePath() {
        return this.imagePath;
    }
    public void setImagePath(String image_path) {
        this.imagePath = image_path;
    }
    
    public static TypedQuery<RecipeStep> findToMigrate(int limit) {
        EntityManager em = entityManager();
        TypedQuery<RecipeStep> q = em.createQuery("SELECT o FROM RecipeStep AS o WHERE o.image IS NOT NULL AND o.image_path IS NULL", RecipeStep.class);
        q.setMaxResults(limit);
        return q;
    }
}
