package com.etshost.msu.web;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.etshost.msu.entity.Policy;

/**
 * Controller for the {@link com.etshost.msu.entity.Policy} class.
 */
@RequestMapping("/policies")
@RestController
public class PolicyController {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Creates a new Policy from the JSON description
	 * @param policy	Policy to create
	 * @return		ID of created Policy
	 */
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
	public String create(@RequestBody Policy policy) {
		// persist and return id
		policy.persist();
		return policy.getId().toString();
	}
	
	/**
	 * Returns JSON list of Policys
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
		List<Policy> results = Policy.findPolicyEntries(start, length, orderField, orderDir);
		return Policy.toJsonArray(results);
	}
	
	/**
	 * Returns JSON list of Policys for use with DataTables
	 * @param request Request having DataTables arguments:
	 * 			draw, start, length, orderColumnName, ordirDir [, query]
	 * @param response
	 * @throws IOException
	 */
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
				try {
					draw = Integer.valueOf(request.getParameter("draw"));
					start = Integer.valueOf(request.getParameter("start"));
					length = Integer.valueOf(request.getParameter("length"));	
					orderColumn = request.getParameter("order[0][column]");
					orderColumnName = request.getParameter("columns["+orderColumn+"][name]");
					orderDir = request.getParameter("order[0][dir]");
				} catch (Exception e) {
					error.add(e.toString());
				}
				
				String results = Policy.generateDataTables(draw, start, length,
						orderColumnName, orderDir);
			
			    out.print(results);
	}
	
	/**
	 * Updates the Policy having the given name
	 * @param name	Name of Policy to update
	 * @param policy	updated Policy
	 * @return		ID of updated Policy
	 */
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value = "/{name}", method = RequestMethod.POST, produces = "application/json")
	public String update(@PathVariable("name") String name, @RequestBody Policy policy) {
		if (policy == null) {
			return "Policy not found";
		}

        policy.merge();
		return policy.getId().toString();
	}
	
	/**
	 * Returns JSON representation of Policy with the given name
	 * @param name	Name of Policy to view 
	 * @return		JSON of Policy
	 */
	@RequestMapping(value = "/{name}", method = RequestMethod.GET, produces = "application/json")
	public String view(@PathVariable("name") String name) {
		Policy policy = Policy.findPolicy(name);
		return policy.toJson();
	}
	
}
