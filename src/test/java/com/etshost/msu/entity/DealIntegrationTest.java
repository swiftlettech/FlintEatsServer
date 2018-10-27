package com.etshost.msu.entity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = Deal.class)
public class DealIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }
    
    @Test
    public void testRemove() {
        Deal obj = dod.getRandomDeal();
        Assert.assertNotNull("Data on demand for 'Deal' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Deal' failed to provide an identifier", id);
        obj = Deal.findDeal(id);
        obj.remove();
        obj.flush();
//        Assert.assertFalse("Failed to remove 'Deal' with identifier '" + id + "'", Entity.isActive(id));
    }
}
