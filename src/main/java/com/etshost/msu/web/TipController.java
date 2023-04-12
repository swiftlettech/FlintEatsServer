package com.etshost.msu.web;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.etshost.msu.bean.DealBean;
import com.etshost.msu.bean.IndexedUGCBean;
import com.etshost.msu.bean.TipBean;
import com.etshost.msu.bean.UGCCreatorCheck;
import com.etshost.msu.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the {@link com.etshost.msu.entity.Tip} class.
 */
@RequestMapping("/ugc/tips")
@RestController
@Transactional
public class TipController {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	protected UGCCreatorCheck creatorChecker;

	/**
	 * Creates a new Tip from the JSON description
	 * @param tip	Tip to create
	 * @return		ID of created Tip
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
	public String create(@RequestBody Tip tip) {
		this.logger.debug("landed at /ugc/tips/create");

		tip.setUsr(User.getLoggedInUser());
		/*
		JsonArray errors = new JsonArray();
		if (errors.size() > 0) {
			return errors.toString();
		}
		*/

		// persist and return id
		tip.persist();
		return tip.getId().toString();
	}
	
	/**
	 * Returns JSON list of Tips
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
				
		List<Tip> results = Tip.findTipEntries(start, length, orderField, orderDir);
		return Tip.toJsonArray(results);
	}
	
	/**
	 * Returns JSON list of Tips for use with DataTables
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
				
				String results = Tip.generateDataTables(draw, start, length,
						orderColumnName, orderDir, query);
			
			    out.print(results);
	}
	
	/**
	 * Updates the Tip having the given ID
	 * @param id	ID of Tip to update
	 * @param tip	updated Tip
	 * @return		ID of updated Tip
	 */
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = "application/json")
	public String update(@PathVariable("id") long id, @RequestBody Tip tip) {
		if (tip.getId() != id) {
			return "ID error";
		}
		
		/*
		JsonArray errors = new JsonArray();
		if (errors.size() > 0) {
			return errors.toString();
		}
		*/
        final Tip oldTip = Tip.findTip(tip.getId());
        tip.setVersion(oldTip.getVersion());
        tip.setCreated(oldTip.getCreated());
		// merge and return id
		tip.merge();
		return tip.getId().toString();
	}
	
	/**
	 * Returns JSON representation of Tip with the given ID
	 * @param id	ID of Tip to view 
	 * @return		JSON of Tip
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
	public String view(@PathVariable("id") long id) {
		Tip tip = Tip.findTip(id);
		if (tip == null) {
			return "0";
		}
		new Viewing(User.getLoggedInUser(), tip.getId()).persist();
		// tip.merge(); //update updated time
		return tip.toJson();
	}

	@PreAuthorize("@creatorChecker.check(#id)")
	@RequestMapping(value = "/{id}", method = RequestMethod.POST, produces = "application/json")
	public String update(@PathVariable("id") long id, @RequestBody TipBean tip) {
		if (tip.getId() != id || Tip.findTip(tip.getId())==null) {
			return "ID error";
		}

		final Tip oldTip =Tip.findTip(tip.getId());
		Tip.TipType ntype = null;

		if (tip.getTipType() != null) {

			try {
				ntype = Tip.TipType.valueOf(tip.getTipType().toUpperCase());
			} catch (IllegalArgumentException | NullPointerException e) {
				this.logger.error(e.toString());
			}
		}
		if (ntype != null && !ntype.equals(oldTip.getTipType())) {
			oldTip.setTipType(ntype);
		}

		if (tip.getText() != null && !tip.getText().equals(oldTip.getText())) {
			oldTip.setText(tip.getText());
		}

		if (tip.getImage() != null && !Arrays.equals(tip.getImage(),oldTip.getImage())) {
			oldTip.setImage(tip.getImage());
		}

		if (tip.getTags() != null) {
			Set<Tag> tags = new HashSet<Tag>();
			for (IndexedUGCBean u : tip.getTags()) {
				tags.add(Tag.findTag(u.id));
			}
			if (!tags.equals(oldTip.getTags())) {
				oldTip.setTags(tags);
			}
		}

		oldTip.setModified(Instant.now());
		oldTip.persist();
		return tip.getId().toString();
	}
	
	@RequestMapping(value = "/search", method = RequestMethod.GET, produces = "application/json")
	public String search(@RequestParam(name = "q", required = true) String q) {
		return Tip.toJsonArrayTip(Tip.search(q));
	}
}
