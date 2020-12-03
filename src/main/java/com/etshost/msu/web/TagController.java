package com.etshost.msu.web;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.etshost.msu.entity.Entity;
import com.etshost.msu.entity.Tag;
import com.etshost.msu.entity.User;

/**
 * Controller for the {@link com.etshost.msu.entity.Tag} class.
 */
@RequestMapping("/tags")
@RestController
public class TagController {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Tags the target with the given tag
	 * @param tag	Tag to add
	 * @param id	ID of target
	 * @return		ID of Tag
	 */
	@RequestMapping(value = "/add/{id}", method = RequestMethod.POST, produces = "application/json")
	public String add(@RequestBody long tagId, @PathVariable("id") long id) {
		Entity e = Entity.findEntity(id);
		Tag tag = Tag.findTag(tagId);
		e.getTags().add(tag);
		e.merge();
		tag.setUsr(User.getLoggedInUser());
		return tag.getId().toString();
	}
	
	/**
	 * Creates a new Tag from the JSON description
	 * @param tag	Tag to create
	 * @return		ID of created Tag
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
	public String create(@RequestBody Tag tag) {
		
		tag.setUsr(User.getLoggedInUser());
		/*
		JsonArray errors = new JsonArray();
		if (errors.size() > 0) {
			return errors.toString();
		}
		*/

		// persist and return id
		tag.persist();
		return tag.getId().toString();
	}
	
	/**
	 * Returns JSON list of Tags
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
			@RequestParam(name = "orderField", defaultValue = "name") String orderField,
			@RequestParam(name = "orderDir", defaultValue = "ASC") String orderDir) {
				
		this.logger.debug("retrieving tag list...");
		List<Tag> results = Tag.findTagEntries(start, length, orderField, orderDir);
		this.logger.debug("results: {}", results.size());
		return Tag.toJsonArray(results);
	}
	
	/**
	 * Returns JSON list of Tags targeting the specified Entity
	 * @param target		id of targeted Entity
	 * @return				JSON array of results
	 */
	@RequestMapping(value = "/list/{id}", method = RequestMethod.GET, produces = "application/json")
	public String list(@PathVariable(name = "id") long target) {
		this.logger.debug("retrieving tag list for id {}", target);

		Entity entity = Entity.findEntity(target);
		List<Tag> tags = new ArrayList<Tag>();
		tags.addAll(entity.getTags());		
		tags.sort((c1, c2) -> c1.getName().compareTo(c2.getName()));
		return Tag.toJsonArrayTag(tags);
	}
	
	/**
	 * Returns JSON list of Tags starting with the given prefix
	 * @param letter		Beginning letter of Tags to search
	 * @return				JSON array of results
	 */
	@RequestMapping(value = "/sublist/{letter}", method = RequestMethod.GET, produces = "application/json")
	public String list(@PathVariable(name = "letter") String letter) {
		// make sure arg is not an id
		if (NumberUtils.isNumber(letter)) {
			Long id = Long.parseLong(letter);
			if (id > 0) {
				return this.list(id);
			}
			
		}
		
		List<Tag> tags = Tag.findTagsByPrefix(letter).getResultList();
		tags.sort((t1, t2) -> t1.getName().compareTo(t2.getName()));
		return Tag.toJsonArray(tags);
	}
	
	/**
	 * Returns JSON list of Tags for use with DataTables
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
				
				String results = Tag.generateDataTables(draw, start, length,
						orderColumnName, orderDir, query);
			
			    out.print(results);
	}
	
	/**
	 * Updates the Tag having the given ID
	 * @param id	ID of Tag to update
	 * @param tag	updated Tag
	 * @return		ID of updated Tag
	 */
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = "application/json")
	public String update(@PathVariable("id") long id, @RequestBody Tag tag) {
		if (tag.getId() != id) {
			return "ID error";
		}
		
		/*
		JsonArray errors = new JsonArray();
		if (errors.size() > 0) {
			return errors.toString();
		}
		*/
        final Tag oldTag = Tag.findTag(tag.getId());
        tag.setVersion(oldTag.getVersion());
        tag.setCreated(oldTag.getCreated());
		// merge and return id
		tag.merge();
		return tag.getId().toString();
	}
	
	/**
	 * Returns JSON representation of Tag with the given ID
	 * @param id	ID of Tag to view 
	 * @return		JSON of Tag
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
	public String view(@PathVariable("id") long id) {
		Tag review = Tag.findTag(id);
		if (review == null) {
			return "0";
		}
		return review.toJson();
	}
	
	
	
	
	@RequestMapping(value = "/index", method = RequestMethod.GET, produces = "application/json")
	public void index() {
		EntityManager em = Entity.entityManager();
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
		try {
			fullTextEntityManager.createIndexer().startAndWait();
		} catch (InterruptedException e) {
			this.logger.debug(e.toString());
		}
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/search", method = RequestMethod.GET, produces = "application/json")
	public String search(@RequestParam(name = "q", required = true) String q) {
		EntityManager em = Entity.entityManager();
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);

		this.logger.debug("searching for: {}", q);

		QueryBuilder qb = fullTextEntityManager
				.getSearchFactory()
				.buildQueryBuilder()
				.forEntity(Tag.class)
				.get();
		org.apache.lucene.search.Query luceneQuery = qb
			    .keyword()
			    .fuzzy()
			    .onField("name")
				.matching(q)
				.createQuery();
		
		this.logger.debug("luceneQuery: {}", luceneQuery.toString());


		// wrap Lucene query in a javax.persistence.Query
		javax.persistence.Query jpaQuery =
		    fullTextEntityManager.createFullTextQuery(luceneQuery, Tag.class);
		this.logger.debug("jpaQuery: {}", jpaQuery.toString());
		

		// execute search
		this.logger.debug("results size: {}", jpaQuery.getResultList().size());
		List<?> result = jpaQuery.getResultList();
		if (result.isEmpty()) {
			return "[]";
		}
		return Tag.toJsonArrayTag((List<Tag>)result);
	}
}
