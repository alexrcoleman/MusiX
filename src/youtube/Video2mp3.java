package youtube;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.coleman.utilities.http.Client;
import com.coleman.utilities.http.DownloadProgressHandler;
import com.coleman.utilities.http.HttpForm;

public class Video2mp3 extends YoutubeDownloader {
	@Override
	public String getMP3URL(DownloadProgressHandler dph, YoutubeVideo video) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SongData downloadSong(DownloadProgressHandler dph, YoutubeVideo video) {
		String youtubeUrl = video.getVideoUrl();
		Client c = new Client();
		c.setUserAgent("Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11");
		HttpForm form = new HttpForm("http://www.video2mp3.net/check.php", "POST");
		form.addFormValue("url", youtubeUrl);
		form.getRequestProperties()
				.put("Cookie",
						"_cc=US; _gip=72.23.198.69; uq=11; CookieACE='6724'; h_348b5035d8c1845cf171ce265a065b52_v=ok; ct_sbi_ctr_v2m=1; ct_sbi_ctr_v2m_exp=Mon, 12 Nov 2012 00:14:59 GMT; __uiv=1; __uis=uuT509eee0e0e3d67.95517340; sb_vc_v2m=1; sb_vc_v2m_exp=Mon, 12 Nov 2012 00:15:01 GMT; __atuvc=1%7C45; __utma=118438286.1260785712.1352592902.1352592902.1352592902.1; __utmb=118438286.1.10.1352592902; __utmc=118438286; __utmz=118438286.1352592902.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); __qca=P0-1424660666-1352592903340");
		form.getRequestProperties().put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		form.getRequestProperties().put("Connection", "keep-alive");
		form.getRequestProperties().put("Cache-Control", "max-age=0");
		
		String result1 = new String(c.httpForm(form));
		Pattern viewPattern = Pattern.compile("\\./view/(.*?)\\|");
		Matcher viewMatcher = viewPattern.matcher(result1);
		if (!viewMatcher.find()) {
			return null;
		}
		String urlSuffix = viewMatcher.group(1);
		String viewUrl = "http://www.video2mp3.net/view/" + urlSuffix;
		String loadUrl = "http://www.video2mp3.net/load/" + urlSuffix;
		String viewCallback = new String(c.readSite(viewUrl));
		
		Pattern cookiePattern = Pattern.compile("document\\.cookie=\"(.*?)\";\n");
		Matcher cookieMatcher = cookiePattern.matcher(viewCallback);
		final Date currentTime = new Date();
		final SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a z");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String nd = sdf.format(currentTime);
		while (cookieMatcher.find()) {
			String cookie = cookieMatcher.group(1);
			cookie = cookie.replace("\"+nd+\"", nd);
			String cookieKey = cookie.substring(0, cookie.indexOf("="));
			String cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie.indexOf(";"));
			c.getCookies().getSiteCookies("http://www.video2mp3.net/").put(cookieKey, cookieValue);
		}
		byte[] songBytes = c.readSite(loadUrl);
		
		SongData song = new SongData(songBytes);
		return song;
	}
}
