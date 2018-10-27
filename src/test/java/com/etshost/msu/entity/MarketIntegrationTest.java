package com.etshost.msu.entity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = Market.class, findAll = false)
public class MarketIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }
    
    @Test
    public void testRemove() {
        Market obj = dod.getRandomMarket();
        Assert.assertNotNull("Data on demand for 'Market' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Market' failed to provide an identifier", id);
        obj = Market.findMarket(id);
        obj.remove();
        obj.flush();
//        Assert.assertFalse("Failed to remove 'Market' with identifier '" + id + "'", Entity.isActive(id));
    }
}
