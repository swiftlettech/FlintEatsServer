package com.etshost.msu.bean;

import com.etshost.msu.entity.Tip;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import java.util.List;

public class TipBean {

    private String tipType;

    private String text;

    private Long id;

    public List<IndexedUGCBean> tags;

    public List<IndexedUGCBean> getTags() {
        return tags;
    }

    public void setTags(List<IndexedUGCBean> tags) {
        this.tags = tags;
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
