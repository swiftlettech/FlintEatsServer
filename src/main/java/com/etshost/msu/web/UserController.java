package com.etshost.msu.web;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.etshost.msu.entity.Preference;
import com.etshost.msu.entity.Reaction;
import com.etshost.msu.entity.UGC;
import com.etshost.msu.entity.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Controller for the {@link com.etshost.msu.entity.User} class.
 */
@RequestMapping("/users")
@RestController
@Transactional
public class UserController {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Returns the User account associated with the current session
	 * @return JSON User
	 */
	@RequestMapping(value = "/me", method = RequestMethod.GET, produces = "application/json")
	public String profile() {
		User user = User.getLoggedInUser();
		return user.toJson();
	}	
	
	/**
	 * Deletes a user and all of their data
	 * @return Boolean success or failure
	 */
	@RequestMapping(value = "/me", method = RequestMethod.DELETE, produces = "application/json")
	public boolean delete() {
		User user = User.getLoggedInUser();

		// Delete the user, cascades to all records associated to the user
		user.delete();
		return true;
	}
	
	/**
	 * Updates the logged in user's avatar
	 * @param avatar64	base64-encoded avatar
	 * @return		ID of updated User
	 */
	@RequestMapping(value = "/me/avatar", method = RequestMethod.PUT, produces = "application/json")
	public String setAvatar(@RequestBody String avatar64) {
		User user = User.getLoggedInUser();
		user.setAvatarBase64(avatar64);
		// merge and return id
		user.merge();
		return user.getId().toString();
	}
	
	/**
	 * Returns the User account associated with the current session
	 * @return JSON User
	 */
	@RequestMapping(value = "/me/consent", method = RequestMethod.POST, produces = "application/json")
	public String consent(@RequestBody int consent) {
		User user = User.getLoggedInUser();
		if (consent > 0) {
			user.setIrbAccept(Instant.now());
		} else {
			user.setIrbAccept(null);
		}
		user.merge();
		return user.toJson(new String[]{"irbAccept"});
	}
	
	/**
	 * Changes the password of the User account associated with the current session
	 * @return ID if success; error if failed
	 */
	@RequestMapping(value = "/me/password", method = RequestMethod.POST, produces = "application/json")
	public List<String> passwordChange(@RequestBody String password) {
		//TODO  Fix the passwordChange logic to return consistent stuff.
		User user = User.getLoggedInUser();
		if (user == null) {
			return null;
		}
		
		List<String> result = user.changePassword(password);
		if (result.isEmpty()) {
			user.merge();
		}
		return result;
	}
	
	/**
	 * Returns UGC of the User account associated with the current session
	 * @return JSON array of UGC
	 */
	@RequestMapping(value = "/me/mine", method = RequestMethod.GET, produces = "application/json")
	public String ugc() {
		User user = User.getLoggedInUser();
		/*
		Set<UGC> ugcSet = user.getUgc();
		List<UGC> ugcList = new ArrayList<UGC>();
		ugcList.addAll(ugcSet);
		*/
		List<UGC> ugcList = UGC.findAllFeedUGCs(user);
		// sort by create date, most recent first
		ugcList.sort((ugc1, ugc2) -> ugc2.getCreated().compareTo(ugc1.getCreated()));
		return UGC.toJsonArrayUGC(ugcList);
	}
	
	/**
	 * Returns UGC of the User account associated with the current session
	 * @return JSON array of UGC
	 */
	@RequestMapping(value = "/me/faves", method = RequestMethod.GET, produces = "application/json")
	public String ugcFaves() {
		User user = User.getLoggedInUser();
		/*
		Set<UGC> ugcSet = user.getUgc();
		List<UGC> ugcList = new ArrayList<UGC>();
		ugcList.addAll(ugcSet);
		*/
		List<Reaction> reactions = Reaction.findReactionsByUsr(user).getResultList();
		List<UGC> ugcList = new ArrayList<UGC>();
		for (Reaction reaction : reactions) {
			if (reaction.getEndTime() == null) {
				ugcList.add(reaction.getTarget());
			}
		}
		// sort by create date, most recent first
		ugcList.sort((ugc1, ugc2) -> ugc2.getCreated().compareTo(ugc1.getCreated()));
		return UGC.toJsonArrayUGC(ugcList);
	}
	
	/**
	 * Returns Users the User account associated with the current session is following
	 * @return JSON array of Users
	 */
	@RequestMapping(value = "/me/followees", method = RequestMethod.GET, produces = "application/json")
	public String myFollowees() {
		this.logger.debug("landed at /me/followees");

		User user = User.getLoggedInUser();
		Set<User> followees = user.getFollowees();
//		this.logger.debug(followees.toString());
//		this.logger.debug(User.toJsonArray(followees));
		return User.toJsonArray(followees);
	}
	
	/**
	 * Returns Users following the User account associated with the current session
	 * @return JSON array of Users
	 */
	@RequestMapping(value = "/me/followers", method = RequestMethod.GET, produces = "application/json")
	public String myFollowers() {
		this.logger.debug("landed at /me/followers");

		User user = User.getLoggedInUser();
		return User.toJsonArray(user.getFollowers());
	}
	
	/**
	 * Returns preferences of the User account associated with the current session
	 * @return JSON array of Preferences
	 */
	@RequestMapping(value = "/me/preferences", method = RequestMethod.GET, produces = "application/json")
	public String preferences() {
		this.logger.debug("landed at /me/preferences");

		User user = User.getLoggedInUser();
		List<Preference> preferences = null;
		JsonArray prefs = new JsonArray(); 

		try {
			preferences = Preference.findPreferencesByUsr(user).getResultList();
			preferences.forEach(p -> {
				if (p.getEndTime() == null) {
					// if preference has not ended
					JsonObject pref = new JsonObject();
					pref.addProperty("target", p.getTarget());
					pref.addProperty("value", p.getValue());
					prefs.add(pref);
				}
			});
		} catch (Exception e) {
			this.logger.error(e.toString());
		}

		return prefs.toString();
	}
	
	/**
	 * Returns reactions of the User account associated with the current session
	 * @return JSON array of Reactions
	 */
	@RequestMapping(value = "/me/reactions", method = RequestMethod.GET, produces = "application/json")
	public String reactions() {
		this.logger.debug("landed at /me/reactions");

		User user = User.getLoggedInUser();
		List<Reaction> reactions = null;
		JsonArray reacts = new JsonArray();
		
		try {
			reactions = Reaction.findReactionsByUsr(user).getResultList();
			reactions.forEach(r -> {
			if (r.getEndTime() == null) {
				// if reaction has not ended
				JsonObject reaction = new JsonObject();
				reaction.addProperty("target", r.getTarget().getId());
				reaction.addProperty("value", r.getValue());
				reacts.add(reaction);
			}
		});
		} catch (Exception e) {
			this.logger.error(e.toString());
		}

		return reacts.toString();
	}

	/**
	 * Get user avatars of ids
	 * @param ids	IDs of User to get
	 * @return		Avatars of targets
	 */
	@RequestMapping(value = "/avatars", method = RequestMethod.POST, produces = "application/json")
	public String avatars(@RequestBody long[] ids) {
		JsonObject avatars = new JsonObject();
		for (long id : ids) {
			User user = User.findUser(id);
			avatars.addProperty(String.valueOf(id), user.getAvatarBase64Scaled());
		}
		this.logger.debug("returning {} avatars", avatars.size());
		return avatars.toString();
	}
	
	
	/**
	 * Creates a new User from the JSON description
	 * @param user	User to create
	 * @return		ID of created User
	 */
	@PreAuthorize("hasAuthority('admin')")	
	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
	public String create(@Valid @RequestBody User user) {
		JsonArray errors = new JsonArray();

		if (!user.getEmail().equals("")) {
			List<User> u = User
					.findUsersByEmailEquals(user.getEmail())
							.getResultList();
			if (u.size() > 0) {
				if (!(u.size() == 1 && u.get(0).equals(user))) {
					// if not only self matches
					errors.add("email");
				}
			}
		} else {
			errors.add("email");
		}
		
		if (!user.getUsername().equals("")) {
			List<User> u = User
					.findUsersByUsernameEquals(user.getUsername())
							.getResultList();
			if (u.size() > 0) {
				if (!(u.size() == 1 && u.get(0).equals(user))) {
					// if not only self matches
					errors.add("username");
				}
			}
		} else {
			errors.add("username");
		}
		
		if (errors.size() > 0) {
			return errors.toString();
		}

		// persist and return id
		user.persist();
		return user.getId().toString();
	}
	
	/**
	 * Returns JSON representation of User with the given ID
	 * @param id	ID of User to view 
	 * @return		JSON of User
	 */
	@PreAuthorize("hasAuthority('admin')")	
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
	public String delete(@PathVariable("id") long id) {
		User user = User.findUser(id);
		if (user == null) {
			return "0";
		}
		user.delete();
		return user.toJson();
	}
	
	/**
	 * Sets the logged-in user to be following the target (or unfollowing)
	 * @param id	ID of User to follow 
	 * @return		ID of target (negative if unfollowing)
	 */
	@RequestMapping(value = "/follow", method = RequestMethod.POST, produces = "application/json")
	public String follow(@RequestBody Long id) {
		User user = User.getLoggedInUser();
		User target = User.findUser(id);
		if (target == null) {
			return "0";
		}
		if (target.getFollowers().contains(user)) {
			target.getFollowers().remove(user);
			target.merge();
			return String.valueOf(target.getId() * -1);
		} else {
			target.getFollowers().add(user);
			target.merge();
			return String.valueOf(target.getId());

		}
	}
	
	/**
	 * Returns JSON list of Users
	 * @param start			index of first item
	 * @param length		number of items to return
	 * @param orderField	field to order results by
	 * @param orderDir		order direction (ASC or DESC)
	 * @return				JSON array of results
	 */
	@PreAuthorize("hasAuthority('admin')")	
	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json")
	public String list(
			@RequestParam(name = "start", defaultValue = "0") int start,
			@RequestParam(name = "length", defaultValue = "-1") int length,
			@RequestParam(name = "orderField", required = false) String orderField,
			@RequestParam(name = "orderDir", defaultValue = "ASC") String orderDir) {
				
		List<User> results = User.findUserEntries(start, length, orderField, orderDir);
		return User.toJsonArray(results);
	}
	
	/**
	 * Returns JSON list of Users for use with DataTables
	 * @param request request having DataTables arguments:
	 * 			draw, start, length, orderColumnName, ordirDir [, query]
	 * @param response
	 * @throws IOException
	 */
	@PreAuthorize("hasAuthority('admin')")	
	@RequestMapping(value = "/datatables", method = RequestMethod.GET, produces = "application/json")
	public void list(HttpServletRequest request, HttpServletResponse response) throws IOException {
	    PrintWriter out = response.getWriter();
		
		List<String> error = new ArrayList<String>();
	
		int draw = 1;
		int start = 0;
		int length = 10;
		String orderColumn = null;
		String orderColumnName = null;			
		String orderDir = "asc";
		String query = null;
		try {
			draw = Integer.valueOf(request.getParameter("draw"));
			start = Integer.valueOf(request.getParameter("start"));
			length = Integer.valueOf(request.getParameter("length"));	
			orderColumn = request.getParameter("order[0][column]");
			orderColumnName = request.getParameter("columns["+orderColumn+"][name]");
			orderDir = request.getParameter("order[0][dir]");
			query = request.getParameter("search[value]");
		} catch (Exception e) {
			error.add(e.toString());
		}
		this.logger.debug("fetching results");
		String results = User.generateDataTables(draw, start, length,
				orderColumnName, orderDir, query);
	
	    out.print(results);
	}
	
	
	/**
	 * Returns Users the specified User account is following
	 * @return JSON array of Users
	 */
	@RequestMapping(value = "/{id}/followees", method = RequestMethod.GET, produces = "application/json")
	public String followees(@PathVariable("id") long id) {
		this.logger.debug("landed at /followees");

		User user = User.findUser(id);
		return User.toJsonArray(user.getFollowees());
	}
	
	/**
	 * Returns Users following the specified User
	 * @return JSON array of Users
	 */
	@RequestMapping(value = "/{id}/followers", method = RequestMethod.GET, produces = "application/json")
	public String followers(@PathVariable("id") long id) {
		this.logger.debug("landed at /followers");

		User user = User.findUser(id);
		return User.toJsonArray(user.getFollowers());
	}
	
	/**
	 * Change user password.
	 * 
	 * @param id	ID of User
	 * @param password
	 * @return user id
	 */
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value = "/{id}/password", method = RequestMethod.POST, produces = "application/json")
	public List<String> passwordChange(@PathVariable("id") long id, @RequestBody String password) {
		User user = User.findUser(id);
		if (user == null) {
			return null;
		}
		
		List<String> result = user.changePassword(password);
		if (result.isEmpty()) {
			user.merge();
		}
		return result;
	}
	
	/**
	 * Updates the User having the given ID
	 * @param id	ID of User to update
	 * @param user	updated User
	 * @return		ID of updated User
	 */
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = "application/json")
	public String update(@PathVariable("id") long id, @Valid @RequestBody User user) {
		if (user.getId() != id) {
			return "ID error";
		}
		JsonArray errors = new JsonArray();
		
		if (!user.getEmail().equals("")) {
			List<User> u = User
					.findUsersByEmailEqualsAndIdNotEquals(user.getEmail(), user.getId())
							.getResultList();
			if (u.size() > 0) {
				if (!(u.size() == 1 && u.get(0).equals(user))) {
					// if not only self matches
					errors.add("email");
				}
			}
		} else {
			errors.add("email");
		}
		
		if (errors.size() > 0) {
			return errors.toString();
		}
        final User oldUser = User.findUser(user.getId());
        user.setVersion(oldUser.getVersion());
        user.setPassword(oldUser.getPassword());
        user.setCreated(oldUser.getCreated());
        user.setStatus(oldUser.getStatus());
		// merge and return id
		user.merge();
		return user.getId().toString();
	}
	
	/**
	 * Returns JSON representation of User with the given ID
	 * @param id	ID of User to view 
	 * @return		JSON of User
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
	public String view(@PathVariable("id") long id) {
		User user = User.findUser(id);
		if (user == null) {
			return "0";
		}
		return user.toJson();
	}
}
