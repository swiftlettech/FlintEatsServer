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

import com.etshost.msu.entity.Comment;
import com.etshost.msu.entity.Entity;
import com.etshost.msu.entity.User;

/**
 * Controller for the {@link com.etshost.msu.entity.Comment} class.
 */
@RequestMapping("/ugc/comments")
@RestController
public class CommentController {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Creates a new Comment from the JSON description
	 * @param comment	Comment to create
	 * @return		ID of created Comment
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
	public String create(@RequestBody Comment comment) {

		comment.setUsr(User.getLoggedInUser());
		/*
		JsonArray errors = new JsonArray();
		if (errors.size() > 0) {
			return errors.toString();
		}
		*/

		// persist and return id
		comment.persist();
		return comment.getId().toString();
	}

	/**
	 * Returns JSON list of Comments
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
				
		List<Comment> results = Comment.findCommentEntries(start, length, orderField, orderDir);
		return Comment.toJsonArray(results);
	}
	
	/**
	 * Returns JSON list of Comments targeting the specified UGC
	 * @param target		id of targeted UGC
	 * @return				JSON array of results
	 */
	@RequestMapping(value = "/list/{id}", method = RequestMethod.GET, produces = "application/json")
	public String list(@PathVariable(name = "id") long target) {
		Entity entity = Entity.findEntity(target);
		List<Comment> comments = new ArrayList<Comment>();
		comments.addAll(entity.getComments());
		comments.sort((c1, c2) -> c1.getCreated().compareTo(c2.getCreated()));
		return Comment.toJsonArray(comments);
	}
	
	/**
	 * Returns JSON list of Comments for use with DataTables
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
				
				String results = Comment.generateDataTables(draw, start, length,
						orderColumnName, orderDir, query);
			
			    out.print(results);
	}
	
	/**
	 * Updates the Comment having the given ID
	 * @param id	ID of Comment to update
	 * @param comment	updated Comment
	 * @return		ID of updated Comment
	 */
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = "application/json")
	public String update(@PathVariable("id") long id, @RequestBody Comment comment) {
		if (comment.getId() != id) {
			return "ID error";
		}
		
		/*
		JsonArray errors = new JsonArray();
		if (errors.size() > 0) {
			return errors.toString();
		}
		*/
        final Comment oldComment = Comment.findComment(comment.getId());
        comment.setVersion(oldComment.getVersion());
        comment.setCreated(oldComment.getCreated());
        comment.setStatus(oldComment.getStatus());
		// merge and return id
		comment.merge();
		return comment.getId().toString();
	}
	
	/**
	 * Returns JSON representation of Comment with the given ID
	 * @param id	ID of Comment to view 
	 * @return		JSON of Comment
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
	public String view(@PathVariable("id") long id) {
		Comment comment = Comment.findComment(id);
		if (comment == null) {
			return "0";
		}
		return comment.toJson();
	}
}
