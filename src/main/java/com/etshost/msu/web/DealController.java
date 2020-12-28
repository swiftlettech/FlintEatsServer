package com.etshost.msu.web;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.etshost.msu.entity.Entity;
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

import com.etshost.msu.entity.Deal;
import com.etshost.msu.entity.User;
import com.etshost.msu.entity.Viewing;

/**
 * Controller for the {@link com.etshost.msu.entity.Deal} class.
 */
@RequestMapping("/ugc/deals")
@RestController
@Transactional
public class DealController {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Creates a new Deal from the JSON description
	 * @param deal	Deal to create
	 * @return		ID of created Deal
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
	public String create(@RequestBody Deal deal) {
		this.logger.debug("landed at /ugc/deals/create");
		
		deal.setUsr(User.getLoggedInUser());
		/*
		JsonArray errors = new JsonArray();
		if (errors.size() > 0) {
			return errors.toString();
		}
		*/

		// persist and return id
		deal.persist();
		return deal.getId().toString();
	}
	
	/**
	 * Returns JSON list of Deals
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
				
		List<Deal> results = Deal.findDealEntries(start, length, orderField, orderDir);
		return Deal.toJsonArray(results);
	}
	
	/**
	 * Returns JSON list of Deals for use with DataTables
	 * @param request Request having DataTables arguments:
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
				
				String results = Deal.generateDataTables(draw, start, length,
						orderColumnName, orderDir, query);
			
			    out.print(results);
	}
	
	/**
	 * Updates the Deal having the given ID
	 * @param id	ID of Deal to update
	 * @param deal	updated Deal
	 * @return		ID of updated Deal
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.POST, produces = "application/json")
	public String update(@PathVariable("id") long id, @RequestBody Deal deal) {
		if (deal.getId() != id) {
			return "ID error";
		}
		
		User currentUser = User.getLoggedInUser();
        Deal oldDeal = Deal.findDeal(id);
		Logger logger = LoggerFactory.getLogger(User.class);
		logger.debug("Old deal User: {}", oldDeal.getUsr());
		if(oldDeal.getUsr().getId() != currentUser.getId() && !currentUser.admin()) {
			return "No Permission";
		}

        deal.setVersion(oldDeal.getVersion());
        deal.setCreated(oldDeal.getCreated());
        deal.setStatus(oldDeal.getStatus());
        deal.setUsr(oldDeal.getUsr());
		// merge and return id
		deal.merge();
		return deal.getId().toString();
	}
	
	/**
	 * Returns JSON representation of Deal with the given ID
	 * @param id	ID of Deal to view 
	 * @return		JSON of Deal
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
	public String view(@PathVariable("id") long id) {
		Deal deal = Deal.findDeal(id);
		if (deal == null) {
			return "0";
		}
		User user = User.getLoggedInUser();
		new Viewing(user, deal).persist();
		deal.merge(); // update updated time
		return deal.toJson();
	}
	
	@RequestMapping(value = "/search", method = RequestMethod.GET, produces = "application/json")
	public String search(@RequestParam(name = "q", required = true) String q) {
		return Deal.toJsonArrayDeal(Deal.search(q));
	}
}
