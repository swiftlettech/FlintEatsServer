package com.etshost.msu.bean;

public class RecipeStepBean {
    public Long id;
    private String title;
    private String instructions;
    private byte[] image;
    private int time_minutes;
    private int step_order;


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

    public Integer getStepOrder() {
        return step_order;
    }

    public void setStepOrder(Integer step_order) {
        this.step_order = step_order;
    }

    public Integer getTimeMinutes() {
        return time_minutes;
    }

    public void setTimeMinutes(Integer time_minutes) {
        this.time_minutes = time_minutes;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

}
