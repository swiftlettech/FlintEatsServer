package com.etshost.msu.web;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.etshost.msu.bean.RecipeStepBean;
import com.etshost.msu.bean.UGCCreatorCheck;
import com.etshost.msu.entity.Recipe;
import com.etshost.msu.entity.RecipeStep;
import com.etshost.msu.entity.User;

/**
 * Controller for the {@link com.etshost.msu.entity.RecipeStep} class.
 */
@RequestMapping("/ugc/recipes/{recipeId}/steps")
@RestController
public class RecipeStepController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	protected UGCCreatorCheck creatorChecker;

    /**
     * Creates a new RecipeStep from the JSON description
     *
     * @param recipeId Recipe in which to create the Step
     * @param recipeStep Recipe to create
     * @return ID of created RecipeStep
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
    public String create(@PathVariable("recipeId") long recipeId, @RequestBody RecipeStep recipeStep) {
        this.logger.debug("landed at /ugc/recipes/{recipeId}/step/create");

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
        recipeStep.setParent(base);
        recipeStep.persist();
        return recipeStep.getId().toString();
    }

    /**
     * Returns JSON list of RecipeSteps
     *
     * @param start      index of first item
     * @param length     number of items to return
     * @param orderField field to order results by
     * @param orderDir   order direction (ASC or DESC)
     * @return JSON array of results
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json")
    public String list(@PathVariable("recipeId") long recipeId) {

        final Recipe recipe = Recipe.findRecipe(recipeId);
        List<RecipeStep> results = RecipeStep.findRecipeStepsByRecipe(recipe);
        return RecipeStep.toJsonArray(results);
    }

    /**
     * Deletes the RecipeStep having the given ID
     *
     * @param id   ID of Recipe to Delete
     * @return ID of updated RecipeStep
     */
    @PreAuthorize("@creatorChecker.check(#recipeId)")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
    public String delete(@PathVariable("recipeId") long recipeId, @PathVariable("id") long id) {
        RecipeStep step = RecipeStep.findRecipeStep(id);
		if (step == null) {
			return "ID error";
		}

        Recipe parent = step.getParent();

        if (parent.getId() != recipeId) {
            return "Mismatched Parent";
        }

        step.delete();

        parent.setModified(Instant.now());
        parent.persist();

        return step.getId().toString();
	}

    /**
     * Updates the RecipeStep having the given ID
     *
     * @param id   ID of Recipe to update
     * @param recipeStep updated RecipeStep
     * @return ID of updated RecipeStep
     */
    @PreAuthorize("@creatorChecker.check(#recipeId)")
    @RequestMapping(value = "/{id}", method = RequestMethod.POST, produces = "application/json")
    public String update(@PathVariable("recipeId") long recipeId, @PathVariable("id") long id, @RequestBody RecipeStepBean recipeStep) {
		if (recipeStep.getId() != id || RecipeStep.findRecipeStep(recipeStep.getId())==null) {
			return "ID error";
		}

		final RecipeStep oldRecipeStep = RecipeStep.findRecipeStep(recipeStep.getId());

        if (oldRecipeStep.getParent().getId() != recipeId) {
            return "Mismatched Parent";
        }

		if (recipeStep.getTitle() != null && !recipeStep.getTitle().equals(oldRecipeStep.getTitle())) {
			oldRecipeStep.setTitle(recipeStep.getTitle());
		}
		if (recipeStep.getInstructions() != null && !recipeStep.getInstructions().equals(oldRecipeStep.getInstructions())) {
			oldRecipeStep.setInstructions(recipeStep.getInstructions());
		}
		if (recipeStep.getStepOrder() != null && !recipeStep.getStepOrder().equals(oldRecipeStep.getStepOrder())) {
			oldRecipeStep.setStepOrder(recipeStep.getStepOrder());
		}
		if (recipeStep.getTimeMinutes() != null && !recipeStep.getTimeMinutes().equals(oldRecipeStep.getTimeMinutes())) {
			oldRecipeStep.setTimeMinutes(recipeStep.getTimeMinutes());
		}
		if (recipeStep.getImage() != null && !Arrays.equals(recipeStep.getImage(),oldRecipeStep.getImage())) {
			oldRecipeStep.setImage(recipeStep.getImage());
		}

		oldRecipeStep.setModified(Instant.now());
		oldRecipeStep.persist();
        Recipe parent = oldRecipeStep.getParent();
        parent.setModified(Instant.now());
        parent.persist();

		return recipeStep.getId().toString();
	}

}
