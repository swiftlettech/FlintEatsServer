// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.etshost.msu.entity;

import com.etshost.msu.entity.PasswordResetToken;
import com.etshost.msu.entity.User;
import java.time.Instant;

privileged aspect PasswordResetToken_Roo_JavaBean {
    
    public String PasswordResetToken.getToken() {
        return this.token;
    }
    
    public void PasswordResetToken.setToken(String token) {
        this.token = token;
    }
    
    public User PasswordResetToken.getUsr() {
        return this.usr;
    }
    
    public void PasswordResetToken.setUsr(User usr) {
        this.usr = usr;
    }


    public Instant PasswordResetToken.getExpiryDate() {
        return this.expiryDate;
    }
    
    public void PasswordResetToken.setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }
    
}
