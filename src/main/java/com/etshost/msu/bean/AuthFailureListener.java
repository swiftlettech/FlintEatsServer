package com.etshost.msu.bean;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.etshost.msu.entity.AuthenticationRecord;
import com.etshost.msu.entity.User;

/**
 * This class implements an application event listener to  increment the
 * authentication failure count when a user fails to authenticate.
 * @author zpowers
 *
 */
@Component
public class AuthFailureListener implements
		ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	@Transactional
	public void onApplicationEvent(
			final AuthenticationFailureBadCredentialsEvent event) {
		
		final String username = event.getAuthentication().getName();
		final WebAuthenticationDetails authDetail = (WebAuthenticationDetails) event
				.getAuthentication().getDetails();
		final String ipAddress = authDetail.getRemoteAddress();
		this.logger.debug("ip: "+ipAddress);
		User user = null;
		AuthenticationRecord failureRecord = null;
		
		try {
			TypedQuery<User> userQuery = User.findUsersByEmailEquals(username);
			if(userQuery.getResultList().size() > 0) {
				user = userQuery.getResultList().get(0);
				user.reportLoginFailure();

			}
		} catch (final EmptyResultDataAccessException e) {
			this.logger.debug("EmptyResultException");
		}
		
		try {
			TypedQuery<AuthenticationRecord> failureQuery = AuthenticationRecord
					.findAuthenticationRecordsByIpAddressEquals(ipAddress);
			if(failureQuery.getResultList().size() > 0) {
				failureRecord = failureQuery.getResultList().get(0);
				failureRecord.setFailedUser(user);
				failureRecord.incrementFailureCount();
				failureRecord.merge();
				this.logger.warn("IP Address: {} has {} failures.",
						ipAddress, failureRecord.getFailureCount());
			} else {
				failureRecord = new AuthenticationRecord(ipAddress, user);
				failureRecord.incrementFailureCount();
				failureRecord.persist();
				this.logger.warn("IP Address: {} has {} failures.",
						ipAddress, failureRecord.getFailureCount());
				return;
			}
		} catch (final EmptyResultDataAccessException e) {
			this.logger.debug("EmptyResultException");
		}
	}
}
