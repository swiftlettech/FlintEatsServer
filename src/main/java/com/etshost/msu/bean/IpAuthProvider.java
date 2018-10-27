package com.etshost.msu.bean;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.transaction.annotation.Transactional;

import com.etshost.msu.entity.AuthenticationRecord;

/**
 * This AuthProvider evaluates the number of failed authentication attempts 
 * from an IP Address and returns a BadCredentialsException if the number of
 * attempts are too high.
 */
public class IpAuthProvider extends DaoAuthenticationProvider {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());


	@Override
	protected void additionalAuthenticationChecks(final UserDetails userDetails,
			final UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		this.logger.debug("performing additional checks");
		
		super.additionalAuthenticationChecks(userDetails, authentication);
		userDetails.getAuthorities();
		if (this.isIpBanned(((WebAuthenticationDetails) authentication
				.getDetails()).getRemoteAddress())) {
			throw new BadCredentialsException("Banned IP address");
		}
	}

	@Transactional
	private boolean isIpBanned(final String ipAddress) {
		final Logger logger = LoggerFactory.getLogger(this.getClass());

		AuthenticationRecord failureRecord = null;
		try {
			TypedQuery<AuthenticationRecord> failureQuery = AuthenticationRecord
					.findAuthenticationRecordsByIpAddressEquals(ipAddress);
			if (failureQuery.getResultList().size() > 0) {
				failureRecord = failureQuery.getResultList().get(0);
			}
		} catch (final EmptyResultDataAccessException e) {
			logger.warn(e.getLocalizedMessage());
		}
		if (failureRecord != null && failureRecord.getFailureCount() > 10) {
			logger.warn("IP Address {} is banned for {} failures.",ipAddress,failureRecord.getFailureCount());
			
			// don't actually ban yet
			//return true;
		}
		return false;
	}
}
