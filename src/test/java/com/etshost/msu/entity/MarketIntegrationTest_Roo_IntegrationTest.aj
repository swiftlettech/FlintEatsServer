// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.etshost.msu.entity;

import com.etshost.msu.entity.Market;
import com.etshost.msu.entity.MarketDataOnDemand;
import com.etshost.msu.entity.MarketIntegrationTest;
import java.util.Iterator;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

privileged aspect MarketIntegrationTest_Roo_IntegrationTest {
    
    declare @type: MarketIntegrationTest: @RunWith(SpringJUnit4ClassRunner.class);
    
    declare @type: MarketIntegrationTest: @ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml");
    
    declare @type: MarketIntegrationTest: @Transactional;
    
    @Autowired
    MarketDataOnDemand MarketIntegrationTest.dod;
    
    @Test
    public void MarketIntegrationTest.testCountMarkets() {
        Assert.assertNotNull("Data on demand for 'Market' failed to initialize correctly", dod.getRandomMarket());
        long count = Market.countMarkets();
        Assert.assertTrue("Counter for 'Market' incorrectly reported there were no entries", count > 0);
    }
    
    @Test
    public void MarketIntegrationTest.testFindMarket() {
        Market obj = dod.getRandomMarket();
        Assert.assertNotNull("Data on demand for 'Market' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Market' failed to provide an identifier", id);
        obj = Market.findMarket(id);
        Assert.assertNotNull("Find method for 'Market' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'Market' returned the incorrect identifier", id, obj.getId());
    }
    
    @Test
    public void MarketIntegrationTest.testFindMarketEntries() {
        Assert.assertNotNull("Data on demand for 'Market' failed to initialize correctly", dod.getRandomMarket());
        long count = Market.countMarkets();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<Market> result = Market.findMarketEntries(firstResult, maxResults);
        Assert.assertNotNull("Find entries method for 'Market' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'Market' returned an incorrect number of entries", count, result.size());
    }
    
    @Test
    public void MarketIntegrationTest.testFlush() {
        Market obj = dod.getRandomMarket();
        Assert.assertNotNull("Data on demand for 'Market' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Market' failed to provide an identifier", id);
        obj = Market.findMarket(id);
        Assert.assertNotNull("Find method for 'Market' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyMarket(obj);
        Integer currentVersion = obj.getVersion();
        obj.flush();
        Assert.assertTrue("Version for 'Market' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void MarketIntegrationTest.testMergeUpdate() {
        Market obj = dod.getRandomMarket();
        Assert.assertNotNull("Data on demand for 'Market' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Market' failed to provide an identifier", id);
        obj = Market.findMarket(id);
        boolean modified =  dod.modifyMarket(obj);
        Integer currentVersion = obj.getVersion();
        Market merged = (Market)obj.merge();
        obj.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'Market' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void MarketIntegrationTest.testPersist() {
        Assert.assertNotNull("Data on demand for 'Market' failed to initialize correctly", dod.getRandomMarket());
        Market obj = dod.getNewTransientMarket(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'Market' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'Market' identifier to be null", obj.getId());
        try {
            obj.persist();
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext();) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        obj.flush();
        Assert.assertNotNull("Expected 'Market' identifier to no longer be null", obj.getId());
    }
    
}