package com.etshost.msu.bean;


import java.time.Instant;
import java.util.List;

public class DealBean {
    public IndexedUGCBean market;

    public IndexedUGCBean getMarket() {
        return market;
    }

    public void setMarket(IndexedUGCBean market) {
        this.market = market;
    }

    public List<IndexedUGCBean> getTags() {
        return tags;
    }

    public void setTags(List<IndexedUGCBean> tags) {
        this.tags = tags;
    }

    public List<IndexedUGCBean> tags;
    private String title;
    private String price;
    private String text;
    public Instant startDate;
    public Instant endDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long id;


    public byte[] image;

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }



    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }



}
