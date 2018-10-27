package com.etshost.msu.auth;

/**
 * Convenience class to wrap errors in JSON
 *
 */
public class Response {
	
	static String error(int code) {
		return "{ \"code\": " + code + ", \"error\": \"" + code + "\"}";
	}
	
	static String error(String msg) {
		return "{ \"code\": -99, \"error\": \"" + msg + "\"}";
	}
	
	static String error(int code, String msg) {
		return "{ \"code\": " + code + ", \"error\": \"" + msg + "\"}";
	}
	
	static String res(String msg) {
		return "{ \"res\": \"" + msg + "\"}";
	}
}
