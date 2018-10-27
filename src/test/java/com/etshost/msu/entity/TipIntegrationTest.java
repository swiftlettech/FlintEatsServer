package com.etshost.msu.entity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = Tip.class)
public class TipIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }
    
    @Test
    public void testRemove() {
        Tip obj = dod.getRandomTip();
        Assert.assertNotNull("Data on demand for 'Tip' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Tip' failed to provide an identifier", id);
        obj = Tip.findTip(id);
        obj.remove();
        obj.flush();
//        Assert.assertFalse("Failed to remove 'Tip' with identifier '" + id + "'", Entity.isActive(id));
    }
}
