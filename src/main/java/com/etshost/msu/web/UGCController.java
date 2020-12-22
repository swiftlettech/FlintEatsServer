package com.etshost.msu.web;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.etshost.msu.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.NamedStoredProcedureQuery;

/**
 * Controller for the {@link com.etshost.msu.entity.UGC} class.
 */
@RequestMapping("/ugc")
@RestController
public class 	UGCController {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	public String feed(int draw, int page) {
		
		try {
			PreferenceServerController preferenceServerController = new PreferenceServerController();
			User me = User.getLoggedInUser();
			String feed = preferenceServerController.getContentFeed(me, page, 10);
			if (feed == null) {
				throw new Exception("Feed is null");
			}
			return feed;
		} catch (Exception e) {
			this.logger.debug("Preference server failure: {}", e.toString());
		}
		
		
		this.logger.debug("Fetching feed, page {}", page);
		long size = UGC.countFeedUGCs();
		int start = Math.min(page * 10, (int)size);

		List<UGC> ugc = UGC.findAllFeedUGCs(start, 10);
//		ugc.sort((ugc1, ugc2) -> ugc2.getCreated().compareTo(ugc1.getCreated()));
		/*
		List<UGC> subList = new ArrayList<UGC>();
		try {
			int start = Math.min(page * 10, ugc.size());
			int end = Math.min((page + 1) * 10 - 1, ugc.size());
			subList = ugc.subList(start, end);
		} catch (IndexOutOfBoundsException e) {
			return "[]";
		}
		*/
		int length = ugc.size();
		this.logger.debug("serializing {} items", length);
		String ugcJson = UGC.toJsonArrayUGC(ugc);
		if (draw < 0) {
			this.logger.debug("{}",draw);
			this.logger.debug("returning {} items", length);
			return ugcJson;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("{\"draw\":");
		sb.append(draw);
		sb.append(",\"feed\":");
		sb.append(ugcJson);
		sb.append("}");
		this.logger.debug("returning {} items", length);
		return sb.toString();
	}
	
	@RequestMapping(value = "/feed", method = RequestMethod.GET, produces = "application/json")
	public String feed(
			@RequestParam(name = "q", required = false) String q,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name= "length", defaultValue = "10") int length,
			@RequestParam(name = "kind", required = false) List<String> types,
			@RequestParam(name="tags",required = false) List<String> tag_ids,
			@RequestParam(name="markets",required = false) List<String> market_ids)
	{

		List<Market> markets = new ArrayList<Market>();
		List<Tag> tags = new ArrayList<Tag>();
		if (market_ids!=null) {
			market_ids.forEach(id -> {
				try {
					Market m = Market.findMarket(Long.parseLong(id));
					if (m != null) markets.add(m);
				} catch (NumberFormatException n) {
					this.logger.debug("Invalid market id: "+id);
				}
			});
			this.logger.debug("Found markets: "+markets);
		}

		if (tag_ids!=null) {
			tag_ids.forEach(id -> {
				try {
					Tag t = Tag.findTag(Long.parseLong(id));
					if (t != null) tags.add(t);
				} catch (NumberFormatException n) {
					this.logger.debug("Invalid tag id: "+id);
				}
			});
			this.logger.debug("Found tags: "+tags);
		}


		Set<UGC> ugcResultSet = new HashSet<UGC>();


		if (types==null || types.contains("deal")) {
			if (q!=null) {
				ugcResultSet.addAll(Deal.search(q));
			} else {
				ugcResultSet.addAll(Deal.findAllDeals());
			}

			if (!markets.isEmpty()) {
				ugcResultSet.removeIf(d -> !markets.contains(((Deal)d).getMarket()));
			}
		}

		if (types==null || types.contains("tip")) {
			if (q!=null) {
				ugcResultSet.addAll(Tip.search(q));
			} else {
				ugcResultSet.addAll(Tip.findAllTips());
			}
		}

		if (!tags.isEmpty()) {
			ugcResultSet.removeIf(d -> {
				Set<Tag> hastags = new HashSet<>(d.getTags());
				hastags.retainAll(tags);
				return hastags.isEmpty();
			});
		}

		List<UGC> ugcResultList = new ArrayList<UGC>(ugcResultSet);
		//TODO: sort this differently?
		ugcResultList.sort((ugc1, ugc2) -> ugc2.getCreated().compareTo(ugc1.getCreated()));
		
		List<UGC> subList;
		try {
			int start = Math.min(page * length, ugcResultList.size());
			int end = Math.min((page + 1) * length - 1, ugcResultList.size());
			subList = ugcResultList.subList(start, end);
		} catch (IndexOutOfBoundsException e) {
			return "[]";
		}

		this.logger.debug("serializing {} items", length);
		String ugcJson = UGC.toJsonArrayUGC(subList);

		return ugcJson;

		//return sb.toString();

	}
	
	@RequestMapping(value = "/ilike/{id}", method = RequestMethod.GET, produces = "application/json")
	public boolean iLike(@PathVariable("id") long id) {
		UGC ugc = UGC.findUGC(id);
		if (ugc != null) {
			return ugc.getILike();
		}
		return false;
	}
	
	@RequestMapping(value = "/recent", method = RequestMethod.GET, produces = "application/json")
	public String recent(@RequestParam(name = "epoch") long epoch) {
		Instant then = Instant.ofEpochSecond(epoch);
		List<UGC> content = UGC.findRecentUGCs(then);
		return UGC.toJsonArrayUGC(content);
	}

	
	@RequestMapping(value = "/feed/{id}/{fave}", method = RequestMethod.GET, produces = "application/json")
	public String userFeed(@PathVariable("id") long id, @PathVariable("fave") boolean faves,
						   @RequestParam(name = "page", required = false) int page) {
		User user = User.findUser(id);
		if (user == null) {
			return "[]";
		}
		if (faves) {
			return UGC.toJsonArrayUGC(user.getFaves());
		}
		List<UGC> ugcList = UGC.findAllFeedUGCs(user);
		return UGC.toJsonArrayUGC(ugcList);
	}
}
