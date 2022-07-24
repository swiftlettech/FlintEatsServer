package com.etshost.msu.bean;

import java.util.List;

public class RecipeBean {
    public List<IndexedUGCBean> getTags() {
        return tags;
    }

    public void setTags(List<IndexedUGCBean> tags) {
        this.tags = tags;
    }

    public Long id;
    public List<IndexedUGCBean> tags;
    private String title;
    private String description;
    private byte[] image;
    private int servings;
    private boolean published;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

}
