package com.etshost.msu.entity;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Primary key of a {@link Viewing}.
 */
@Embeddable
public class ViewingPk implements Serializable { 

	// generated serialVersionUID (AspectJ)
	private static final long serialVersionUID = 3929307768681239039L;
	
	public ViewingPk() {
		this.setStartTime(Instant.now());
	}
	
	public ViewingPk(long userId, long targetId, Instant startTime) {
		this.setUserId(userId);
		this.setTargetId(targetId);
		this.setStartTime(startTime);
	}

	@Column(name = "userId")
    private Long userId;
    
    @Column(name = "targetId")
    private Long targetId;

    @Column(name = "startTime")
    private Instant startTime;
    
    public long getUserId() {
        return this.userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getTargetId() {
        return this.targetId;
    }

    public void setTargetId(long targetId) {
        this.targetId = targetId;
    }
    
    public Instant getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }
    
    public long getSerialVersionUID() {
    	return serialVersionUID;
    }
    
	// Spring complains when this isn't present
	public boolean equals(Object o) {
        if (this == o) {
        	return true;
        }
        if (o == null || getClass() != o.getClass()) {
        	return false;
        }

        ViewingPk that = (ViewingPk) o;
        
        if (this.userId != that.userId
        		|| this.targetId != that.targetId
        		|| this.startTime != that.startTime) {
        	return false;
        }

        return true;
    }
	
	// Spring complains when this isn't present
    public int hashCode() {
        int result;
        result = (int)(userId + targetId + startTime.toEpochMilli());
        return result;
    }
}