package com.etshost.msu.bean;

import com.etshost.msu.entity.AuthenticationRecord;
import com.etshost.msu.entity.UGC;
import com.etshost.msu.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;



@Component("creatorChecker")
public class UGCCreatorCheck {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public boolean check(Long id) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return UGC.findUGC(id).getUsr().equals(User.getLoggedInUser());
    }
}
