package com.etshost.msu.entity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = Food.class)
public class FoodIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }
    
    @Test
    public void testRemove() {
        Food obj = dod.getRandomFood();
        Assert.assertNotNull("Data on demand for 'Food' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Food' failed to provide an identifier", id);
        obj = Food.findFood(id);
        obj.remove();
        obj.flush();
//        Assert.assertFalse("Failed to remove 'Food' with identifier '" + id + "'", Entity.isActive(id));
    }
}
