package com.etshost.msu.entity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = Recipe.class)
public class RecipeIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }
    
    @Test
    public void testRemove() {
        Recipe obj = dod.getRandomRecipe();
        Assert.assertNotNull("Data on demand for 'Recipe' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Recipe' failed to provide an identifier", id);
        obj = Recipe.findRecipe(id);
        obj.remove();
        obj.flush();
//        Assert.assertFalse("Failed to remove 'Recipe' with identifier '" + id + "'", Entity.isActive(id));
    }
}
