package com.etshost.msu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import com.etshost.msu.entity.FoodPantrySite;
import com.etshost.msu.entity.Market;

@Component
public class CacheInitService implements ApplicationListener<ContextRefreshedEvent> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private Market market;
    @Autowired
    private FoodPantrySite foodPantrySite;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.debug("Start loading locations into cache");
        foodPantrySite.findAllFoodPantrySitesJson();
        market.findAllMarketsJson();
        logger.debug("Done loading locations into cache");
    }

}