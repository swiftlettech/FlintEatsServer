package com.etshost.msu.entity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = FoodProperty.class)
public class FoodPropertyIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }
    
    @Test
    public void testRemove() {
        FoodProperty obj = dod.getRandomFoodProperty();
        Assert.assertNotNull("Data on demand for 'FoodProperty' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'FoodProperty' failed to provide an identifier", id);
        obj = FoodProperty.findFoodProperty(id);
        obj.remove();
        obj.flush();
//        Assert.assertFalse("Failed to remove 'FoodProperty' with identifier '" + id + "'", Entity.isActive(id));
    }
}
