package com.coleman.utilities.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Request {
	private String requestMethod;
	private StringBuilder exactRequest;
	private Map<String, String> properties;
	private Map<String, String> cookies;
	private String fileName;
	private byte[] content;
	private Map<String, String> getProperties;
	
	private Request() {
		requestMethod = "?";
		properties = new HashMap<String, String>();
		exactRequest = new StringBuilder();
		getProperties = new HashMap<String, String>();
	}
	
	public Map<String, String> getGetProperties() {
		return getProperties;
	}
	
	public byte[] getContent() {
		return content;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	
	public String getRequestMethod() {
		return requestMethod;
	}
	
	public Map<String, String> getCookies() {
		return cookies;
	}
	
	public String getExactRequest() {
		return exactRequest.toString();
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder(requestMethod + " method\n");
		Iterator<String> keys = properties.keySet().iterator();
		Iterator<String> values = properties.values().iterator();
		while (keys.hasNext() && values.hasNext()) {
			String key = keys.next();
			String value = values.next();
			builder.append("\t").append(key).append(" = ").append(value).append("\n");
		}
		return builder.toString();
	}
	
	public static Request readRequest(InputStream stream) {
		BufferedReader input = new BufferedReader(new InputStreamReader(stream));
		
		Request header = new Request();
		header.cookies = new HashMap<String, String>();
		try {
			String requestHeader = input.readLine();
			
			header.exactRequest.append(requestHeader).append("\n");
			if (requestHeader == null)
				return null;
			String[] requestPieces = requestHeader.split(" ");
			
			header.requestMethod = requestPieces[0].toUpperCase();
			
			header.properties = new HashMap<String, String>();
			String file = requestPieces[1];
			file = URLDecoder.decode(file, "UTF-8");
			if (file.endsWith("/"))
				file += "index.html";
			if (file.contains("?")) {
				String params = requestPieces[1].substring(requestPieces[1].indexOf("?") + 1);
				file = file.substring(0, file.indexOf("?"));
				header.getProperties = Request.parseParamaters(params);
			}
			header.fileName = file.toLowerCase();
			
			String line;
			while (true) {
				line = input.readLine();
				if (line == null)
					return header;
				if (line.trim().equals(""))
					break;
				header.exactRequest.append(line).append("\n");
				String propertyName = line.substring(0, line.indexOf(": "));
				String propertyValue = line.substring(line.indexOf(": ") + 2, line.length());
				if (propertyName.equals("Cookie"))
					header.cookies = Request.parseParamaters(propertyValue);
				else
					header.properties.put(propertyName, propertyValue);
			}
			header.exactRequest.append("\n");
			if (header.properties.containsKey("Content-Length"))
				if (!header.properties.containsKey("Content-Length")) {
					return null;
				} else {
					int contentLength = Integer.parseInt(header.properties.get("Content-Length"));
					char[] chars = new char[contentLength];
					input.read(chars);
					header.content = new String(chars).getBytes();
				}
		} catch (SocketException e) {
			header = null;
		} catch (IOException e) {
			header = null;
		}
		return header;
	}
	
	public static String parseParamaters(Map<String, String> map) {
		Iterator<String> keyIterator = map.keySet().iterator();
		Iterator<String> valueIterator = map.values().iterator();
		
		StringBuilder cookies = new StringBuilder();
		while (keyIterator.hasNext() && valueIterator.hasNext()) {
			String key = keyIterator.next();
			String value = valueIterator.next();
			if (cookies.length() > 0)
				cookies.append("&");
			cookies.append(key).append("=").append(value);
		}
		return cookies.toString();
	}
	
	public static Map<String, String> parseParamaters(String str) {
		Map<String, String> map = new HashMap<String, String>();
		
		String[] params = str.split("&");
		for (String param : params) {
			String[] paramPieces = param.split("=");
			if (paramPieces.length != 2)
				continue;
			String key = ClientUtils.decode(paramPieces[0]);
			String val = ClientUtils.decode(paramPieces[1]);
			map.put(key, val);
		}
		return map;
	}
}
