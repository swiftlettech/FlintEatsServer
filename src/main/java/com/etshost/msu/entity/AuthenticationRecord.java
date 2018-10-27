package com.etshost.msu.entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;

/**
 * Keeps track of wrong password attempts made by a user or IP Address.
 * Implementation allows for ease of account locking and IP tracking.
 */
@Audited
@Configurable
@javax.persistence.Entity
@RooJavaBean
@RooJpaActiveRecord(finders = { "findAuthenticationRecordsByIpAddressEquals" })
@RooJson
@RooToString
public class AuthenticationRecord extends Entity {

    public AuthenticationRecord() {
        super();
    }

    public AuthenticationRecord(final String ipAddress, final User user) {
        super();
        this.ipAddress = ipAddress;
        this.failedUser = user;
    }

    @ManyToOne
    private User failedUser;

    private int failureCount;

    private String ipAddress;

    public void incrementFailureCount() {
        this.failureCount++;
    }

    public void resetFailureCount() {
        this.failureCount = 0;
    }
}
