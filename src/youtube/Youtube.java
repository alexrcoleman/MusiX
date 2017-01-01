package youtube;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.coleman.utilities.http.Client;
import com.coleman.utilities.http.ClientUtils;
import com.coleman.utilities.http.HttpForm;

public class Youtube {
	public static final String API_KEY = "AIzaSyA0ZZ3q8SHM2Brj732QZlCx9kAOzmWEzi0";
	public static HashSet<String> bannedVideos = new HashSet<>();

	public static void banVideoId(String id) {
		bannedVideos.add(id);
	}

	public static YoutubeVideo searchVideo(String search) {
		if (search.startsWith("youtube.com"))
			search = "www." + search;
		if (search.startsWith("www."))
			search = "http://" + search;

		String gdataURL = null;
		boolean isSpecific = false;
		if (search.startsWith("http://www.youtube.com")
				|| search.startsWith("https://www.youtube.com")) {
			Matcher m = Pattern.compile("\\?v=(.*?)($|&)").matcher(search);
			if (m.find()) {
				System.out.println(m.group(2));
				gdataURL = "https://www.googleapis.com/youtube/v3/videos?part=snippet&id="
						+ m.group(1) + "&key=" + Youtube.API_KEY;
				isSpecific = true;
			}
		}
		ArrayList<YoutubeVideo> videos = new ArrayList<YoutubeVideo>();
		long startTimeCode = System.currentTimeMillis();
		if (!isSpecific) {
			search = ClientUtils.encode(search.replace(' ', '+'));
			Client c = new Client();
			c.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			String url = "https://www.googleapis.com/youtube/v3/search?";
			url += "&key=" + Youtube.API_KEY;// "&key=AIzaSyABOH8EWlzcCR3OqzELJAhxYBdU-ncC7yk";
			url += "&type=video";
			url += "&part=snippet";
			url += "&videoCategoryId=10";
			url += "&order=relevance";
			url += "&maxResults=5";
			url += "&q=" + search;

			HttpForm form = new HttpForm(url, "GET");
			String callback = new String(c.httpForm(form));
			JSONObject json = new JSONObject(new JSONTokener(callback));
			// JSONObject feed = json.getJSONObject("feed");
			JSONArray items = json.getJSONArray("items");
			int audioIndex = 0;
			int lyricIndex = 0;
			int otherIndex = 0;
			int videoIndex = 0;
			int liveIndex = 0;
			
			for (int i = 0; i < items.length(); i++) {
				JSONObject item = items.getJSONObject(i);
				JSONObject id = item.getJSONObject("id");
				JSONObject snippet = item.getJSONObject("snippet");
				String title = snippet.getString("title");
				String videoid = id.getString("videoId");
				if (bannedVideos.contains(videoid))
					continue;
				YoutubeVideo video = new YoutubeVideo(
						"https://www.youtube.com/watch?v=" + videoid, videoid,
						null, null, title);
				if (title.toLowerCase().contains("official audio")) {
					videos.add(audioIndex, video);
					audioIndex++;
					lyricIndex++;
					otherIndex++;
					liveIndex++;
					videoIndex++;
				} else if (title.toLowerCase().contains("lyric")) {
					videos.add(lyricIndex, video);
					lyricIndex++;
					otherIndex++;
					liveIndex++;
					videoIndex++;
				} else if (title.toLowerCase().contains("video")) {
					videos.add(videoIndex, video);
					liveIndex++;
					videoIndex++;
				} else if ((!search.toLowerCase().contains("live") && title.toLowerCase().contains("live")) || (!search.toLowerCase().contains("acoustic") && title.toLowerCase().contains("acoustic"))) {
					videos.add(liveIndex, video);
					liveIndex++;
				} else {
					videos.add(otherIndex, video);
					otherIndex++;
					liveIndex++;
					videoIndex++;
				}
				System.out.println(videos);
			}
		} else {
			Client c = new Client();
			c.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			String url = gdataURL;

			HttpForm form = new HttpForm(url, "GET");
			String callback = new String(c.httpForm(form));
			JSONObject json = new JSONObject(new JSONTokener(callback));
			JSONArray items = json.getJSONArray("items");
			JSONObject item = items.getJSONObject(0);
			JSONObject snippet = item.getJSONObject("snippet");
			String title = snippet.getString("title");
			String videoid = item.getString("id");
			YoutubeVideo video = new YoutubeVideo(
					"https://www.youtube.com/watch?v=" + videoid, videoid, null,
					null, title);
			videos.add(video);
		}
		System.out.println(videos);
		System.out.println("Took "
				+ (System.currentTimeMillis() - startTimeCode) / 1000.0
				+ " seconds to find video");
		return videos.get(0);
	}
}
