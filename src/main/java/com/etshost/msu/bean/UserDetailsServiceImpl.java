package com.etshost.msu.bean;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import com.etshost.msu.entity.Role;
import com.etshost.msu.entity.Status;
import com.etshost.msu.entity.User;

/**
 * Implements a custom user detail service to provide user detail information
 * for the Spring Security framework using User and their associated Roles.
 */
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	public org.springframework.security.core.userdetails.User buildNewUser(final User msuuser) {
		final String username = msuuser.getUsername();
		final String password = msuuser.getPassword();
		boolean enabled = false;
		boolean accountNonExpired = false;
		boolean credentialsNonExpired = false;
		boolean accountNonLocked = false;
		if (msuuser.getStatus() == Status.ACTIVE) {
			enabled = true;
			accountNonExpired = true;
			credentialsNonExpired = true;
			accountNonLocked = true;
		}
		final Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority("login"));
		for (final Role role : msuuser.getRoles()) {
			authorities.add(new SimpleGrantedAuthority(role.getName()));
		}
		final org.springframework.security.core.userdetails.User user =
				new org.springframework.security.core.userdetails.User(username, password,
						enabled, accountNonExpired, credentialsNonExpired, accountNonLocked,
						authorities);
		return user;
	}

	@Override
	public UserDetails loadUserByUsername(final String username)
			throws UsernameNotFoundException, DataAccessException {
		User user = null;
		final TypedQuery<User> results = User
				.findUsersByUsernameEquals(username);
		if (results.getResultList().size() > 0) {
			user = results.getResultList().get(0);
		}
		if (user == null) {
			throw new UsernameNotFoundException("User not found");
		}
		return this.buildNewUser(user);
	}
}
