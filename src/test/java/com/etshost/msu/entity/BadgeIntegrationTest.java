package com.etshost.msu.entity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = Badge.class)
public class BadgeIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }
    
    @Test
    public void testRemove() {
        Badge obj = dod.getRandomBadge();
        Assert.assertNotNull("Data on demand for 'Badge' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Badge' failed to provide an identifier", id);
        obj = Badge.findBadge(id);
        obj.remove();
        obj.flush();
//        Assert.assertFalse("Failed to remove 'Badge' with identifier '" + id + "'", Entity.isActive(id));
    }
}
