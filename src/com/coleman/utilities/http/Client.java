package com.coleman.utilities.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

public class Client {
	private String userAgent = null;
	private boolean cookiesEnabled = true;
	private Cookies cookies;

	public Client() {
		this.cookies = new Cookies();
	}

	/**
	 * Gets the cookies currently contained in the client
	 * 
	 * @return Cookies object
	 * @see Cookies
	 */
	public Cookies getCookies() {
		return cookies;
	}

	/**
	 * Checks whether or not cookies are currently set to be saved in between posts
	 * 
	 * @return boolean, true meaning enabled, false meaning disabled
	 */
	public boolean areCookiesEnabled() {
		return cookiesEnabled;
	}

	/**
	 * Toggles whether or not cookies should be saved </p> It should be noted that this will not remove any old cookies,
	 * merely temporarly prevent cookies from being used, and prevent new ones from being stored
	 * 
	 * @param cookiesEnabled
	 */

	public void setCookiesEnabled(boolean cookiesEnabled) {
		this.cookiesEnabled = cookiesEnabled;
	}

	/**
	 * Modifies the user agent to be used. If a null value is passed (the default value of the agent), the default Java agent
	 * will be used
	 * 
	 * @param userAgent
	 * @see <a href="http://www.useragentstring.com/pages/useragentstring.php">list of browsers</a>
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * Gets the current set user agent (default is null, which will use the default java agent)
	 * 
	 * @return current agent
	 */
	public String getUserAgent() {
		return userAgent;
	}

	public byte[] readSite(String site, String requestHeaders) {
		InputStream is = null;
		Socket socket = null;
		try {
			socket = new Socket(site, 80);
			socket.getOutputStream().write(requestHeaders.getBytes());
			Request r = Request.readRequest(socket.getInputStream());
			if (areCookiesEnabled()) {
				saveCookies(site, r.getExactRequest());
			}
			return r.getExactRequest().getBytes();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (Exception e) {
			}
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Reads the page at the given site, and returns the HTML found.
	 * 
	 * @param site
	 *            Site to read
	 * @return The text at the given site, or null if an error occurs.
	 */
	public byte[] readSite(String site) {
		InputStream is = null;
		try {
			URL url = new URL(site);
			URLConnection conn = url.openConnection();
			conn = initConnection((HttpURLConnection) conn, null, null);
			is = conn.getInputStream();

			if (areCookiesEnabled()) {
				saveCookies(conn);
			}
			return ClientUtils.readInputStream(is);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (Exception e) {
			}
		}
	}

	protected HashMap<Long, Downloader> downloaders = new HashMap<Long, Downloader>();

	public Downloader readSiteAsync(final String site, final DownloadProgressHandler dph) {
		final long uid = new Random().nextLong();
		Downloader downloadThread = new Downloader(site, dph, uid);
		downloadThread.start();
		downloaders.put(uid, downloadThread);
		return downloadThread;
	}

	public byte[] waitForComplete(long id) {
		return waitForComplete(downloaders.get(id));
	}

	public byte[] waitForComplete(Downloader d) {
		while (d.isAlive()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		return d.getBytes();
	}

	/**
	 * Reads the page at the given site, and returns the HTML found.
	 * 
	 * @param site
	 *            Site to read
	 * @return The text at the given site, or null if an error occurs.
	 */
	public URLConnection loadSite(String site) {
		InputStream is = null;
		try {
			URL url = new URL(site);
			URLConnection conn = url.openConnection();
			conn = initConnection((HttpURLConnection) conn, null, null);
			conn.connect();
			if (areCookiesEnabled()) {
				saveCookies(conn);
			}
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Performs an HTTP post on the given site with given parameters, using client-wide properties, and returns the callback
	 * 
	 * @param site
	 * @param params
	 *            HTTP post key:value parameters stored in a map
	 * @return callback of post method
	 */
	public byte[] post(String site, Map<String, String> params) {
		return post(site, "application/x-www-form-urlencoded", convertMapToString(params).getBytes());
	}

	/**
	 * Performs an HTTP post on the given site with given body, using client-wide properties, and returns the callback
	 * 
	 * @param site
	 * @param body
	 *            HTTP post body (text/plain)
	 * @return callback of post method
	 */
	public byte[] post(String site, String body) {
		return post(site, "text/plain", body.getBytes());
	}

	public byte[] httpForm(final HttpForm form) {
		return httpForm(form.getFormSite(), form.getFormMethod(), form.getContentType(), form.getBytes().length > 0 ? new OutputStreamHandler() {

			@Override
			public void writeTo(OutputStream stream) throws IOException {
				if (form.getBytes().length > 0) {
					stream.write(form.getBytes());
				}
			}

		} : null, form.getRequestProperties());
	}

	/**
	 * Performs an HTTP post on the given site with given body, using client-wide properties, and returns the callback
	 * 
	 * @param site
	 * @param contentType
	 * @param content
	 * @param body
	 *            HTTP post body (text/plain)
	 * @return callback of post method
	 */
	public byte[] post(String site, String contentType, byte[] content) {
		try {
			// URL Inilializing:
			URL url = new URL(site);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn = initConnection(conn, contentType, "POST");

			// Writing data to the connection:
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.write(content);
			out.close();

			// Reading the output:
			InputStream in = conn.getInputStream();
			byte[] read = ClientUtils.readInputStream(in);
			in.close();

			// Saving cookies if needed
			if (areCookiesEnabled()) {
				saveCookies(conn);
			}
			return read;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte[] postMultipart(String site, Map<String, String> map, String fileFieldName, String fileName, String contentType, byte[] fileBytes) {
		String boundary = "----------V2ymHFg03ehbqgZCaKO6jy";
		try {
			// URL Inilializing:
			URL url = new URL(site);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn = initConnection(conn, "multipart/form-data; boundary=" + boundary, "POST");
			OutputStream output = conn.getOutputStream();
			StringBuilder builder = new StringBuilder();
			builder.append("--" + boundary + "\r\n");
			Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> next = iterator.next();
				if (next.getKey() == null || next.getValue() == null)
					continue;
				String key = next.getKey();
				String value = next.getValue();

				builder.append("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n");
				builder.append(value);
				builder.append("\r\n--" + boundary + "\r\n");
			}
			builder.append("Content-Disposition: form-data; name=\"" + fileFieldName + "\"; filename=\"" + fileName + "\"\r\nContent-Type: " + contentType + "\r\n\r\n");

			output.write(builder.toString().getBytes());
			output.write(fileBytes);
			builder = new StringBuilder();
			builder.append("\r\n--" + boundary + "--\r\n");
			output.write(builder.toString().getBytes());
			output.close();

			// Reading the output:
			InputStream in = conn.getInputStream();
			byte[] read = ClientUtils.readInputStream(in);
			in.close();
			System.out.println("Response: " + conn.getResponseCode() + " " + conn.getResponseMessage());

			// Saving cookies if needed
			if (areCookiesEnabled()) {
				saveCookies(conn);
			}
			return read;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte[] httpForm(String site, String method, String contentType, OutputStreamHandler dataWriter, Map<String, String> requestProperties) {
		try {
			System.out.println("Reading " + site);
			// URL Inilializing:
			URL url = new URL(site);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			/*
			 * if(site.equals("https://www.google.com?internal")) { String cookie = conn.getHeaderField(
			 * "Set-Cookie").split(";")[0]; Pattern pattern = Pattern.compile("content=\\\"0;url=(.*?)\\\""); Matcher m =
			 * pattern.matcher(response); if( m.find() ) { String redirect = m.group(1); conn = (HttpURLConnection) new
			 * URL(redirect).openConnection(); conn.setRequestProperty("User-Agent",
			 * "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
			 * ); conn.setRequestProperty("Cookie", cookie );
			 * 
			 * } } else if(site.contains("gdata")) { System.out.println(new String(httpForm("https://www.google.com", "GET",
			 * "text/plain", null, null))); }
			 */

			conn = initConnection(conn, contentType, method);
			if (requestProperties != null) {
				for (Entry<String, String> requestProperty : requestProperties.entrySet()) {
					conn.setRequestProperty(requestProperty.getKey(), requestProperty.getValue());
					System.out.println("\t" + requestProperty.getKey() + " = " + requestProperty.getValue());
				}
			}
			// Writing data to the connection:
			if (dataWriter != null) {
				System.out.println("Writing data");
				OutputStream out = conn.getOutputStream();
				dataWriter.writeTo(out);
				out.close();
			}

			// Reading the output:

			InputStream in = conn.getInputStream();
			byte[] read = ClientUtils.readInputStream(in);
			in.close();

			// Saving cookies if needed
			if (areCookiesEnabled()) {
				saveCookies(conn);
			}
			return read;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Inialitizes a HttpURLConnection with client-wide properties, and passed values
	 * 
	 * @param conn
	 *            Connection to modify
	 * @param contentType
	 *            Content type to use (null for none)
	 * @param method
	 *            Method to use (POST, GET, or null for default)
	 * @return Modified HttpURLConnection
	 */
	private HttpURLConnection initConnection(HttpURLConnection conn, String contentType, String method) {

		String mycookies = Cookies.makeString(this.getCookies().getSiteCookies(conn.getURL().toString()));
		try {
			if (method != null)
				conn.setRequestMethod(method);
		} catch (ProtocolException e) {
			e.printStackTrace();
		}
		if (contentType != null)
			conn.setRequestProperty("Content-Type", contentType);
		if (areCookiesEnabled())
			conn.setRequestProperty("Cookie", mycookies);
		if (getUserAgent() != null)
			conn.setRequestProperty("User-Agent", getUserAgent());
		conn.setDoInput(true);
		if (method != null && method.toUpperCase().equals("POST"))
			conn.setDoOutput(true);
		conn.setUseCaches(false);

		return conn;

	}

	/**
	 * Converts a map of key:value strings to a key1=value1&key2=value2&key3=value3... string
	 * 
	 * @param params
	 *            Map to read
	 * @return Parsed string
	 */
	public static String convertMapToString(Map<String, String> params) {
		String content = "";
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> next = iterator.next();
			if (next.getKey() == null || next.getValue() == null)
				continue;
			if (!content.equals(""))
				content += "&";
			content += ClientUtils.encode(next.getKey() + "") + "=" + ClientUtils.encode(next.getValue() + "");
		}
		return content;
	}

	/**
	 * Takes the cookies from the data returned after a URLConnection was read, and saves it to cookies
	 * 
	 * @param conn
	 *            URLConnection to read cookies off
	 */
	private void saveCookies(URLConnection conn) {
		Map<String, String> localcookies = this.getCookies().getSiteCookies(conn.getURL().toString());
		if (localcookies == null)
			localcookies = new HashMap<String, String>();
		Iterator<Entry<String, List<String>>> entries = conn.getHeaderFields().entrySet().iterator();

		while (entries.hasNext()) {
			Entry<String, List<String>> entry = entries.next();
			String headerName = entry.getKey();
			String value = entry.getValue().size() == 0 ? "" : entry.getValue().get(0);
			if (headerName != null && headerName.equals("Set-Cookie")) {
				String cookieNameAndValue = value.substring(0, value.indexOf(";"));
				String cookieName = cookieNameAndValue.substring(0, cookieNameAndValue.indexOf("="));
				String cookieValue = cookieNameAndValue.substring(cookieNameAndValue.indexOf("=") + 1);
				localcookies.put(cookieName, cookieValue);
			}
		}
		this.getCookies().setCookies(conn.getURL().toString(), localcookies);
	}

	private void saveCookies(String site, String responseHeader) throws IOException {
		Map<String, String> localcookies = this.getCookies().getSiteCookies(site);
		if (localcookies == null)
			localcookies = new HashMap<String, String>();
		BufferedReader r = new BufferedReader(new StringReader(responseHeader));
		String line;
		while ((line = r.readLine()) != null) {
			String headerName = line.substring(0, line.indexOf(":"));
			String value = line.substring(line.indexOf(":") + 1);
			if (headerName != null && headerName.equals("Set-Cookie")) {
				String cookieNameAndValue = value.substring(0, value.indexOf(";"));
				String cookieName = cookieNameAndValue.substring(0, cookieNameAndValue.indexOf("="));
				String cookieValue = cookieNameAndValue.substring(cookieNameAndValue.indexOf("=") + 1);
				localcookies.put(cookieName, cookieValue);
			}
		}
		this.getCookies().setCookies(site, localcookies);
	}

	public class Downloader extends Thread {
		private String site;
		private DownloadProgressHandler dph;
		long bytesRead = 0;
		long maxBytes = -1;
		byte[] bytes;
		long uid;

		public Downloader(String site, DownloadProgressHandler dph, long uid) {
			this.site = site;
			this.dph = dph;
			this.uid = uid;
		}

		@Override
		public void run() {
			InputStream is = null;
			try {
				URL url = new URL(site);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn = initConnection(conn, null, null);
				if (conn.getResponseCode() == -1)
					throw new IllegalStateException("Bad URL? " + site);
				String lengthStr = conn.getHeaderField("content-length");
				if (lengthStr != null) {
					maxBytes = Long.parseLong(lengthStr);
					if (dph != null) {
						dph.sizeDetermined(maxBytes);
					}
				}

				is = conn.getInputStream();

				if (areCookiesEnabled()) {
					saveCookies(conn);
				}
				BufferedInputStream stream = new BufferedInputStream(is);

				final ByteArrayOutputStream byteOut;
				if (maxBytes != -1)
					byteOut = new ByteArrayOutputStream((int) maxBytes);
				else
					byteOut = new ByteArrayOutputStream();
				byte[] buffer = new byte[2056];
				int bytesRead;
				long totalRead = 0;
				if (dph != null) {
					while ((bytesRead = stream.read(buffer)) > 0) {
						try {
							byteOut.write(buffer, 0, bytesRead);
						} catch (java.lang.OutOfMemoryError e) {
							System.err.println("Bad memory... @ " + System.currentTimeMillis());
							System.gc();
						}
						totalRead += bytesRead;
						dph.progressUpdate(totalRead);
						if (!dph.isAlive()) {
							dph.downloadFailed("Terminated by user");
							stream.close();
							return;
						}
					}
				} else {
					while ((bytesRead = stream.read(buffer)) > 0) {
						byteOut.write(buffer, 0, bytesRead);
						totalRead += bytesRead;
					}
				}
				stream.close();
				this.bytes = byteOut.toByteArray();
				byteOut.close();
				if (dph != null)
					dph.downloadComplete(bytes);
			} catch (Exception e) {
				e.printStackTrace();
				if (dph != null)
					dph.downloadFailed(e.getMessage());
			} finally {
				try {
					if (is != null)
						is.close();
				} catch (Exception e) {
				}
			}
			downloaders.remove(this);
		}

		public long getBytesRead() {
			return bytesRead;
		}

		public long getSize() {
			return maxBytes;
		}

		public byte[] getBytes() {
			return bytes;
		}
	}

}