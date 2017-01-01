package youtube;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.coleman.utilities.http.Client;

public class YoutubeVideo {
	protected String videoUrl;
	protected String videoId;
	protected String songArtist;
	protected String songName;
	protected String videoTitle;

	public YoutubeVideo(String videoUrl, String videoId, String songArtist, String songName, String videoTitle) {
		this.videoId = videoId;
		this.videoUrl = videoUrl;
		this.songArtist = songArtist;
		this.songName = songName;
		this.videoTitle = videoTitle;
		this.videoTitle = parseTitle(this.videoTitle);
	}

	public YoutubeVideo(String videoUrl, String songArtist, String songName) {
		this.videoUrl = videoUrl;
		this.videoId = videoUrl.substring(videoUrl.indexOf("?v=") + 3);
		this.videoId = videoId.substring(0, videoId.indexOf("&") > 0 ? videoId.indexOf("&") : videoId.length());
		this.songArtist = songArtist;
		this.songName = songName;
	}

	static Pattern songMetaDataPattern = Pattern.compile(
			"<li class=\"watch-meta-item \">.*?<h4 class=\"title\">.*?Music.*?</h4>.*?<ul class=\"content watch-info-tag-list\">.*?<li>\"(.*?)\" by (.*?) \\(<a href=", Pattern.DOTALL);
	static Pattern videoTitlePattern = Pattern.compile("<span id=\"eow-title\" .*?>\n    (.*?)");
	public YoutubeVideo(String videoUrl) {
		this.videoUrl = videoUrl;
		this.videoId = videoUrl.substring(videoUrl.indexOf("?v=") + 3);
		this.videoId = videoId.substring(0, videoId.indexOf("&") > 0 ? videoId.indexOf("&") : videoId.length());
		Client c = new Client();
		c.setUserAgent("Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0");
		String html = new String(c.readSite(videoUrl));
		Matcher songMetaDataMatcher = songMetaDataPattern.matcher(html);
		Matcher videoTitleMatcher = videoTitlePattern.matcher(html);
		if (songMetaDataMatcher.find()) {
			System.out.println("FOUND METADATA: " + true);
			this.songName = songMetaDataMatcher.group(1).replace("&#39;", "'").replace("Karaoke", "").replace(" - ", "").replace("Live", "").replace("()", "").trim();
			this.songName = songName.replace("������", "���");
			this.songName = songName.replace("/", "-").replace("\\", "-");
			this.songArtist = songMetaDataMatcher.group(2).replace("&#39;", "'").replaceAll("\\(.*?\\)", "");
			this.songArtist = songArtist.replace("/", "-").replace("\\", "-");
			int ftIndex = this.songArtist.indexOf("ft.");
			if (ftIndex == -1)
				ftIndex = this.songArtist.indexOf("featuring");
			if (ftIndex == -1)
				ftIndex = this.songArtist.indexOf("Featuring");
			if (ftIndex == -1)
				ftIndex = this.songArtist.indexOf("Feat.");
			if (ftIndex == -1)
				ftIndex = this.songArtist.indexOf("feat.");
			if (ftIndex != -1)
				this.songArtist = this.songArtist.substring(0, ftIndex);
			this.songArtist = this.songArtist.trim();
		}
		if (videoTitleMatcher.find())
			this.videoTitle = videoTitleMatcher.group(1).replace("&#39;", "'");
		this.videoTitle = parseTitle(this.videoTitle);
	}
	private static final String[] featureStrings = { "ft.", "featuring", "Featuring", "Feat.", "feat.", "feat" };
	public static String parseTitle(String title) {
		if(title == null)
			return "[title missing]";
		title = title.replaceAll("\\(.*?\\)", "");
		title = title.replaceAll("\\[.*?\\]", "");
		if(title.contains("-"))
			title = title.split("-")[0] + " - " + title.split("-")[1];
		if(title.contains("|"))
			title = title.split("\\|")[0] + " - " + title.split("\\|")[1];
		if(title.contains(":"))
			title = title.split(":")[0] + " - " + title.split(":")[1];
		int ftIndex = -1;
		for(String feat : featureStrings) {
			ftIndex = title.indexOf(feat);
			if(ftIndex != -1)
				break;
		}
		if(ftIndex != -1)
			title = title.substring(0, ftIndex);
		
		title = title.replace("lyrics", "");
		title = title.replace("lyric video", "");
		title = title.replace("Lyrics", "");
		title = title.replace("Lyric video", "");
		title = title.replace("Lyric Video", "");
		
		while(title.contains("  "))
			title = title.replace("  ", " ");
		title = title.trim();
		return title;
	}

	public String getSongArtist() {
		return songArtist;
	}

	public String getSongName() {
		return songName;
	}

	public String getVideoTitle() {
		return videoTitle;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public String getVideoId() {
		return videoId;
	}

	public String toString() {
		if(songName == null) {
			return videoTitle + " [" + videoUrl + "]";
		}
		return songName + " by " + songArtist + " [" + videoUrl + "]";
	}
}
