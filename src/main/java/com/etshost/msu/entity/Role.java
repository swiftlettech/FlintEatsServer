package com.etshost.msu.entity;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.ManyToMany;
import javax.persistence.TypedQuery;

import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;

/**
 * An authority granted to {@link User}s to determine privileges.
 */
@Audited
@javax.persistence.Entity
@Configurable
@RooJavaBean
@RooJpaActiveRecord(finders = { "findRolesByStatus" })
@RooJson
@RooToString
public class Role extends Entity {
	
	private String name;

    private String description;
	
    /**
     * A Set of Users who belong to this Role.
     */
    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "roles")
    private Set<User> users = new HashSet<User>();

    public static Role findRoleByName(String name) {
        EntityManager em = Role.entityManager();
        TypedQuery<Role> q = em.createQuery("SELECT o FROM Role AS o WHERE o.name LIKE :name", Role.class);
        q.setParameter("name", name);
        return q.getSingleResult();
    }
}
