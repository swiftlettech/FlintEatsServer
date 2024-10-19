package com.etshost.msu.entity;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Represents a food item.
 */
@Audited
@javax.persistence.Entity
@Configurable
@Transactional
public class Food extends Entity {
	
	private String name;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "food")
    private Set<FoodProperty> properties = new HashSet<FoodProperty>();

}
