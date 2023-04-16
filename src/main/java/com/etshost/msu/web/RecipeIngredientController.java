package com.etshost.msu.web;
import java.time.Instant;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.etshost.msu.bean.RecipeIngredientBean;
import com.etshost.msu.bean.UGCCreatorCheck;
import com.etshost.msu.entity.Recipe;
import com.etshost.msu.entity.RecipeIngredient;
import com.etshost.msu.entity.User;

/**
 * Controller for the {@link com.etshost.msu.entity.RecipeIngredient} class.
 */
@RequestMapping("/ugc/recipes/{recipeId}/ingredients")
@RestController
public class RecipeIngredientController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	protected UGCCreatorCheck creatorChecker;

    /**
     * Creates a new RecipeIngredient from the JSON description
     *
     * @param recipeId Recipe in which to create the Ingredient
     * @param recipeIngredient Recipe to create
     * @return ID of created RecipeIngredient
     */
	@Transactional
	@PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
    public String create(@PathVariable("recipeId") long recipeId, @RequestBody RecipeIngredient recipeIngredient) {
        this.logger.debug("landed at /ugc/recipes/{recipeId}/ingredients/create");

        // Check for base recipe
        if(Recipe.findRecipe(recipeId) == null) {
            return "Recipe ID error";
        }
        Recipe base = Recipe.findRecipe(recipeId);
        // Check for Admin or if User owns this Recipe
        if (base.getUsr().getId() != User.getLoggedInUser().getId() && !User.getLoggedInUser().admin()) {
            return "Permission error";
        }

        // persist and return id
        recipeIngredient.setParent(base);
        recipeIngredient.persist();
        return recipeIngredient.getId().toString();
    }

    /**
     * Returns JSON list of RecipeIngredients
     *
     * @return JSON array of results
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json")
    public String list(@PathVariable("recipeId") long recipeId) {

        final Recipe recipe = Recipe.findRecipe(recipeId);
        List<RecipeIngredient> results = RecipeIngredient.findRecipeIngredientsByRecipe(recipe);
        return RecipeIngredient.toJsonArray(results);
    }

    /**
     * Deletes the RecipeIngredient having the given ID
     *
     * @param id   ID of RecipeIngredient to Delete
     * @return ID of updated RecipeIngredient
     */
	@Transactional
    @PreAuthorize("@creatorChecker.check(#recipeId)")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
    public String delete(@PathVariable("recipeId") long recipeId, @PathVariable("id") long id) {
        RecipeIngredient ingredient = RecipeIngredient.findRecipeIngredient(id);
		if (ingredient == null) {
			return "ID error";
		}

        Recipe parent = ingredient.getParent();

        if (parent.getId() != recipeId) {
            return "Mismatched Parent";
        }

        ingredient.delete();
        parent.removeIngredient(ingredient);

        parent.setModified(Instant.now());
        parent.persist();

        return ingredient.getId().toString();
	}

    /**
     * Updates the RecipeIngredient having the given ID
     *
     * @param id   ID of Recipe to update
     * @param recipeIngredient updated RecipeIngredient
     * @return ID of updated RecipeIngredient
     */
	@Transactional
    @PreAuthorize("@creatorChecker.check(#recipeId)")
    @RequestMapping(value = "/{id}", method = RequestMethod.POST, produces = "application/json")
    public String update(@PathVariable("recipeId") long recipeId, @PathVariable("id") long id, @RequestBody RecipeIngredientBean recipeIngredient) {
		if (recipeIngredient.getId() != id || RecipeIngredient.findRecipeIngredient(recipeIngredient.getId())==null) {
			return "ID error";
		}

		final RecipeIngredient oldRecipeIngredient = RecipeIngredient.findRecipeIngredient(recipeIngredient.getId());

        if (oldRecipeIngredient.getParent().getId() != recipeId) {
            return "Mismatched Parent";
        }

		if (recipeIngredient.getName() != null && !recipeIngredient.getName().equals(oldRecipeIngredient.getName())) {
			oldRecipeIngredient.setName(recipeIngredient.getName());
		}
		if (recipeIngredient.getMeasurement() != null && !recipeIngredient.getMeasurement().equals(oldRecipeIngredient.getMeasurement())) {
			oldRecipeIngredient.setMeasurement(recipeIngredient.getMeasurement());
		}

		oldRecipeIngredient.setModified(Instant.now());
		oldRecipeIngredient.persist();
        Recipe parent = oldRecipeIngredient.getParent();
        parent.setModified(Instant.now());
        parent.persist();

		return recipeIngredient.getId().toString();
	}

}
