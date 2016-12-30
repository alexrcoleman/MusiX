package youtube;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.coleman.utilities.http.DownloadProgressHandler;

public class YoutubeInMP3 extends YoutubeDownloader {
	// http://www.youtubeinmp3.com/widget/button/?video=https://www.youtube.com/watch?v=i62Zjga8JOM
	public static void main(String[] args) {
		YoutubeInMP3 instance = new YoutubeInMP3();
		YoutubeVideo video = new YoutubeVideo("https://www.youtube.com/watch?v=5qfP7a9UcuY");
		System.out.println(instance.getMP3URL(null, video));
	}
	static Pattern downloadLinkPattern = Pattern.compile("<a id=\"downloadButton\" href=\"(.*?)\" .*?>");
	@Override
	public String getMP3URL(DownloadProgressHandler dph, YoutubeVideo video) {
		String url = "http://www.youtubeinmp3.com/widget/button/?video=https://www.youtube.com/watch?v=" + video.getVideoId();
		byte[] b = c.readSite(url);
		if (b == null) {
			if (dph != null) {
				dph.downloadFailed("Invalid conversion protocol, FIX THIS ALEX!");
			}
			return null;
		}
		String html = new String(b);
		Matcher m = downloadLinkPattern.matcher(html);
		if(m.find()) {
			return "http://www.youtubeinmp3.com" + m.group(1);
		}
		
		return null;
	}

	
}
