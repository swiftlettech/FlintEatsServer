package com.etshost.msu.entity;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;

import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Represents an achievement made by a {@link User}.
 */
@Audited
@javax.persistence.Entity
@Configurable
@Transactional
public class Badge extends Entity {

	private byte[] avatar;
	
    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "badges")
    private Set<User> users = new HashSet<User>();
    
}
