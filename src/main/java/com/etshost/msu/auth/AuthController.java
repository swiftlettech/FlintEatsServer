package com.etshost.msu.auth;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.etshost.msu.bean.BASE64DecodedMultipartFile;
import com.etshost.msu.bean.UserDetailsServiceImpl;
import com.etshost.msu.entity.Deal;
import com.etshost.msu.entity.Entity;
import com.etshost.msu.entity.Market;
import com.etshost.msu.entity.Policy;
import com.etshost.msu.entity.Recipe;
import com.etshost.msu.entity.RecipeStep;
import com.etshost.msu.entity.Review;
import com.etshost.msu.entity.Role;
import com.etshost.msu.entity.Tag;
import com.etshost.msu.entity.Tip;
import com.etshost.msu.entity.User;
import com.etshost.msu.service.ImageStorageService;

/**
 * Controller to handle logins based on whether successful.
 */
@RequestMapping("/auth")
@RestController
public class AuthController {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Handler successful login
	 * @param request
	 * @param response
	 * @return JSESSIONID
	 */
	@RequestMapping(value = "/success", method = RequestMethod.GET, produces = "application/json")
	public String loginSuccess(HttpServletRequest request, HttpServletResponse response) {
		return RequestContextHolder.currentRequestAttributes().getSessionId();
	}
	
	/**
	 * Handler failed login
	 * @param request
	 * @param response
	 * @return 0
	 */
	@RequestMapping(value = "/failure", method = RequestMethod.GET, produces = "application/json")
	public String loginFailure(HttpServletRequest request, HttpServletResponse response) {
		return "0";
	}
	
	/**
	 * Destroys user session
	 * @return 0
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.GET, produces = "application/json")
	public int logout() {
		ServletRequestAttributes attr =
				(ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpSession session = attr.getRequest().getSession();
		session.invalidate();
		
		// not sure if this does anything
		SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
		
		return 0;
	}
	
	/**
	 * Creates a new User from the JSON description
	 * @param user	User to create
	 * @return		ID of created User
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
	public String create(@RequestBody User user) {
		if (user.getUsername() == null) {
			return "[\"Username invalid.\"]";
		}
		if ((user.getEmail() == null || user.getEmail().equals(""))
				&& (user.getPhone() == null || user.getPhone().equals(""))) {
			return "[\"You must provide an email or a phone number.\"]";
		}
		if (!user.getUsername().equals("")) {
			List<User> u = User
					.findUsersByUsernameEqualsNoCase(user.getUsername())
							.getResultList();
			if (u.size() > 0) {
				if (!(u.size() == 1 && u.get(0).equals(user))) {
					// if not only self matches
					return "[\"Username already in use.\"]";
				}
			}
		} else {
			return "[\"Username not valid.\"]";
		}
		
		if (user.getEmail() == null) {
			return "[\"Email invalid.\"]";
		}
		if (!user.getEmail().equals("")) {
			List<User> u = User
					.findUsersByEmailEqualsNoCase(user.getEmail())
							.getResultList();
			if (u.size() > 0) {
				if (!(u.size() == 1 && u.get(0).equals(user))) {
					// if not only self matches
					return "[\"Email already in use.\"]";
				}
			}
		} else {
			return "[\"Email not valid.\"]";
		}
		
		if (!user.getPhone()
				.matches("^(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$")) {
			return "[\"Phone number not valid.\"]";
		}

		user.persist();
		
		//perform login
		UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl();
		UserDetails userDetails = null;
		try {
			userDetails = userDetailsService.loadUserByUsername(user.getUsername());
		} catch (Exception e) {
			this.logger.debug(e.toString());
		}
		if (userDetails == null) {
			this.logger.debug("user is null: {}", user.getUsername());
			return null;
		}

		Authentication authToken =
				new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
														userDetails.getPassword(),
														userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authToken);
		
		// return user id
		return user.getId().toString();
	}
	
	/**
	 * For testing purposes.
	 * XXX: REMOVE FOR PRODUCTION
	 * @exclude
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/init", method = RequestMethod.GET, produces = "application/json")
	public String init(HttpServletRequest request, HttpServletResponse response) {
//		Role admin = new Role();
//		admin.setName("admin");
//		admin.persist();
//		Role login = new Role();
//		login.setName("login");
//		login.persist();

		Role admin = Role.findRoleByName("admin");
		Role login = Role.findRoleByName("login");


		Set<Role> roles = new HashSet<Role>();
		roles.add(admin);
		roles.add(login);

		User user = new User();
		user.setUsername("admin2");
			user.setEmail("admin2@flinteats.org");
		user.setPassword("lji123");
		user.setRoles(roles);
		user.persist();



//		User user = new User();
//		user.setUsername("admin");
//		user.setEmail("admin@etshost.com");
//		user.setPassword("EtS2437!");
//		user.setRoles(roles);
//		user.persist();
//
//		User user2 = new User();
//		user2.setUsername("msu");
//		user2.setEmail("msu@etshost.com");
//		user2.setPassword("a8f0ddb94b0f14afb703a462c352200a8cef9cc3118e9ca77dd4b5aaa4ebf63e");
//		user2.setRoles(roles);
//		user2.persist();
		return user.getId().toString();
	}
	
	/**
	 * For testing purposes.
	 * XXX: REMOVE FOR PRODUCTION
	 * @exclude
	 * @return
	 */
	@RequestMapping(value = "/reindex", method = RequestMethod.GET, produces = "application/json")
	public void reindex() {
		EntityManager em = Entity.entityManager();
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
		try {
			fullTextEntityManager.createIndexer(Market.class, Tag.class, Deal.class, Tip.class, Recipe.class, Review.class).startAndWait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * For testing purposes.
	 * XXX: REMOVE FOR PRODUCTION
	 * @exclude
	 * @return
	 */
	@RequestMapping(value = "/versions", method = RequestMethod.GET, produces = "application/json")
	public String versions() {
		String javaVersion = getClass().getPackage().getImplementationVersion();
		String schemaVersion = Entity.entityManager()
			.createNativeQuery("SELECT version FROM public.flyway_schema_history ORDER BY installed_rank DESC LIMIT 1")
			.getSingleResult()
			.toString();
		return String.format("{ \"javaVersion\": \"%s\", \"schemaVersion\": \"%s\" }", javaVersion, schemaVersion);
	}
	@Autowired
	ImageStorageService storage;
	
	@RequestMapping(value = "/migrate", method = RequestMethod.GET, produces = "application/json")
	public String migrate() {
		int limit = 200;
		// Tips
		List<Tip> tips = Tip.findToMigrate(limit).getResultList();
		for (Tip tip : tips) {
			logger.debug("Migrating Tip " + tip.getId());
			MultipartFile f = new BASE64DecodedMultipartFile(tip.getImage(), "photo.jpg");
			try {
				String path = storage.saveImageToServer(f, "tip_" + Long.toString(tip.getId()) + ".png");
				tip.setImagePath(path);
				tip.persist();
			} catch (IOException e) {
				return e.toString();
			}
		}
		limit = limit - tips.size();
		
		// Deals
		List<Deal> deals = Deal.findToMigrate(limit).getResultList();
		for (Deal item : deals) {
			logger.debug("Migrating Deal " + item.getId());
			MultipartFile f = new BASE64DecodedMultipartFile(item.getImage(), "photo.jpg");
			try {
				String path = storage.saveImageToServer(f, "deal_" + Long.toString(item.getId()) + ".png");
				item.setImagePath(path);
				item.persist();
			} catch (IOException e) {
				return e.toString();
			}
		}
		limit = limit - deals.size();
		
		// Market
		List<Market> markets = Market.findToMigrate(limit).getResultList();
		for (Market item : markets) {
			logger.debug("Migrating Market " + item.getId());
			MultipartFile f = new BASE64DecodedMultipartFile(item.getImage(), "photo.jpg");
			try {
				String path = storage.saveImageToServer(f, "market_" + Long.toString(item.getId()) + ".png");
				item.setImagePath(path);
				item.persist();
			} catch (IOException e) {
				return e.toString();
			}
		}
		limit = limit - markets.size();
		
		// Recipes
		List<Recipe> recipes = Recipe.findToMigrate(limit).getResultList();
		for (Recipe item : recipes) {
			logger.debug("Migrating Recipe " + item.getId());
			MultipartFile f = new BASE64DecodedMultipartFile(item.getImage(), "photo.jpg");
			try {
				String path = storage.saveImageToServer(f, "recipe_" + Long.toString(item.getId()) + ".png");
				item.setImagePath(path);
				item.persist();
			} catch (IOException e) {
				return e.toString();
			}
		}
		limit = limit - recipes.size();
		
		// RecipeSteps
		List<RecipeStep> recipeSteps = RecipeStep.findToMigrate(limit).getResultList();
		for (RecipeStep item : recipeSteps) {
			logger.debug("Migrating RecipeStep " + item.getId());
			MultipartFile f = new BASE64DecodedMultipartFile(item.getImage(), "photo.jpg");
			try {
				String path = storage.saveImageToServer(f, "recipeStep_" + Long.toString(item.getId()) + ".png");
				item.setImagePath(path);
				item.persist();
			} catch (IOException e) {
				return e.toString();
			}
		}
		limit = limit - recipeSteps.size();
		
		// Users
		List<User> users = User.findToMigrate(limit).getResultList();
		for (User item : users) {
			logger.debug("Migrating User " + item.getId());
			MultipartFile f = new BASE64DecodedMultipartFile(item.getAvatar(), "photo.jpg");
			try {
				String path = storage.saveImageToServer(f, "user_" + Long.toString(item.getId()) + ".png");
				item.setImagePath(path);
				item.persist();
			} catch (IOException e) {
				return e.toString();
			}
		}
		limit = limit - users.size();

		return Integer.toString(limit);
	}
	
	/**
	 * For testing purposes.
	 * XXX: REMOVE FOR PRODUCTION
	 * @exclude
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/seed", method = RequestMethod.GET, produces = "application/json")
	public String seed(HttpServletRequest request, HttpServletResponse response) {
		Market market = new Market();
		market.setName("Flint Farmers' Market");
		market.setAddress("300 E 1st St, Flint, Michigan, MI 48502");
		market.persist();
		
		Review review = new Review();
		review.setUsr(User.findUsersByEmailEquals("kschemmel.ets@gmail.com").getSingleResult());
		review.setTarget(market);
		review.setText("This is a review of " + market.getName() + ". Great food!");
		review.persist();
		
		Market market2 = new Market();
		market2.setName("The Local Grocer");
		market2.setAddress("601 Martin Luther King Ave, Flint, MI 48502");
		market2.persist();
		
		Review review2 = new Review();
		review2.setUsr(User.findUsersByEmailEquals("kschemmel.ets@gmail.com").getSingleResult());
		review2.setTarget(market2);
		review2.setText("This is a review of " + market2.getName() + ". Great food!");
		review2.persist();
		return null;
	}
	
	/**
	 * For testing purposes.
	 * XXX: REMOVE FOR PRODUCTION
	 * @exclude
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/seed2", method = RequestMethod.GET, produces = "application/json")
	public String seed2(HttpServletRequest request, HttpServletResponse response) {
		Policy terms = new Policy("terms-of-service");
		terms.setDisplayName("Terms of Service");
		terms.setText("Lorem ipsum...");
		terms.persist();
		
		Policy consent = new Policy("irb-consent");
		consent.setDisplayName("IRB Consent");
		consent.setText("Lorem ipsum...");
		consent.persist();

		return null;
	}

	/**
	 * For testing purposes.
	 * XXX: REMOVE FOR PRODUCTION
	 * @exclude
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/devlogin", method = RequestMethod.GET, produces = "application/json")
	public String devlogin(HttpServletRequest request, HttpServletResponse response) {
		//perform login
		UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl();
		UserDetails userDetails = null;
		try {
			userDetails = userDetailsService.loadUserByUsername("admin2");
		} catch (Exception e) {
			this.logger.debug(e.toString());
		}
		if (userDetails == null) {
			this.logger.debug("user is null: {}", "admin2");
			return null;
		}

		Authentication authToken =
				new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
						userDetails.getPassword(),
						userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authToken);
		User u  = User.getLoggedInUser();
		// return user id
		return u.getId().toString();
	}

}
