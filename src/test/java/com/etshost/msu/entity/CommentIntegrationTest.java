package com.etshost.msu.entity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.roo.addon.test.RooIntegrationTest;

@RooIntegrationTest(entity = Comment.class)
public class CommentIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }
    
    @Test
    public void testRemove() {
        Comment obj = dod.getRandomComment();
        Assert.assertNotNull("Data on demand for 'Comment' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Comment' failed to provide an identifier", id);
        obj = Comment.findComment(id);
        obj.remove();
        obj.flush();
//        Assert.assertFalse("Failed to remove 'Comment' with identifier '" + id + "'", Entity.isActive(id));
    }
}
