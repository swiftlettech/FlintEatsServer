package com.etshost.msu.web;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.etshost.msu.bean.IndexedUGCBean;
import com.etshost.msu.bean.ReviewBean;
import com.etshost.msu.bean.UGCCreatorCheck;
import com.etshost.msu.entity.Entity;
import com.etshost.msu.entity.Market;
import com.etshost.msu.entity.Review;
import com.etshost.msu.entity.ReviewProperty;
import com.etshost.msu.entity.Tag;
import com.etshost.msu.entity.User;
import com.etshost.msu.entity.Viewing;

/**
 * Controller for the {@link com.etshost.msu.entity.Review} class.
 */
@RequestMapping("/ugc/reviews")
@RestController
@Transactional
public class ReviewController {
	@Autowired
	protected UGCCreatorCheck creatorChecker;

	@Autowired
	private Market marketRepository;

	/**
	 * Creates a new Review from the JSON description
	 * @param review	Review to create
	 * @return		ID of created Review
	 */
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(value = "/create", method = RequestMethod.POST, consumes="application/json", produces = "application/json")
	public String create(@RequestBody Review review) {
		review.setUsr(User.getLoggedInUser());
		
		// Set parent on properties
		review.getProperties().forEach(prop -> {
			prop.setReview(review);
		});

		// persist and return id
		review.persist();
		marketRepository.backgroundRefreshMarketCache();
		return review.getId().toString();
	}
	
	/**
	 * Returns JSON list of Reviews
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
				
		List<Review> results = Review.findReviewEntries(start, length, orderField, orderDir);
		return Review.toJsonArray(results);
	}
	
	/**
	 * Returns JSON list of Reviews for use with DataTables
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
				
				String results = Review.generateDataTables(draw, start, length,
						orderColumnName, orderDir, query);
			
			    out.print(results);
	}
	
	/**
	 * Updates the Review having the given ID
	 * @param id	ID of Review to update
	 * @param review	updated Review
	 * @return		ID of updated Review
	 */
    @PreAuthorize("@creatorChecker.check(#id)")
	@RequestMapping(value = "/{id}", method = RequestMethod.POST, produces = "application/json")
	public String update(@PathVariable("id") long id, @RequestBody ReviewBean review) {
		if (review.getId() != id || Review.findReview(review.getId())==null) {
			return "ID error";
		}

		final Review oldReview = Review.findReview(review.getId());
		ReviewProperty.deleteReviewPropertiesByParent(oldReview);

		if (review.getText() != null && !review.getText().equals(oldReview.getText())) {
			oldReview.setText(review.getText());
		}
		if (review.getTargetId() != null && !review.getTargetId().equals(oldReview.getTarget().getId())) {
			oldReview.setTargetById(review.getTargetId());
		}
		if (review.getProperties() != null) {
			review.getProperties().forEach(prop -> {
				prop.setReviewById(review.id);
			});
			oldReview.setProperties(review.getProperties());
		}
		if (review.getTags() != null) {
			Set<Tag> tags = new HashSet<Tag>();
			for (IndexedUGCBean u : review.getTags()) {
				tags.add(Tag.findTag(u.id));
			}
			if (!tags.equals(oldReview.getTags())) {
				oldReview.setTags(tags);
			}
		}

		oldReview.setModified(Instant.now());
		oldReview.persist();

		marketRepository.backgroundRefreshMarketCache();

		return review.getId().toString();
	}
	
	/**
	 * Returns JSON representation of Review with the given ID
	 * @param id	ID of Review to view 
	 * @return		JSON of Review
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
	public String view(@PathVariable("id") long id) {
		Review review = Review.findReview(id);
		if (review == null) {
			return "0";
		}

		new Viewing(User.getLoggedInUserId(), review.getId()).persist();
		return review.toJson();
	}
	
	/**
	 * Returns Reviews targeted to the given ID
	 * @param targetId	ID of UGC under review 
	 * @return			JSON of Reviews
	 */
	@RequestMapping(value = "/target/{targetId}", method = RequestMethod.GET, produces = "application/json")
	public String byTarget(@PathVariable("targetId") long targetId) {
		Entity target = Entity.findEntity(targetId);
		List<Review> reviews = Review.findReviewsByTarget(target);
		if (reviews == null) {
			return "[]";
		}
		return Review.toJsonArray(reviews);
	}
}
