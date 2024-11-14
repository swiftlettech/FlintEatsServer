package com.etshost.msu.entity;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.etshost.msu.bean.BASE64DecodedMultipartFile;
import com.etshost.msu.service.ImageStorageService;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

import java.io.IOException;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Size;

import flexjson.JSON;

/**
 * Represents a recipe for a culinary dish.
 */
@Analyzer(impl = org.apache.lucene.analysis.standard.StandardAnalyzer.class)
@Indexed
@Audited
@javax.persistence.Entity
@Configurable
@Transactional
public class Recipe extends UGC {
    
	@Autowired
    @Transient
	ImageStorageService storage;
    
    @Size(min = 3, max = 255)
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)	
    private String title;

    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
    private String description;

    @JSON(include = false)
    private byte[] image;
	
    private int servings;

    @Field(index=Index.YES, store=Store.NO)
    private boolean published;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="recipe")
    @JSON(name = "steps")
    private Set<RecipeStep> steps = new HashSet<RecipeStep>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy="recipe")
    @JSON(name = "ingredients")
    private Set<RecipeIngredient> ingredients = new HashSet<RecipeIngredient>();

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


    @SuppressWarnings("unchecked")
	public static List<Recipe> search(String q) {
        Logger logger = LoggerFactory.getLogger(Recipe.class);
        EntityManager em = Entity.entityManager();
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);

        logger.debug("searching Recipes for: {}", q);

        QueryBuilder qb = fullTextEntityManager
                .getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Recipe.class)
                .get();
        org.apache.lucene.search.Query luceneQuery = qb.bool()
            .must(qb
                .keyword()
                .fuzzy()
                .withPrefixLength(3)
                .onFields("title", "description")
                .matching(q)
                .createQuery())
            .filteredBy(new TermQuery(new Term("published", "true")))
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
		    fullTextEntityManager.createFullTextQuery(luceneQuery, Recipe.class);
        logger.debug("jpaQuery: {}", jpaQuery.toString());
//        @SuppressWarnings("unchecked") List<Object[]> results = jpaQuery.getResultList();
//        for (Object[] result : results) {
//            Explanation e = (Explanation) result[1];
//            logger.debug(e.toString());
//        }


        // execute search
        logger.debug("results size: {}", jpaQuery.getResultList().size());
        List<Recipe> result = jpaQuery.getResultList();
        return result;
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
        MultipartFile f = new BASE64DecodedMultipartFile(image, "photo.jpg");
        try {
            String path = storage.saveImageToServer(f, "recipe_" + Long.toString(this.getId()) + "_" + System.currentTimeMillis() + ".png");
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

    public boolean getPublished() {
        return this.published;
    }
    
    public void setPublished(Boolean published) {
        this.published = published;
    }

    public Set<RecipeStep> getSteps() {
        return this.steps;
    }

    public void setSteps(Set<RecipeStep> steps) {
        this.steps = steps;
    }

    public void removeStep(RecipeStep step) {
        this.steps.remove(step);
        step.setParent(null);
    }

    public Set<RecipeIngredient> getIngredients() {
        return this.ingredients;
    }

    public void setIngredients(Set<RecipeIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void removeIngredient(RecipeIngredient ingredient) {
        this.ingredients.remove(ingredient);
        ingredient.setParent(null);
    }

    // Jpa_ActiveRecord.aj
    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("title","servings");
    
    public static long countRecipes() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Recipe o", Long.class).getSingleResult();
    }
    
    public static List<Recipe> findAllRecipes() {
        return entityManager().createQuery("SELECT o FROM Recipe o", Recipe.class).getResultList();
    }
    public static List<Recipe> findAllPublishedRecipes() {
        return entityManager().createQuery("SELECT o FROM Recipe o WHERE o.published = true", Recipe.class).getResultList();
    }
    
    public static List<Recipe> findAllPublishedRecipes(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Recipe o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        jpaQuery = jpaQuery + " WHERE o.published = true";
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
        .exclude("*.class", "*.logger", "steps.usr", "ingredients.usr", "usr.email", "usr.phone").serialize(this);
    }
    
    public String toJson(String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class", "*.logger", "steps.usr", "ingredients.usr", "usr.email", "usr.phone").serialize(this);
    }
    
    public static Recipe fromJsonToRecipe(String json) {
        return new JSONDeserializer<Recipe>()
        .use(null, Recipe.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection) {
        return new JSONSerializer()
        .exclude("*.class", "*.logger", "steps.usr", "ingredients.usr", "usr.email", "usr.phone").serialize(collection);
    }
    
    public static String toJsonArray(Collection<? extends Entity> collection, String[] fields) {
        return new JSONSerializer()
        .include(fields).exclude("*.class", "*.logger", "steps.usr", "ingredients.usr", "usr.email", "usr.phone").serialize(collection);
    }
    
    public static Collection<Recipe> fromJsonArrayToRecipes(String json) {
        return new JSONDeserializer<List<Recipe>>()
        .use("values", Recipe.class).deserialize(json);
    }

    private String imagePath;
    public String getImagePath() {
        return this.imagePath;
    }
    public void setImagePath(String image_path) {
        this.imagePath = image_path;
    }
    
    public static TypedQuery<Recipe> findToMigrate(int limit) {
        EntityManager em = entityManager();
        TypedQuery<Recipe> q = em.createQuery("SELECT o FROM Recipe AS o WHERE o.image IS NOT NULL AND o.image_path IS NULL", Recipe.class);
        q.setMaxResults(limit);
        return q;
    }
    
}
