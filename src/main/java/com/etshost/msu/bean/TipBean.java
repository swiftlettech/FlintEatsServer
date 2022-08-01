package com.etshost.msu.bean;

import java.util.List;

public class TipBean {

    private String tipType;

    private String text;

    private Long id;

    public List<IndexedUGCBean> tags;

    public byte[] image;

    public List<IndexedUGCBean> getTags() {
        return tags;
    }

    public void setTags(List<IndexedUGCBean> tags) {
        this.tags = tags;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getTipType() {
        return tipType;
    }

    public void setTipType(String tipType) {
        this.tipType = tipType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



}
