package com.etshost.msu.entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Represents a property of a {@link Food}.
 */
@Audited
@javax.persistence.Entity
@Configurable
@Transactional
public class FoodProperty extends Entity {
	
	enum PropertyType {
		CALORIES,
		CARBS,
		FAT,
		PROTEIN
	}
	
    @ManyToOne
    private Food food;
    
	@Enumerated(EnumType.STRING)
    private PropertyType propertyType;
    
    private double value;

}
