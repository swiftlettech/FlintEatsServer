package com.etshost.msu.bean;


import java.util.List;
import java.util.Set;

import com.etshost.msu.entity.ReviewProperty;

public class ReviewBean {
    public Long id;
    public List<IndexedUGCBean> tags;
    public Set<ReviewProperty> properties;
    private String text;
    public Long targetId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<IndexedUGCBean> getTags() {
        return tags;
    }

    public void setProperties(Set<ReviewProperty> properties) {
        this.properties = properties;
    }

    public Set<ReviewProperty> getProperties() {
        return properties;
    }

    public void setTags(List<IndexedUGCBean> tags) {
        this.tags = tags;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTarget(Long targetId) {
        this.targetId = targetId;
    }


}
