package com.etshost.msu.web;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.etshost.msu.bean.IndexedUGCBean;
import com.etshost.msu.bean.RecipeBean;
import com.etshost.msu.bean.UGCCreatorCheck;
import com.etshost.msu.entity.Recipe;
import com.etshost.msu.entity.Tag;
import com.etshost.msu.entity.User;
import com.etshost.msu.entity.Viewing;

/**
 * Controller for the {@link com.etshost.msu.entity.Recipe} class.
 */
@RequestMapping("/ugc/recipes")
@RestController
@Transactional
public class RecipeController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	protected UGCCreatorCheck creatorChecker;

    /**
     * Creates a new Recipe from the JSON description
     *
     * @param recipe Recipe to create
     * @return ID of created Recipe
     */
	@PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
    public String create(@RequestBody Recipe recipe) {
        recipe.setUsr(User.getLoggedInUser());

        // persist and return id
        recipe.persist();
        return recipe.getId().toString();
    }

    /**
     * Returns JSON list of Recipes
     *
     * @param start      index of first item
     * @param length     number of items to return
     * @param orderField field to order results by
     * @param orderDir   order direction (ASC or DESC)
     * @return JSON array of results
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json")
    public String list(
            @RequestParam(name = "start", defaultValue = "0") int start,
            @RequestParam(name = "length", defaultValue = "-1") int length,
            @RequestParam(name = "orderField", required = false) String orderField,
            @RequestParam(name = "orderDir", defaultValue = "ASC") String orderDir) {

        List<Recipe> results = Recipe.findRecipeEntries(start, length, orderField, orderDir);
        return Recipe.toJsonArray(results);
    }

    /**
     * Gets the Recipe having the given ID
     *
     * @param id   ID of Recipe to update
     * @return Recipe
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public String getRecipe(@PathVariable("id") long id) {
		Recipe recipe = Recipe.findRecipe(id);
		if (recipe == null) {
			return "0";
		}

        new Viewing(User.getLoggedInUserId(), recipe.getId()).persist();
        return recipe.toJson();
    }
    /**
     * Updates the Recipe having the given ID
     *
     * @param id   ID of Recipe to update
     * @param recipe updated Recipe
     * @return ID of updated Recipe
     */
    @PreAuthorize("@creatorChecker.check(#id)")
    @RequestMapping(value = "/{id}", method = RequestMethod.POST, produces = "application/json")
    public String update(@PathVariable("id") long id, @RequestBody RecipeBean recipe) {
		if (recipe.getId() != id || Recipe.findRecipe(recipe.getId())==null) {
			return "ID error";
		}

		final Recipe oldRecipe = Recipe.findRecipe(recipe.getId());

		if (recipe.getTitle() != null && !recipe.getTitle().equals(oldRecipe.getTitle())) {
			oldRecipe.setTitle(recipe.getTitle());
		}
		if (recipe.getDescription() != null && !recipe.getDescription().equals(oldRecipe.getDescription())) {
			oldRecipe.setDescription(recipe.getDescription());
		}
		if (recipe.getServings() != null && !recipe.getServings().equals(oldRecipe.getServings())) {
			oldRecipe.setServings(recipe.getServings());
		}
		if (recipe.getPublished() != null && !recipe.getPublished().equals(oldRecipe.getPublished())) {
			oldRecipe.setPublished(recipe.getPublished());
		}
		if (recipe.getImage() != null && !Arrays.equals(recipe.getImage(),oldRecipe.getImage())) {
			oldRecipe.setImage(recipe.getImage());
		}

		if (recipe.getTags() != null) {
			Set<Tag> tags = new HashSet<Tag>();
			for (IndexedUGCBean u : recipe.getTags()) {
				tags.add(Tag.findTag(u.id));
			}
			if (!tags.equals(oldRecipe.getTags())) {
				oldRecipe.setTags(tags);
			}
		}

		oldRecipe.setModified(Instant.now());
		oldRecipe.persist();
		return recipe.getId().toString();
	}

}
