package com.etshost.msu.entity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = Tag.class)
public class TagIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }
    
    @Test
    public void testRemove() {
        Tag obj = dod.getRandomTag();
        Assert.assertNotNull("Data on demand for 'Tag' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Tag' failed to provide an identifier", id);
        obj = Tag.findTag(id);
        obj.remove();
        obj.flush();
//        Assert.assertFalse("Failed to remove 'Tag' with identifier '" + id + "'", Entity.isActive(id));
    }
}
