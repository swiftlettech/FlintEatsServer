package com.etshost.msu.entity;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.envers.Audited;
import org.imgscalr.Scalr;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentPBEConfig;
import org.jasypt.hibernate4.encryptor.HibernatePBEEncryptorRegistry;
import org.jasypt.hibernate4.type.EncryptedStringType;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.transaction.annotation.Transactional;

import com.etshost.msu.auth.OAuthProvider;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import flexjson.JSON;
import flexjson.JSONSerializer;


/**
 * Represents a user of the system.
 */
@Audited
@Configurable
@javax.persistence.Entity
@RooJavaBean
@RooJpaActiveRecord(finders = {
		"findUsersByEmailEquals",
		"findUsersByEmailLike",
		"findUsersByFirstNameLike",
		"findUsersByLastNameLike",
		"findUsersByRoles",
		"findUsersByStatus",
		"findUsersByUsernameEquals",
		"findUsersByUsernameLike"
})
@RooJson
@RooToString
@Table(name = "usr")
@Transactional
@TypeDef(name = "encryptedString",
	typeClass = EncryptedStringType.class,
	parameters = {
		@org.hibernate.annotations.Parameter(
				name = "encryptorRegisteredName",
				value = "myHibernateStringEncryptor")
	}
)
public class User extends Entity {

//    @ManyToOne
//    private Avatar avatar;
	
	// going to try a simpler approach
	@JSON(include = false)
	private byte[] avatar;
	
	private byte[] background;
	
    @Pattern(regexp = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}|^$",
    		message = "Not a valid email address.")
    private String email;

    private String firstName;
    
    private String lastName;

    @JSON(include = false)
    @Type(type = "encryptedString")
    private String password;
    
    @Pattern(regexp = "^(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$",
    		message = "Not a valid phone number")
    private String phone;
    
    private boolean gisOn;
    
    private NotificationFrequency notificationFrequency;
    
	private String username;
	
	private Instant termsAccept;
	
	private Instant irbAccept;

    
    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "followers")
    private Set<User> followees = new HashSet<User>();
    
    @ManyToMany(cascade = CascadeType.ALL)
    private Set<User> followers = new HashSet<User>();

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Role> roles = new HashSet<Role>();
    
    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Badge> badges = new HashSet<Badge>();
    
    // UGC
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usr")
    private Set<UGC> ugc = new HashSet<UGC>();

    public User() {
        super();
        final StandardPBEStringEncryptor strongEncryptor = new StandardPBEStringEncryptor();
        final EnvironmentPBEConfig config = new EnvironmentPBEConfig();
        String password = "03f2316993f6cc3886914ec8a48ccf82f36f05a782c37864cb7178786fa30bc16370420cc2fefbb4a5f842a0d761bce0dff00171a2977b952109464b001b9959";
        config.setPassword(password);
        strongEncryptor.setConfig(config);
        final HibernatePBEEncryptorRegistry registry = HibernatePBEEncryptorRegistry.getInstance();
        registry.registerPBEStringEncryptor("myHibernateStringEncryptor", strongEncryptor); 
    }
    
    /*
     * This constructor fills a new user's profile from the user's profile with the OAuth provider
     */
    public User(OAuthProvider provider, JsonObject profile) {
        super();
        final StandardPBEStringEncryptor strongEncryptor = new StandardPBEStringEncryptor();
        final EnvironmentPBEConfig config = new EnvironmentPBEConfig();
        String password = "03f2316993f6cc3886914ec8a48ccf82f36f05a782c37864cb7178786fa30bc16370420cc2fefbb4a5f842a0d761bce0dff00171a2977b952109464b001b9959";
        config.setPassword(password);
        strongEncryptor.setConfig(config);
        final HibernatePBEEncryptorRegistry registry = HibernatePBEEncryptorRegistry.getInstance();
        registry.registerPBEStringEncryptor("myHibernateStringEncryptor", strongEncryptor);
        
        
        
        
        // construct user differently based on provider
        switch (provider) {
			case FACEBOOK:
				break;
				
			case GOOGLE:
				// set email
		    	JsonArray emailArray = profile.get("emails").getAsJsonArray();
		    	for (JsonElement jmail : emailArray) {
		    		try {
			    		JsonObject jmailObj = jmail.getAsJsonObject();
			    		if (jmailObj.get("type").getAsString().equals("account")) {
			    			setEmail(jmailObj.get("value").getAsString());
			    			break;
			    		}
		    		} catch (Exception e) {
		    			this.logger.error(e.toString());
		    		}
		    	}
		    	// set name
		    	if (profile.get("name") != null
		    			&& profile.get("name").isJsonObject()) {
		    		JsonObject jName = profile.get("name").getAsJsonObject();
		    		if (jName.get("familyName") != null
		    				&& jName.get("familyName").getAsJsonPrimitive().isString()) {
		    			setLastName(jName.get("familyName").getAsString());
		    		}
		    		if (jName.get("givenName") != null
		    				&& jName.get("givenName").getAsJsonPrimitive().isString()) {
		    			setFirstName(jName.get("givenName").getAsString());
		    		}
		    		
		    	}
		    	// set avatar
		    	if (profile.get("image") != null
		    			&& profile.get("image").isJsonObject()) {
		    		JsonObject jImage = profile.get("image").getAsJsonObject();
		    		if (jImage.get("url") != null
		    				&& jImage.get("url").getAsJsonPrimitive().isString()) {
		    			setAvatar(jImage.get("url").getAsString());
		    		}	    		
		    	}
				break;
				
			case TWITTER:
				// set email
			    if (profile.get("email") != null
	    				&& profile.get("email").getAsJsonPrimitive().isString()) {
			    	setEmail(profile.get("email").getAsString());
			    }
			    // set name
			    if (profile.get("name") != null
	    				&& profile.get("name").getAsJsonPrimitive().isString()) {
			    	setName(profile.get("name").getAsString());
			    }
			    // set avatar
			    if (profile.get("profile_image_url") != null
	    				&& profile.get("profile_image_url").getAsJsonPrimitive().isString()) {
			    	setAvatar(profile.get("profile_image_url").getAsString());
			    }
				break;
				
			default:
				break;
	        
        }
        
        // default no notifications
        setNotificationFrequency(NotificationFrequency.NEVER);
        
        // set random password to not break login
        setPassword(UUID.randomUUID().toString());
    }
    
    /*
    
	@JsonCreator
	public static User factory(@JsonProperty("username") String username, 
			@JsonProperty("email") String email, @JsonProperty("password") String password) {
		User user = new User();
		if (email.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}|^$")) {
			user.setEmail(email);
		}
		user.setPassword(password);
		return user;
	}
	*/

    @JSON(include = false)
    public PasswordResetToken createPasswordResetToken() {
        PasswordResetToken prt = new PasswordResetToken();
        Instant now = Instant.now();
        prt.setUsr(this);
        prt.setToken(UUID.randomUUID().toString());
        // token valid for 24 hours
        prt.setExpiryDate(now.plus(1, ChronoUnit.DAYS));
        prt.persist();
        return prt;
    }
    
    @JSON(include = false)
    public byte[] getAvatar() {
    	return this.avatar;
    }
    
    @JSON(include = false)
    public String getAvatarBase64() {
    	if (this.avatar == null) {
    		return null;
    	}
    	String avatar64 = Base64.getEncoder().encodeToString(this.avatar);
        return avatar64;
    }
    
    @JSON(name = "avatar64")
	public String getAvatarBase64Scaled() {
    	if (this.avatar == null) {
    		return null;
    	}
		final InputStream in = new ByteArrayInputStream(this.avatar);
		BufferedImage image;
		BufferedImage imageScaled;

		// read in the image
		try {
			image = ImageIO.read(in);
		} catch (final IOException e) {
			// TODO Auto-generated catch block. Come up with better return.
			e.printStackTrace();
			return null;
		}
		if (image == null) {
			return null;
		}
		
		imageScaled = Scalr.resize(image, 64);

		// write the image
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			ImageIO.write(imageScaled, "png", output);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// Come up with better return
			return null;
		}
    	String image64 = Base64.getEncoder().encodeToString(output.toByteArray());
		return image64;
	}
    
    @JSON(name = "name")
    public String getName() {
        if (this.getFirstName() == null) this.setFirstName("");
        if (this.getLastName() == null) this.setLastName("");
        return this.getFirstName() + " " + this.getLastName();
    }

    //TODO: change language
    public void reportLoginFailure() {
        final MailSender mailSender = new JavaMailSenderImpl();
        ((JavaMailSenderImpl) mailSender).setHost("imap.etshost.com");
        ((JavaMailSenderImpl) mailSender).setPort(465);
        ((JavaMailSenderImpl) mailSender).setUsername("chass@etshost.com");
        ((JavaMailSenderImpl) mailSender).setPassword("EtS0199!");
        final Properties props = ((JavaMailSenderImpl) mailSender).getJavaMailProperties();
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.send.protocol", "smtps");
        ((JavaMailSenderImpl) mailSender).setJavaMailProperties(props);
        final SimpleMailMessage templateMessage = new SimpleMailMessage();
        templateMessage.setSubject("Failed Login Attempt");
        templateMessage.setFrom("help@<email>");
        templateMessage.setTo(this.getEmail());
        templateMessage.setText("Dear " + this.getName()
        		+ "\nThere has been a failed login to your account."
        		+ "If you suspect suspicious account activity, "
        		+ "please contact help@<email>");
        try {
            mailSender.send(templateMessage);
        } catch (final MailException ex) {
            this.logger.info(ex.getMessage());
        }
    }

    public String changePassword(final String password) {
    	/*
        if (password.equals(this.getPassword())) {
            return "New password must be different from old password.";
        }
        // check for password complexity
        // this is a simple DIY solution; consider vt-password if more functionality is needed
        // password must meet at least two criteria
        int met = 0;
        // contains number
        if (password.matches(".*\\d.*")) {
            met++;
        }
        // contains letter (lowercase)
        if (password.matches(".*[a-z].*")) {
            met++;
        }
        // contains letter (uppercase)
        if (password.matches(".*[A-Z].*")) {
            met++;
        }
        // contains special character (non-letter, non-number)
        if (password.matches(".*[^a-zA-Z0-9].*")) {
            met++;
        }
        // met 2+ criteria, and is 8+ characters
        if (!password.matches(".{8,}") || met < 2) {
            return "Password does not meet complexity requirements.<br />"
            		+ "Must be at least 8 characters and contain at least two of the following:<br />"
            		+ "- Uppercase letter<br />"
            		+ "- Lowercase letter<br />"
            		+ "- Number<br />"
            		+ "- Special character";
        }
        */
        this.setPassword(password);
        return this.getId().toString();
    }
    
    public List<UGC> getFaves() {
		List<Reaction> reactions = Reaction.findReactionsByUsr(this).getResultList();
		List<UGC> ugcList = new ArrayList<UGC>();
		for (Reaction reaction : reactions) {
			if (reaction.getEndTime() == null) {
				ugcList.add(reaction.getTarget());
			}
		}
		// sort by create date, most recent first
		ugcList.sort((ugc1, ugc2) -> ugc2.getCreated().compareTo(ugc1.getCreated()));
		return ugcList;
    }

    public boolean hasRole(String roleName) {
        Role role = null;
        try {
            role = Role.findRoleByName(roleName);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
        return this.getRoles().contains(role);
    }
    
    @JSON(name = "admin")
    public boolean admin() {
    	return this.hasRole("admin");
    }
    
    @Override
    @Transactional
    public User merge() {
		final Instant now = Instant.now();
        if (this.entityManager == null) {
            this.entityManager = Entity.entityManager();
        }
        if ((this.getId() == 0) || (this.getId() == null)) {
            this.setCreated(now);
        }
        this.setModified(now);
        final User merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    public void setAvatar(String urlString) {
    	// download avatar from URL
    	URL url;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
        try {
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect(); 

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(conn.getInputStream(), baos);

            setAvatar(baos.toByteArray());
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }
    
    public void setAvatarBase64(String avatar64) {
    	try {
    		byte[] avatar = Base64.getMimeDecoder().decode(avatar64);
            this.setAvatar(avatar);
    	} catch (Exception e) {
    		this.logger.error(e.toString());
    	}
    }
    
    public void setName(String name) {
    	String[] names = name.split(" ");
    	if (names[0] != null) {
    		setFirstName(names[0]);
    	}
    	names[0] = "";
    	setLastName(String.join(" ", names).trim());
    }
    
    public void setNotificationFrequency(String freq) {
    	if (freq == null) {
    		return;
    	}
    	try {
    		NotificationFrequency frequency = NotificationFrequency.valueOf(freq);
    		this.setNotificationFrequency(frequency);
    	} catch (IllegalArgumentException e) {
    		this.logger.warn(e.toString());
    	}
    }
    
    public String toJson() {
        return new JSONSerializer()
        		.include("id")
        		.exclude("logger", "password", "*.class").serialize(this);
    }
    
    public static String toJsonArrayUser(Collection<User> collection) {
        return new JSONSerializer()
        		.include("id", "class", "username", "avatar64")
        		.exclude("*")
        		.serialize(collection);
    }

    public static User getLoggedInUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
        	return null;
        }
        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
        	return null;
        }

        SimpleGrantedAuthority anon = new SimpleGrantedAuthority("ROLE_ANONYMOUS");
        if (authentication.getAuthorities().contains(anon)) {
            return null;
        }

        return User.findUsersByUsernameEqualsNoCase(authentication.getName()).getSingleResult();
    }
    
	/**
	 * This method attempts to read the current Spring Security Context to get
	 * the IP address from a remote user.
	 * XXX:This method is not tested with IPv6 addresses,
	 *  however it should work without any modification.
	 * 
	 * @return The String representation of an IP address.
	 */
	public static String getUserIp() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
        	return null;
        }
        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
        	return null;
        }
        WebAuthenticationDetails details =
        		(WebAuthenticationDetails) authentication.getDetails();
        if (details == null) {
        	return null;
        }
		return details.getRemoteAddress();
	}

	/**
	 * This method attempts to read the current Spring Security Context to get
	 * the login of the remote user.
	 * 
	 * @return User login name.
	 */
	public static String getUserLogin() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
        	return null;
        }
        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
        	return null;
        }
		return authentication.getName();
	}

    public static String generateDataTables(final int draw, final int start, final int length,
    		final String orderColumnName, final String orderDir) {
        long count = User.countUsers();
        List<User> uList = null;
        if (length == -1) {
            uList = User.findAllUsers(orderColumnName, orderDir);
        } else {
            uList = User.findUserEntries(start, (int)Math.min(length, count), orderColumnName, orderDir);
        }

        JsonArray data = new JsonArray();
        Iterator<User> i = uList.iterator();
        while (i.hasNext()) {
            User u = i.next();
            JsonArray uj = new JsonArray();
            uj.add(u.getId());
            uj.add(u.getCreated().toString());
            uj.add(u.getUsername());
            uj.add(u.getStatus().name());
            data.add(uj);
        }

        JsonObject obj = new JsonObject();
        obj.addProperty("draw", String.valueOf(draw));
        obj.addProperty("recordsTotal", String.valueOf(count));
        obj.addProperty("recordsFiltered", String.valueOf(count));
        obj.add("data", data);
        return obj.toString();
    }

    public static String generateDataTables(final int draw, final int start, final int length,
    		final String orderColumnName, final String orderDir, final String query) {
        // if no query string, don't filter
        if (query == null || query.equals("")) {
            return generateDataTables(draw, start, length, orderColumnName, orderDir);
        }
        long count = User.countUsers();
        // make a set containing matching elements
        Set<User> qSet = new HashSet<User>();
        qSet.addAll(User.findUsersByUsernameLike(query).getResultList());
        List<User> qList = new ArrayList<User>();
        qList.addAll(qSet);
        User.sort(qList, orderColumnName, orderDir);
        Iterator<User> i;
        if (length == -1) {
            i = qList.iterator();
        } else {
            List<User> subList = qList.subList(start, Math.min(start + length, qList.size()));
            i = subList.iterator();
        }
        JsonArray data = new JsonArray();
        while (i.hasNext()) {
            User u = i.next();
            JsonArray uj = new JsonArray();
            uj.add(u.getId());
            uj.add(u.getCreated().toString());
            uj.add(u.getUsername());
            uj.add(u.getStatus().name());
            data.add(uj);
        }
        JsonObject obj = new JsonObject();
        obj.addProperty("draw", String.valueOf(draw));
        obj.addProperty("recordsTotal", String.valueOf(count));
        obj.addProperty("recordsFiltered", String.valueOf(qList.size()));
        obj.add("data", data);
        return obj.toString();
    }

   /*
    * Finders
    */
    
    // pulled from Roo file due to bug [ROO-3570]
    public static List<User> findUserEntries(int firstResult, int maxResults,
    		String sortFieldName, String sortOrder) {
    	if (sortFieldName == null || sortOrder == null) {
    		return User.findUserEntries(firstResult, maxResults);
    	}
        String jpaQuery = "SELECT o FROM User o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)
        		|| Entity.fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        
        if (maxResults < 0) {
            return entityManager().createQuery(jpaQuery, User.class)
            		.setFirstResult(firstResult).getResultList();
        }

        return entityManager().createQuery(jpaQuery, User.class)
        		.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
 	public static TypedQuery<User> findUsersByEmailEqualsAndIdNotEquals(String email, long id) {
 		if (email == null || email.length() == 0) {
 			throw new IllegalArgumentException("The email argument is required");
 		}
    	EntityManager em = User.entityManager();
    	TypedQuery<User> q = em.createQuery("SELECT o FROM User AS o "
                       + "WHERE o.email = :email AND o.id <> :id", User.class);
        q.setParameter("email", email);
        q.setParameter("id", id);
        return q;
    }
 	
    public static TypedQuery<User> findUsersByEmailEqualsNoCase(String email) {
        if (email == null || email.length() == 0) throw new IllegalArgumentException("The email argument is required");
        EntityManager em = User.entityManager();
        TypedQuery<User> q = em.createQuery("SELECT o FROM User AS o WHERE LOWER(o.email) = LOWER(:email)", User.class);
        q.setParameter("email", email);
        return q;
    }
 	
    public static TypedQuery<User> findUsersByUsernameEqualsNoCase(String username) {
        if (username == null || username.length() == 0) throw new IllegalArgumentException("The username argument is required");
        EntityManager em = User.entityManager();
        TypedQuery<User> q = em.createQuery("SELECT o FROM User AS o WHERE LOWER(o.username) = LOWER(:username)", User.class);
        q.setParameter("username", username);
        return q;
    }

}
