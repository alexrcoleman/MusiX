package com.coleman.utilities.http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Cookies {
	
	private Map<String, Map<String, String>> cookies = new HashMap<String, Map<String, String>>();
	
	/**
	 * Returns the cookies in format domain.com:cookies
	 * 
	 * @return Cookie data
	 */
	public Map<String, Map<String, String>> getCookies() {
		return cookies;
	}
	
	/**
	 * Gets cookies at a given site
	 * 
	 * @param site
	 *            Site to check (ex. http://www.example.com)
	 * @return Cookies at the given site, in the format cookiename:value
	 */
	public Map<String, String> getSiteCookies(String site) {
		return cookies.get(ClientUtils.getRootDomain(site));
	}
	
	/**
	 * Resets all cookies for all sites
	 */
	public void clearCookies() {
		cookies = new HashMap<String, Map<String, String>>();
	}
	
	/**
	 * Clears cookies at a given site
	 * 
	 * @param site
	 *            Site to clear cookies at
	 */
	public void clearCookies(String site) {
		cookies.remove(ClientUtils.getRootDomain(site));
	}
	
	/**
	 * Gets the value of a cookie at a given site of a given key name
	 * 
	 * @param site
	 *            Site to get from
	 * @param key
	 *            Cookie name/key
	 * @return Value of cookie
	 */
	public String getCookie(String site, String key) {
		return cookies.get(ClientUtils.getRootDomain(site)).get(key);
	}
	
	/**
	 * Clears a cookie at given site
	 * 
	 * @param site
	 *            Site to clear cookie at
	 * @param key
	 *            Name of cookie
	 */
	public void clearCookie(String site, String key) {
		cookies.get(ClientUtils.getRootDomain(site)).remove(key);
	}
	
	/**
	 * Sets the cookie of given value at given site to be given value
	 * 
	 * @param site
	 *            the site to find the cookie at
	 * @param key
	 *            the name of the cookie to modify
	 * @param val
	 *            the value of the cookie
	 */
	public void setCookie(String site, String key, String val) {
		if (!cookies.containsKey(ClientUtils.getRootDomain(site))) {
			System.out.println("Creating new cookies for '" + site + "'");
			cookies.put(ClientUtils.getRootDomain(site), new HashMap<String, String>());
		}
		cookies.get(ClientUtils.getRootDomain(site)).put(key, val);
	}
	
	/**
	 * Sets the cookies at a site to be a given map of cookies (key:val)
	 * 
	 * @param site
	 *            Site
	 * @param map
	 *            HashMap of cookies to use for site
	 */
	public void setCookies(String site, Map<String, String> map) {
		this.cookies.put(ClientUtils.getRootDomain(site), map);
	}
	
	/**
	 * Returns the cookies in a basic format
	 */
	public String toString() {
		if (cookies == null)
			return "No cookies";
		return cookies.toString();
	}
	
	/**
	 * Converts the given cookies to a string readable by a site
	 * 
	 * @param cookies
	 *            the set of cookies to parse
	 * @return cookies in a http cookie format (key1=val1; key2=val2; key3=val3)
	 */
	public static String makeString(Map<String, String> cookies) {
		if (cookies == null)
			return "";
		String content = "";
		Iterator<Entry<String, String>> iterator = cookies.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> next = iterator.next();
			if (!content.equals(""))
				content += "; ";
			content += next.getKey() + "=" + next.getValue();
		}
		return content;
	}
}
