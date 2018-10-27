package com.etshost.msu.entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

/**
 * A property of a {@link Review}.
 * @author kschemmel
 *
 */
@Audited
@javax.persistence.Entity
@Configurable
@RooJavaBean
@RooJpaActiveRecord
@RooJson
@RooToString
@Transactional
public class ReviewProperty extends Entity {
	
	enum PropertyType {
		ACCESSIBILITY,
		CLEANLINESS,
		FRIENDLINESS,
		SAFETY,
		SELECTION
	}
	
    @ManyToOne
    private Review review;
    
	@Enumerated(EnumType.STRING)
    private PropertyType propertyType;
    
    private int value;

}
