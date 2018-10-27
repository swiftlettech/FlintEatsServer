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

import com.etshost.msu.entity.Reaction;
import com.etshost.msu.entity.UGC;
import com.etshost.msu.entity.User;

/**
 * Controller for the {@link com.etshost.msu.entity.Reaction} class.
 */
@RequestMapping("/reactions")
@RestController
public class ReactionController {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Creates a new Reaction from the JSON description
	 * @param reaction	Reaction to create
	 * @return		hashcode of created Reaction
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
	public String create(@RequestBody Reaction reaction) {
		User user = User.getLoggedInUser();
		// "clear" prior reactions
		List<Reaction> reacts = Reaction.findReactions(user, reaction.getTarget()).getResultList();
		for (Reaction react : reacts) {
			if (react.getEndTime() == null) {
				// if active reaction
				if (react.getValue() == reaction.getValue()) {
					// if same value, disable and return 0
					react.setEndTime(Instant.now());
					react.merge();
					return "0";
				}
				// else just disable
				react.setEndTime(Instant.now());
				react.merge();
			}
		}
		// persist and return hashcode
		reaction.setUsr(user);
		reaction.persist();

		return String.valueOf(reaction.getPk().getTargetId());
	}
	
	/**
	 * Returns JSON list of Reactions
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
				
		List<Reaction> results = Reaction.findReactionEntries(start, length, orderField, orderDir);
		return Reaction.toJsonArray(results);
	}
	
	@RequestMapping(value = "/recent", method = RequestMethod.GET, produces = "application/json")
	public String recent(@RequestParam(name = "epoch") long epoch) {
		Instant then = Instant.ofEpochSecond(epoch);
		List<Reaction> reactions = Reaction.findRecentReactions(then);
		return Reaction.toJsonArray(reactions);
	}
	
	/**
	 * Returns JSON list of Reactions for use with DataTables
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
				
				String results = Reaction.generateDataTables(draw, start, length,
						orderColumnName, orderDir, query);
			
			    out.print(results);
	}
*/
	
	/**
	 * TODO: how to best accomplish this?
	 * Updates the Reaction having the given ID
	 * @param uid	ID of Reaction to update
	 * @param ugcid	ID of Reaction to update
	 * @param reaction	updated Reaction
	 * @return		ID of updated Reaction
	 *
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value = "/{uid}/{ugcid}", method = RequestMethod.PUT, produces = "application/json")
	public String update(@PathVariable("uid") long uid,
			@PathVariable("ugcid") long ugcid,
			@RequestBody Reaction reaction) {
/*
		if (reaction.getId() != id) {
			return "ID error";
		}
*/		
		/*
		JsonArray errors = new JsonArray();
		if (errors.size() > 0) {
			return errors.toString();
		}
		*
        final Reaction oldReaction = Reaction.findReaction(reaction.getPk());
        reaction.setVersion(oldReaction.getVersion());
        reaction.getPk().setStartTime(oldReaction.getPk().getStartTime());
//        reaction.setStatus(oldReaction.getStatus());
		// merge and return id
		reaction.merge();
		return reaction.getPk().toString();
	}
	*/
	
	/**
	 * Returns JSON representation of a User's Reactions to a UGC
	 * @param uid	ID of User 
	 * @param ugcid	ID of UGC
	 * @return		JSON of Reaction
	 */
	@RequestMapping(value = "/{uid}/{ugcid}", method = RequestMethod.GET, produces = "application/json")
	public String view(@PathVariable("uid") long uid, @PathVariable("ugcid") long ugcid) {
		User user = User.findUser(uid);
		UGC ugc = UGC.findUGC(ugcid);
		List<Reaction> reactions = Reaction.findReactions(user, ugc).getResultList();
		if (reactions == null) {
			return "0";
		}
		return Reaction.toJsonArray(reactions);
	}
}
