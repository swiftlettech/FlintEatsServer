// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.etshost.msu.entity;

import com.etshost.msu.entity.UGC;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

privileged aspect UGC_Roo_Jpa_ActiveRecord {
    
    public static final List<String> UGC.fieldNames4OrderClauseFilter = java.util.Arrays.asList("usr");
    
    public static long UGC.countUGCS() {
        return entityManager().createQuery("SELECT COUNT(o) FROM UGC o", Long.class).getSingleResult();
    }
    
    public static List<UGC> UGC.findAllUGCS() {
        return entityManager().createQuery("SELECT o FROM UGC o", UGC.class).getResultList();
    }
    
    public static List<UGC> UGC.findAllUGCS(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM UGC o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, UGC.class).getResultList();
    }
    
    public static UGC UGC.findUGC(Long id) {
        if (id == null) return null;
        return entityManager().find(UGC.class, id);
    }
    
    public static List<UGC> UGC.findUGCEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM UGC o", UGC.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<UGC> UGC.findUGCEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM UGC o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, UGC.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public UGC UGC.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        UGC merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}