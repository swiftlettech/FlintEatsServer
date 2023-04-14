package com.etshost.msu.web;
import java.time.Instant;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.etshost.msu.entity.Entity;
import com.etshost.msu.entity.User;
import com.etshost.msu.entity.Viewing;

/**
 * Controller for the {@link com.etshost.msu.entity.Viewing} class.
 */
@RequestMapping("/viewings")
@RestController
@Transactional
public class ViewingController {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping(value = "/open", method = RequestMethod.POST, produces = "application/json")
	public String open(@RequestBody long id) {
		this.logger.debug("open view of {}", id);
		Entity e = Entity.findEntity(id);
		User user = User.getLoggedInUser();
		Viewing viewing = new Viewing(user.getId(), e.getId());
		viewing.persist();
		return String.valueOf(viewing.getTargetId());
	}
	
	@RequestMapping(value = "/close", method = RequestMethod.POST, produces = "application/json")
	public String close(@RequestBody long id) {
		this.logger.debug("close view of {}", id);
		Entity e = Entity.findEntity(id);
		User user = User.getLoggedInUser();
		List<Viewing> viewings = Viewing.findViewings(user, e).getResultList();
		if (viewings.isEmpty()) {
			return "0";
		}
		Viewing viewing = viewings.get(0);
		viewing.setEndTime(Instant.now());
		viewing.merge();
		return String.valueOf(viewing.getTargetId());
	}
	
	@RequestMapping(value = "/recent", method = RequestMethod.GET, produces = "application/json")
	public String recent(@RequestParam(name = "epoch") long epoch) {
		Instant then = Instant.ofEpochSecond(epoch);
		List<Viewing> reactions = Viewing.findRecentViewings(then);
		return Viewing.toJsonArray(reactions);
	}
}
