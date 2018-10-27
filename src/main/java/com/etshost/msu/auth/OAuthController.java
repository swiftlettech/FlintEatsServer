package com.etshost.msu.auth;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

import com.etshost.msu.bean.UserDetailsServiceImpl;
import com.etshost.msu.entity.User;
import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuthService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Controller allowing for OAuth login through supported third-party providers.
 */
@RequestMapping("/oauth")
@RestController
@PropertySource("classpath:auth/oauth.properties")
public class OAuthController {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${facebook.apiKey}")
	private String facebookApiKey;

	@Value("${facebook.apiSecret}")
	private String facebookApiSecret;
	
	@Value("${facebook.validateUrl}")
	private String facebookValidateUrl;
	
	@Value("${google.apiKey}")
	private String googleApiKey;

	@Value("${google.apiSecret}")
	private String googleApiSecret;
	
	@Value("${google.validateUrl}")
	private String googleValidateUrl;
	
	@Value("${twitter.apiKey}")
	private String twitterApiKey;

	@Value("${twitter.apiSecret}")
	private String twitterApiSecret;
	
	@Value("${twitter.validateUrl}")
	private String twitterValidateUrl;
	
	private OAuthProvider provider;
	
	private JsonObject response;
	
	/**
	 * Validates OAuth credentials with appropriate third-party provider
	 * @param body JSON: (provider, accessToken [, accessTokenSecret)
	 * @return JSON response from provider, with JSESSIONID appended
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	@RequestMapping(value = "/validate", method = RequestMethod.POST, produces = "application/json")
	public String validate(@RequestBody String body)
			throws MalformedURLException, IOException, InterruptedException, ExecutionException {
				
	    JsonParser parser = new JsonParser();
	    JsonObject jBody = parser.parse(body).getAsJsonObject();
	    String providerName = jBody.get("provider").getAsString();
	    String token = jBody.get("accessToken").getAsString();

		JsonElement secret = null;
	    String tokenSecret = null;
	    try {
	    	secret = jBody.get("accessTokenSecret");
	    } catch (Exception e) {
	    	this.logger.debug("No accessTokenSecret");
	    }
	    if (secret != null
	    		&& secret.getAsJsonPrimitive().isString()) {
	    	tokenSecret = jBody.get("accessTokenSecret").getAsString();
	    }
		
		provider = null;
		try {
			provider = OAuthProvider.valueOf(providerName.toUpperCase());
		} catch (Exception e) {
			this.logger.error(e.getMessage());
			return null;
		}
		
		String url = null;
		
		URLConnection conn;
		OutputStreamWriter writer;
		BufferedReader reader;
		String line;
		
		this.logger.debug("provider: {}", provider.toString());

		final OAuthService<OAuth1AccessToken> service;
		final OAuthService<OAuth2AccessToken> service2;
		final OAuthRequest oRequest;
		final Response oResponse;
		String oResponseBody;
		
		switch (provider) {
			case FACEBOOK:
				url = "https://graph.facebook.com/me?fields=id&access_token=";
				url += token;
			    conn = new URL(url).openConnection();
			    conn.setDoOutput(true);
			    writer = new OutputStreamWriter(conn.getOutputStream());
			    writer.flush();

			    // Get the response
				oResponseBody = "";
			    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			    while ((line = reader.readLine()) != null) {
			       oResponseBody += line;
			    }
			    reader.close();
				break;
				
			case GOOGLE:
				service2 = new ServiceBuilder()
						.apiKey(googleApiKey)
						.apiSecret(googleApiSecret)
						.scope("profile email")
						.build(GoogleApi20.instance());
				oRequest = new OAuthRequest(Verb.GET, googleValidateUrl);
				service2.signRequest(new OAuth2AccessToken(token), oRequest);
				oResponse = service2.execute(oRequest);
				oResponseBody = oResponse.getBody();
			    response = parser.parse(oResponseBody).getAsJsonObject();
			    if (response.get("emails") != null
			    		&& response.get("emails").isJsonArray()) {
			    	// if email is in response, validation was successful, and we can log in
			    	JsonArray emailArray = response.get("emails").getAsJsonArray();
			    	String email = null;
			    	for (JsonElement jmail : emailArray) {
			    		try {
				    		JsonObject jmailo = jmail.getAsJsonObject();
				    		if (jmailo.get("type").getAsString().equals("account")) {
				    			email = jmailo.get("value").getAsString();
				    			break;
				    		}
			    		} catch (Exception e) {
			    			this.logger.error(e.toString());
			    		}
			    	}
			    	this.login(email);
			    }
				break;
				
			case TWITTER:
				
				service = new ServiceBuilder()
						.apiKey(twitterApiKey)
						.apiSecret(twitterApiSecret)
						.build(TwitterApi.instance());
				oRequest = new OAuthRequest(Verb.GET, twitterValidateUrl);
				oRequest.addQuerystringParameter("include_email", "true");
				service.signRequest(new OAuth1AccessToken(token, tokenSecret), oRequest);
				oResponse = service.execute(oRequest);
				oResponseBody = oResponse.getBody();
			    response = parser.parse(oResponseBody).getAsJsonObject();
			    if (response.get("email") != null
			    		&& response.get("email").getAsJsonPrimitive().isString()) {
			    	// if email is in response, validation was successful, and we can log in
			    	String email = response.get("email").getAsString();
			    	this.login(email);
			    }

				break;
				
			default:
				return null;
		}
		
		// add JSESSIONID to response
		String jSessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
		response.addProperty("JSESSIONID", jSessionId);

		this.logger.debug("{}", response);

		return response.toString();
	}

	private boolean login(String email) {
		this.logger.debug("Attempting login of {}", email);
		UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl();
		UserDetails userDetails = null;
		try {
			userDetails = userDetailsService.loadUserByUsername(email);
		} catch (Exception e) {
			this.logger.debug(e.toString());
			this.logger.debug("User not found. Creating account for {}", email);
			userDetails = signup(email);
		}
		if (userDetails == null) {
			this.logger.debug("user is null: {}");
			return false;
		}

		Authentication authToken =
				new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
														userDetails.getPassword(),
														userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authToken);
		return true;
	}
	
	private UserDetails signup(String email) {
		UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl();

		User user = new User(provider, response);
		user.persist();
		
		UserDetails userDetails = null;
		try {
			userDetails = userDetailsService.loadUserByUsername(email);
		} catch (UsernameNotFoundException e) {
			this.logger.debug("User not found. Did not create account for {}", email);
		}
		return userDetails;
	}
}
