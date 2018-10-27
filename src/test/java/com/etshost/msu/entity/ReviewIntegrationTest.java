package com.etshost.msu.entity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = Review.class)
public class ReviewIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }
    
    @Test
    public void testRemove() {
        Review obj = dod.getRandomReview();
        Assert.assertNotNull("Data on demand for 'Review' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Review' failed to provide an identifier", id);
        obj = Review.findReview(id);
        obj.remove();
        obj.flush();
//        Assert.assertFalse("Failed to remove 'Review' with identifier '" + id + "'", Entity.isActive(id));
    }
}
