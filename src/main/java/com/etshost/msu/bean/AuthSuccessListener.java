package com.etshost.msu.bean;

import javax.persistence.TypedQuery;

import org.springframework.context.ApplicationListener;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.etshost.msu.entity.AuthenticationRecord;
import com.etshost.msu.entity.User;

/**
 * This class implements an application event listener to  reset the
 * authentication failure count when a user sucessfully authenticates.
 * @author zpowers
 *
 */
@Component
public class AuthSuccessListener implements
		ApplicationListener<AuthenticationSuccessEvent> {
	
	@Override
	@Transactional
	public void onApplicationEvent(final AuthenticationSuccessEvent event) {

		final String username = event.getAuthentication().getName();
		final WebAuthenticationDetails authDetail = (WebAuthenticationDetails) event
				.getAuthentication().getDetails();
		final String ipAddress = authDetail.getRemoteAddress();
		AuthenticationRecord failureRecord = null;
		User user = null;
		
		try {
			TypedQuery<AuthenticationRecord> failureQuery = AuthenticationRecord
					.findAuthenticationRecordsByIpAddressEquals(ipAddress);
			if (failureQuery.getResultList().size() > 0)
				failureRecord = failureQuery.getResultList().get(0);
		} catch (final EmptyResultDataAccessException e) {
			return;
		}
		if(failureRecord == null) {
			return;
		}
		if(failureRecord.getFailureCount() == 0) {
			return;
		}
		try {
			TypedQuery<User> userQuery = User
					.findUsersByEmailEquals(username);
			if (userQuery.getResultList().size() > 0) {
				user = userQuery.getResultList().get(0);
			}
		} catch (final EmptyResultDataAccessException e) {
		}
		//Send Email that a successful login was made?
		failureRecord.resetFailureCount();
		failureRecord.setFailedUser(user);
		failureRecord.merge();
	}
}
