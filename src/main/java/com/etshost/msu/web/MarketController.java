package com.etshost.msu.web;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.etshost.msu.entity.Deal;
import com.etshost.msu.entity.Entity;
import com.etshost.msu.entity.Market;
import com.etshost.msu.entity.User;
import com.etshost.msu.entity.Viewing;

/**
 * Controller for the {@link com.etshost.msu.entity.Market} class.
 */
@RequestMapping("/markets")
@RestController
public class MarketController {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Market repository;
	
	/**
	 * Creates a new Market from the JSON description
	 * @param market	Market to create
	 * @return		ID of created Market
	 */
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
	public String create(@RequestBody Market market) {
		/*
		JsonArray errors = new JsonArray();
		if (errors.size() > 0) {
			return errors.toString();
		}
		*/

		// persist and return id
		market.persist();
		return market.getId().toString();
	}
	
	/**
	 * Returns JSON list of Markets
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
		if(length < 0) {
			return repository.findAllMarketsJson();
		} else {
			List<Market> results = Market.findMarketEntries(start, length, orderField, orderDir);
			return Market.toJsonArrayMarket(results);
		}
	}
	
	/**
	 * Returns JSON list of Markets for use with DataTables
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
				
				String results = Market.generateDataTables(draw, start, length,
						orderColumnName, orderDir, query);
			
			    out.print(results);
	}
	
	/**
	 * Returns JSON array of Markets
	 * TODO: define radius to improve performance
	 * 
	 * @return		JSON array of Markets
	 */
//	@RequestMapping(value = "/map", method = RequestMethod.GET, produces = "application/json")
	public String map(int draw) {
		List<Market> markets = Market.findAllMarkets();
		String mkts = Market.toJsonArrayMarket(markets);
		if (draw < 0) {
			return mkts;
		}
		return "{\"draw\":" + draw + ",\"markets\":" + mkts + "}";
	}
	
	@RequestMapping(value = "/map", method = RequestMethod.GET, produces = "application/json")
	public String map(@RequestParam(name = "draw", defaultValue = "-1") int draw,
			@RequestParam(name = "q", required = false) String q) {
		if (q == null || q.length() == 0) {
			return this.map(draw);
		}
		EntityManager em = Entity.entityManager();
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);

		this.logger.debug("searching Deals for: {}", q);

		// search deals
		QueryBuilder qb = fullTextEntityManager
				.getSearchFactory()
				.buildQueryBuilder()
				.forEntity(Deal.class)
				.get();
		org.apache.lucene.search.Query luceneQuery = qb
			    .keyword()
			    .fuzzy()
			    .onFields("title", "price", "text")
				.matching(q)
				.createQuery();
		
		// wrap Lucene query in a javax.persistence.Query
		javax.persistence.Query jpaQuery =
		    fullTextEntityManager.createFullTextQuery(luceneQuery, Deal.class);
//		this.logger.debug("jpaQuery: {}", jpaQuery.toString());
		

		// execute search
		this.logger.debug("results size: {}", jpaQuery.getResultList().size());
		List<?> result = jpaQuery.getResultList();
		
		// enumerate relevant markets
		Set<Market> markets = new HashSet<Market>();
		result.forEach(deal -> markets.add(((Deal) deal).getMarket()));
		
		this.logger.debug("searching Markets for: {}", q);

		// search markets
		qb = fullTextEntityManager
				.getSearchFactory()
				.buildQueryBuilder()
				.forEntity(Market.class)
				.get();
		luceneQuery = qb
			    .keyword()
			    .fuzzy()
			    .onField("name")
				.matching(q)
				.createQuery();
		
		// wrap Lucene query in a javax.persistence.Query
		jpaQuery =
		    fullTextEntityManager.createFullTextQuery(luceneQuery, Market.class);
//		this.logger.debug("jpaQuery: {}", jpaQuery.toString());
		

		// execute search
		this.logger.debug("results size: {}", jpaQuery.getResultList().size());
		result = jpaQuery.getResultList();
		
		result.forEach(market -> markets.add((Market) market));
		
		int count = markets.size();
		String mkts = Market.toJsonArrayMarket(markets);
		if (draw < 0) {
			return mkts;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("{\"draw\":");
		sb.append(draw);
		sb.append(",\"markets\":");
		sb.append(mkts);
		sb.append("}");
		this.logger.debug("returning {} markets", count);
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/search", method = RequestMethod.GET, produces = "application/json")
	public String search(@RequestParam(name = "draw") int draw,
			@RequestParam(name = "q", required = true) String q) {
		EntityManager em = Entity.entityManager();
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);

		this.logger.debug("searching Markets for: {}", q);

		QueryBuilder qb = fullTextEntityManager
				.getSearchFactory()
				.buildQueryBuilder()
				.forEntity(Market.class)
				.get();
		org.apache.lucene.search.Query luceneQuery = qb
			    .keyword()
			    .fuzzy()
			    .onField("name")
				.matching(q)
				.createQuery();
		
//		this.logger.debug("luceneQuery: {}", luceneQuery.toString());


		// wrap Lucene query in a javax.persistence.Query
		javax.persistence.Query jpaQuery =
		    fullTextEntityManager.createFullTextQuery(luceneQuery, Market.class);
//		this.logger.debug("jpaQuery: {}", jpaQuery.toString());
		

		// execute search
		this.logger.debug("results size: {}", jpaQuery.getResultList().size());
		List<?> result = jpaQuery.getResultList();
		return Market.toJsonArrayMarket((List<Market>)result);
	}
	
	/**
	 * Updates the Market having the given ID
	 * @param id	ID of Market to update
	 * @param market	updated Market
	 * @return		ID of updated Market
	 */
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value = "/{id}", method = RequestMethod.POST, produces = "application/json")
	public String update(@PathVariable("id") long id, @RequestBody Market market) {
		if (market == null) {
			return "Market not found";
		}
		if (market.getId() != id) {
			return "ID error";
		}
		/*
		JsonArray errors = new JsonArray();
		if (errors.size() > 0) {
			return errors.toString();
		}
		*/
        final Market oldMarket = Market.findMarket(market.getId());
        market.setVersion(oldMarket.getVersion());
        market.setCreated(oldMarket.getCreated());
        market.setStatus(oldMarket.getStatus());
		// merge and return id
        market.merge();
		return market.getId().toString();
	}
	
	/**
	 * Returns JSON representation of Market with the given ID
	 * @param id	ID of Market to view 
	 * @return		JSON of Market
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
	public String view(@PathVariable("id") long id) {
		Market market = Market.findMarket(id);
		if (market == null) {
			return "0";
		}

		new Viewing(User.getLoggedInUserId(), market.getId()).persist();
		return market.toJson();
	}
	
	/**
	 * Returns JSON representation of Market with the given ID
	 * @param id	ID of Market to view 
	 * @return		JSON of Market
	 */
	@RequestMapping(value = "/{id}/deals", method = RequestMethod.GET, produces = "application/json")
	public String getDeals(@PathVariable("id") long id) {
		Market market = Market.findMarket(id);
		if (market == null) {
			return "0";
		}
		List<Deal> deals = Deal.findLiveDealsByMarket(market).getResultList();
		return Deal.toJsonArrayDeal(deals);
	}
}
