package com.etshost.msu.web;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.etshost.msu.entity.Preference;
import com.etshost.msu.entity.UGC;
import com.etshost.msu.entity.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class PreferenceServerController {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public String get(String path, String args) {
		this.logger.debug("GETting");
		String basePath = "http://localhost:5090";
		StringBuffer fullPath = new StringBuffer();
		fullPath.append(basePath);
		fullPath.append(path);
		fullPath.append(args);
		this.logger.debug(fullPath.toString());
		HttpURLConnection connection = null;
		try {
		    // create connection
			URL url = new URL(fullPath.toString());
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");	
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(10000);
			connection.setRequestProperty("content-type", "application/json");

			// get response  
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuffer response = new StringBuffer();
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	public String post(String path, String body) {
		String basePath = "http://localhost:5090";
		HttpURLConnection connection = null;
		try {
		    // create connection
			URL url = new URL(basePath + path);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(10000);
			connection.setRequestProperty("content-type", "application/json");
			
			// send request
			connection.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream (
					connection.getOutputStream());
			wr.writeBytes(body);
			wr.close();
			
			// get response  
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuffer response = new StringBuffer();
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	public String getContentFeed(User user, int page, int pageSize) {
		String args = "?user_id=" + user.getId() + "&page=" + page + "&page_size=" + pageSize;
		String response = this.get("/content_feed/", args);
		if (response == null) {
			return null;
		}
		try {
			this.logger.debug("returned response:" + response);
			JsonArray ids = new JsonParser().parse(response).getAsJsonArray();
			List<UGC> content = new ArrayList<UGC>();
			Iterator<JsonElement> i = ids.iterator();
			while (i.hasNext()) {
				long id = i.next().getAsLong();
				UGC ugc = UGC.findUGC(id);
				content.add(ugc);
			}
			return UGC.toJsonArrayUGC(content);
		} catch (Exception e) {
			this.logger.error(e.toString());
			return null;
		}
	}
	
	public String getFeedUpdates(User user) {
		String args = "?user_id=" + user.getId();
		String response = this.get("/feed_updates/", args);
		if (response == null) {
			return null;
		}
		try {
			this.logger.debug("returned response:" + response);
			JsonArray ids = new JsonParser().parse(response).getAsJsonArray();
			List<UGC> content = new ArrayList<UGC>();
			Iterator<JsonElement> i = ids.iterator();
			while (i.hasNext()) {
				long id = i.next().getAsLong();
				UGC ugc = UGC.findUGC(id);
				content.add(ugc);
			}
			return UGC.toJsonArrayUGC(content);
		} catch (Exception e) {
			this.logger.error(e.toString());
			return null;
		}
	}
	
	public String postUserPreference(Preference preference) {
		return this.post("/user_preferences/", preference.toJson());
	}
}
