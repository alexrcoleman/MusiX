package youtube;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.coleman.utilities.http.DownloadProgressHandler;

public class YoutubeMP3 extends YoutubeDownloader {
	private static final int __AM = 65521;

	protected int cc(String a) {
		int c = 1, b = 0, d, e;
		for (e = 0; e < a.length(); e++) {
			d = a.charAt(e);
			c = (c + d) % __AM;
			b = (b + c) % __AM;
		}
		return b << 16 | c;
	}

	@Override
	public String getMP3URL(DownloadProgressHandler dph, YoutubeVideo video) {
		// http://www.youtube-mp3.org/a/pushItem/?item=
		// http%3A//www.youtube.com/watch%3Fv%3DYZ4l4YOuDCE
		// &el=na
		// &bf=false
		// &r=1416189314926
		// &s=115163
		/*String url = "";// "http://www.youtube-mp3.org";
		url += "/a/pushItem/?item=http%3A//www.youtube.com/watch%3Fv%3D" + video.getVideoId();
		url += "&el=na";
		url += "&bf=false";
		url += "&r=" + System.currentTimeMillis();
		url += "&s=" + sig(url);
		url = "http://www.youtube-mp3.org" + url;*/
		String url = "http://www.youtube-mp3.org/a/pushItem/";
		url += "?item=https%3A//www.youtube.com/watch%3Fv%3D" + video.getVideoId();
		url += "&el=na";
		url += "&bf=false";
		url += "&r=" + System.currentTimeMillis();
		url += "&s=" + sig(url);
		byte[] b = c.readSite(url);
		if (b == null) {
			if (dph != null) {
				dph.downloadFailed("Invalid conversion protocol, FIX THIS ALEX!");
				return null;
			}
		}
		System.out.println("VIDEO ID SHOULD BE " + new String(b));
		String status = null;
		String h = null;
		String h2 = null;
		String ts_create = null;
		String r = null;
		while (h == null) {
			url = "";// "http://www.youtube-mp3.org";
			// /a/itemInfo/?video_id=" + video_id + "&ac=www&t=grp&r=" + a.getTime();
			url += "/a/itemInfo/" + "?video_id=" + video.getVideoId() + "&ac=www&t=grp&r=" + System.currentTimeMillis();
			url += "&s=" + sig(url);
			url = "http://www.youtube-mp3.org" + url;
			String callback = new String(c.readSite(url));
			System.out.println(callback);
			if (callback.equals("pushItemYTError();")) {
				Youtube.banVideoId(video.getVideoId());
				throw new IllegalArgumentException("Copyright issue, search again...");
			}
			callback = callback.substring(7);
			JSONObject json = new JSONObject(new JSONTokener(callback));
			status = json.getString("status");
			String pf = json.getString("pf");
			if(status.equals("captcha")) {
				dph.downloadFailed("Fucking shit, they gave us a captcha");
				return null;
			}
			if (status.equals("serving")) {
				h = json.getString("h");
				h2 = json.getString("h2");
				try {
					r = URLEncoder.encode(json.getString("r"), "UTF-8");
				} catch (UnsupportedEncodingException|JSONException e) {
					e.printStackTrace();
					dph.downloadFailed("Encoding failed... weird.");
					return null;
				}
				ts_create = "" + json.getInt("ts_create");
			} else
				try {
					System.out.println("Pinging...");
					System.out.println(new String(c.readSite(pf)));
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		// http://www.youtube-mp3.org/get?ab=128&video_id=AZrbW9UT1vE&h=49084a7a143ae3c5136f4e48ae410e9a&r=1386556301051.1457915441
		//String r = a + "." + cc(video.getVideoId() + a);
		///get?video_id=' + video_id + '&h=-1&r=-1.1
		String downloadLink = "/get?video_id=" + video.getVideoId() + "&ts_create=" + ts_create + "&r=" + r + "&h2=" + h2;
		downloadLink += "&s=" + sig(downloadLink);
		downloadLink = "http://www.youtube-mp3.org" + downloadLink;
		return downloadLink;
	}

	
	protected static String gs(int[] I, String[] B) {
		String J = "";
		for (int R = 0; R < I.length; R++) {
			J += B[I[R]];
		}
		return J;
	}

	protected static String gh() {
		return "www.youtube-mp3.org";
	}

	protected static int fn(String[] I, String B) {
		for (int R = 0; R < I.length; R++) {
			if (I[R].equals(B))
				return R;
		}
		return -1;
	}

	protected static int sig(String H) {
		
		HashMap<String, Integer> A = new HashMap<String, Integer>();
		A.put("a",870);
		A.put("b", 906);A.put("c", 167);A.put("d", 119);A.put("e", 130);A.put("f", 899);A.put("g", 248);A.put("h", 123);A.put("i", 627);A.put("j", 706);A.put("k", 694);A.put("l", 421);A.put("m", 214);A.put("n", 561);A.put("o", 819);A.put("p", 925);A.put("q", 857);A.put("r", 539);A.put("s", 898);A.put("t", 866);A.put("u", 433);A.put("v", 299);A.put("w", 137);A.put("x", 285);A.put("y", 613);A.put("z", 635);A.put("_", 638);A.put("&", 639);A.put("-", 880);A.put("/", 687);A.put("=", 721);
		String[] r3 = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
		
		double F = 1.51214;
		double N = 3219;
		for (int Y = 0; Y < H.length(); Y++) {
			String Q = H.substring(Y, Y+1).toLowerCase();
			if (fn(r3, Q) > -1) {
				N = N + (Integer.parseInt(Q) * 121 * F);
			} else {
				if (A.containsKey(Q)) {
					N = N + (A.get(Q) * F);
				}
			}
			N = N * 0.1;
		}
		N = Math.round(N * 1000);
		return (int)N;
	}
	/*
	 * _sig = function(H) {
	 * 
	 * ew = function(I, B) { var P = "K", J = "indexOf"; return I[J](B, b0I[P](I.length, B.length)) !== -1; };
	 * 
	 * fn = function(I, B) { var P = "E", J = "G"; for (var R = 0; b0I[J](R, I.length); R++) { if (b0I[P](I[R], B)) return R;
	 * } return -1; }; var L = [1.23413, 1.51214, 1.9141741, 1.5123114, 1.51214, 1.2651], F = 1; try { F = L[b0I[P3](1, 2)];
	 * var W = gh(), S = gs(X[0], M), T = gs(X[1], M); if (ew(W, S) || ew(W, T)) { F = L[1]; } else { F = L[b0I[d3](5, 3)]; }
	 * } catch (I) { } ; var N = 3219; for (var Y = 0; b0I[z3](Y, H.length); Y++) { var Q = H[n3](Y, 1)[K3](); if (fn(r3, Q)
	 * > -1) { N = N + (b0I[g3](parseInt(Q), 121, F)); } else { if (b0I[N3](Q, A)) { N = N + (b0I[D3](A[Q], F)); } } N =
	 * b0I[e3](N, 0.1); } N = Math[m3](b0I[U](N, 1000)); return N; };
	 */
}
