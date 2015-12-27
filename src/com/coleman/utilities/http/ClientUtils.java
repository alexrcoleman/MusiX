package com.coleman.utilities.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientUtils {
	
	/**
	 * Gets the root domain of a site</p> Example:
	 * 
	 * <pre>
	 * getRootDomain(&quot;http://www.example.com&quot;)
	 * </pre>
	 * 
	 * will return
	 * 
	 * <pre>
	 * &quot;example.com&quot;
	 * </pre>
	 * 
	 * @param url
	 *            a full, http protocol url
	 * @return Root domain of the url
	 */
	public static String getRootDomain(String url) {
		if (url.split("/").length < 3) {
			throw new IllegalArgumentException("URL did not follow http:// protocol (" + url + ")");
		}
		String[] domainKeys = url.split("/")[2].split("\\.");
		int length = domainKeys.length;
		int dummy = domainKeys[0].equals("www") ? 1 : 0;
		if (length - dummy == 2)
			return domainKeys[length - 2] + "." + domainKeys[length - 1];
		else {
			if (domainKeys[length - 1].length() == 2) {
				return domainKeys[length - 3] + "." + domainKeys[length - 2] + "." + domainKeys[length - 1];
			} else {
				return domainKeys[length - 2] + "." + domainKeys[length - 1];
			}
		}
		
	}
	
	/**
	 * Reads the given input stream input a string line-by-line, and closes the
	 * input stream when finished
	 * 
	 * @param is
	 *            InputStream to read from
	 * @return String representation of bytes read
	 */
	public static byte[] readInputStream(InputStream is) {
		try {
			BufferedInputStream stream;
			if (is instanceof BufferedInputStream) {
				stream = (BufferedInputStream) is;
			} else {
				stream = new BufferedInputStream(is);
			}
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024 * 4];
			int bytesRead;
			while ((bytesRead = stream.read(buffer)) > 0) {
				byteOut.write(buffer, 0, bytesRead);
			}
			byte[] bytes = byteOut.toByteArray();
			byteOut.close();
			stream.close();
			return bytes;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Sleeps for the given amount of milliseconds.
	 * 
	 * @param millis
	 *            the number of milliseconds to sleep
	 * @return whether or not the sleep was successful (a sleep fails if it is
	 *         interrupted)
	 */
	public static boolean sleep(long millis) {
		try {
			Thread.sleep(millis);
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Encodes the given text first via UTF-8, then replaces %0A and %0D with
	 * %0A%0D (more reliable)
	 * 
	 * @param text
	 *            the text to encode
	 * @return the encoded text
	 */
	public static String encode(String text) {
		
		try {
			text = URLEncoder.encode(text, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		text = text.replaceAll("(%0D%0A)|(%0A)|(%0D)", "%0D%0A");
		return text;
	}
	
	/**
	 * Decodes the given text via UTF-8
	 * 
	 * @param text
	 *            the text to encode
	 * @return the encoded text
	 */
	public static String decode(String text) {
		
		try {
			text = URLDecoder.decode(text, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		text = text.replace("&#39;", "'");
		text = text.replace("&amp;", "&");
		text = text.replace("&#39;", "'");
		text = text.replace("&#39;", "'");
		return text;
	}
	public static String replacer(StringBuffer outBuffer) {

		  String data = outBuffer.toString();
		  try {
		     StringBuffer tempBuffer = new StringBuffer();
		     int incrementor = 0;
		     int dataLength = data.length();
		     while (incrementor < dataLength) {
		        char charecterAt = data.charAt(incrementor);
		        if (charecterAt == '%') {
		           tempBuffer.append("<percentage>");
		        } else if (charecterAt == '+') {
		           tempBuffer.append("<plus>");
		        } else {
		           tempBuffer.append(charecterAt);
		        }
		        incrementor++;
		     }
		     data = tempBuffer.toString();
		     data = URLDecoder.decode(data, "utf-8");
		     data = data.replaceAll("<percentage>", "%");
		     data = data.replaceAll("<plus>", "+");
		  } catch (Exception e) {
		     e.printStackTrace();
		  }
		  return data;
		}
	
	/**
	 * converts the given text into MD5 hash
	 * 
	 * @param text
	 *            the text to convert
	 * @return the converted text
	 */
	public static String toMD5(String text) {
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("MD5");
			byte[] bytes = m.digest(text.getBytes());
			String hexString = "";
			for (byte b : bytes) {
				String hex = Integer.toHexString(0xFF & b);
				if (hex.length() == 1) {
					// could use a for loop, but we're only dealing with a
					// single byte
					hexString += ('0');
				}
				hexString += (hex);
			}
			return hexString;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Gets the title of the webpage with given html code
	 * 
	 * @param html
	 *            the html of webpage
	 * @return the title of the site
	 */
	public static String getTitle(String html) {
		html = html.replaceAll("\\s+", " ");
		Pattern p = Pattern.compile("<title>(.*?)</title>");
		Matcher m = p.matcher(html);
		
		while (m.find() == true) {
			String title = m.group(1);
			if (title != null && !title.equals(""))
				return title;
		}
		return "";
	}
	
	/**
	 * Gets the title of the webpage with given html code
	 * 
	 * @param html
	 *            the html of webpage
	 * @return the title of the site
	 */
	public static String getIconPath(String html) {
		html = html.replaceAll("\\s+", " ");
		Pattern p = Pattern.compile("<LINK REL=\"SHORTCUT ICON\" HREF=\"(.*?)\">");
		Matcher m = p.matcher(html);
		
		while (m.find() == true) {
			String title = m.group(1);
			if (title != null && !title.equals("")) {
				return title;
			}
		}
		return "/favicon.ico";
	}
	
	public static Map<String, String> parseSemicolon(String string) {
		Map<String, String> parsed = new HashMap<String, String>();
		for (String str : string.split("; ")) {
			String key = str.substring(0, str.indexOf("="));
			String value = str.substring(str.indexOf("=") + 1);
			parsed.put(key, value);
		}
		return parsed;
	}
}
