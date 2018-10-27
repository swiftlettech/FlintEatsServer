package com.etshost.msu.web;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.etshost.msu.entity.Preference;
import com.etshost.msu.entity.User;

/**
 * Controller for the {@link com.etshost.msu.entity.Preference} class.
 */
@RequestMapping("/preferences")
@RestController
public class PreferenceController {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Creates a new Preference from the JSON description
	 * @param preference	Preference to create
	 * @return		hashcode of created Preference
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
	public String create(@RequestBody Preference preference) {
		User user = User.getLoggedInUser();
		// "clear" prior preferences
		List<Preference> prefs = Preference.findPreferences(user, preference.getTarget()).getResultList();
		for (Preference pref : prefs) {
			if (pref.getEndTime() == null) {
				// if active preference
				if (pref.getValue() == preference.getValue()) {
					// if same value, disable and return 0
					pref.setEndTime(Instant.now());
					pref.merge();
					return "0";
				}
				// else just disable
				pref.setEndTime(Instant.now());
				pref.merge();
			}
		}
		// persist and return hashcode
		preference.setUsr(user);
		preference.persist();
		
		// send preference to preference server
		try {
			PreferenceServerController preferenceServerController = new PreferenceServerController();
			String resp = preferenceServerController.postUserPreference(preference);
			this.logger.debug(resp);
		} catch (Exception e) {
			this.logger.debug("Preference server failure: {}", e.toString());
		}

		return String.valueOf(preference.getId());
	}
	
	/**
	 * Returns JSON list of Preferences
	 * @param start			index of first item
	 * @param length		number of items to return
	 * @param orderField	field to order results by
	 * @param orderDir		order direction (ASC or DESC)
	 * @return				JSON array of results
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json")
	public String list(
			@RequestParam(name = "start", defaultValue = "0") int start,
			@RequestParam(name = "length", defaultValue = "-1") int length,
			@RequestParam(name = "orderField", required = false) String orderField,
			@RequestParam(name = "orderDir", defaultValue = "ASC") String orderDir) {
				
		List<Preference> results = Preference.findPreferenceEntries(start, length, orderField, orderDir);
		return Preference.toJsonArrayPreference(results);
	}
	
	/**
	 * Returns JSON list of Preferences for use with DataTables
	 * @param request Request having DataTables arguments:
	 * 			draw, start, length, orderColumnName, ordirDir [, query]
	 * @param response
	 * @throws IOException
	 */
/*
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
				
				String results = Preference.generateDataTables(draw, start, length,
						orderColumnName, orderDir, query);
			
			    out.print(results);
	}
*/
	
	/**
	 * TODO: how to best accomplish this?
	 * Updates the Preference having the given ID
	 * @param uid	ID of Preference to update
	 * @param ugcid	ID of Preference to update
	 * @param preference	updated Preference
	 * @return		ID of updated Preference
	 *
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value = "/{uid}/{ugcid}", method = RequestMethod.PUT, produces = "application/json")
	public String update(@PathVariable("uid") long uid,
			@PathVariable("ugcid") long ugcid,
			@RequestBody Preference preference) {
/*
		if (preference.getId() != id) {
			return "ID error";
		}
*/		
		/*
		JsonArray errors = new JsonArray();
		if (errors.size() > 0) {
			return errors.toString();
		}
		*
        final Preference oldPreference = Preference.findPreference(preference.getPk());
        preference.setVersion(oldPreference.getVersion());
        preference.getPk().setStartTime(oldPreference.getPk().getStartTime());
//        preference.setStatus(oldPreference.getStatus());
		// merge and return id
		preference.merge();
		return preference.getPk().toString();
	}
	*/
	
	/**
	 * Returns JSON representation of a User's Preferences to a UGC
	 * @param uid	ID of User 
	 * @param ugcid	ID of UGC
	 * @return		JSON of Preference
	 */
	@RequestMapping(value = "/{uid}/{keyword}", method = RequestMethod.GET, produces = "application/json")
	public String view(@PathVariable("uid") long uid, @PathVariable("keyword") String keyword) {
		User user = User.findUser(uid);
		List<Preference> preferences = Preference.findPreferences(user, keyword).getResultList();
		if (preferences == null) {
			return "0";
		}
		return Preference.toJsonArray(preferences);
	}
}
