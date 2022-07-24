package com.etshost.msu.entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

import flexjson.JSON;

@Audited
@javax.persistence.Entity
@Configurable
@RooJavaBean
@RooJpaActiveRecord
@RooJson
@RooToString
@Transactional
public class RecipeStep extends UGC {

    @ManyToOne
    @JoinColumn(name="recipe_id")
    private Recipe recipe;

    private Integer step_order;

    private String title;

    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
    private String instructions;

    @JSON(include = false)
    private byte[] image;

    private Integer time_minutes;

}
