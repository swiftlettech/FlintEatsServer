// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.etshost.msu.entity;

import com.etshost.msu.entity.Entity;
import com.etshost.msu.entity.Review;
import com.etshost.msu.entity.ReviewProperty;
import java.util.Set;

privileged aspect Review_Roo_JavaBean {
    
    public Entity Review.getTarget() {
        return this.target;
    }
    
    public void Review.setTarget(Entity target) {
        this.target = target;
    }
    
    public Set<ReviewProperty> Review.getProperties() {
        return this.properties;
    }
    
    public void Review.setProperties(Set<ReviewProperty> properties) {
        this.properties = properties;
    }
    
    public String Review.getText() {
        return this.text;
    }
    
    public void Review.setText(String text) {
        this.text = text;
    }
    
}