// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.etshost.msu.entity;

import com.etshost.msu.entity.Comment;
import com.etshost.msu.entity.Entity;
import com.etshost.msu.entity.Status;
import com.etshost.msu.entity.Tag;
import java.time.Instant;
import java.util.Set;
import org.slf4j.Logger;

privileged aspect Entity_Roo_JavaBean {
    
    public Logger Entity.getLogger() {
        return this.logger;
    }
    
    public Long Entity.getId() {
        return this.id;
    }
    
    public void Entity.setId(Long id) {
        this.id = id;
    }
    
    public Instant Entity.getCreated() {
        return this.created;
    }
    
    public void Entity.setCreated(Instant created) {
        this.created = created;
    }
    
    public Instant Entity.getModified() {
        return this.modified;
    }
    
    public void Entity.setModified(Instant modified) {
        this.modified = modified;
    }
    
    public Status Entity.getStatus() {
        return this.status;
    }
    
    public void Entity.setStatus(Status status) {
        this.status = status;
    }
    
    public Integer Entity.getVersion() {
        return this.version;
    }
    
    public void Entity.setVersion(Integer version) {
        this.version = version;
    }
    
    public Set<Tag> Entity.getTags() {
        return this.tags;
    }
    
    public void Entity.setTags(Set<Tag> tags) {
        this.tags = tags;
    }
    
    public Set<Comment> Entity.getComments() {
        return this.comments;
    }
    
    public void Entity.setComments(Set<Comment> comments) {
        this.comments = comments;
    }
    
}