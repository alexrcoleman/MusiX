package com.coleman.utilities.http;

import java.util.HashMap;
import java.util.Map;

public class HttpForm {
	private Map<String, String> values;
	private Map<String, String> requestProperties;
	private String formMethod;
	private String contentType;
	private String formSite;
	
	public HttpForm(String formSite, String formMethod) {
		this.formSite = formSite;
		this.formMethod = formMethod;
		this.values = new HashMap<String, String>();
		this.requestProperties = new HashMap<String, String>();
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getFormSite() {
		return formSite;
	}
	
	public void setFormSite(String formSite) {
		this.formSite = formSite;
	}
	
	public String getFormMethod() {
		return formMethod;
	}
	
	public void setFormMethod(String formMethod) {
		this.formMethod = formMethod;
	}
	
	public HttpForm addFormValue(String valueName, String value) {
		values.put(valueName, value);
		return this;
	}
	
	public byte[] getBytes() {
		return Client.convertMapToString(this.values).getBytes();
	}
	
	public Map<String, String> getRequestProperties() {
		return requestProperties;
	}
}
