import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.coleman.utilities.http.Client;
import com.coleman.utilities.http.ClientUtils;


public class Lyrics {
	public static Client c = new Client();
	private static HashMap<String,String> memo = new HashMap<>();
	public static String search(String song, String artist) {
		
		song = ClientUtils.encode(song);
		artist = ClientUtils.encode(artist);
		String url = String.format("http://search.azlyrics.com/search.php?q=%s+%s", song, artist);
		url = url.toLowerCase();
		if(memo.containsKey(url)) {
			return memo.get(url);
		}
		//String url = String.format("http://lyricalizer.ac3xx.com/?song=%s&artist=%s", song, artist);
		byte[] bytes = c.readSite(url);
		if(bytes == null) {
			return null;
		}
		String searchResults = new String(bytes);
		// System.out.println(searchResults);
		Pattern p = Pattern.compile("1. <a href=\"(.*?)\" target=\"_blank\"><b>", Pattern.DOTALL);
		Matcher m = p.matcher(searchResults);
		if(!m.find())
			return null;
		String lyricsURL = m.group(1);
		// System.out.println(lyricsURL);
		bytes = c.readSite(lyricsURL);
		String lyricsResult;
		try {
			lyricsResult = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		// System.out.println(lyricsResult);
		p = Pattern.compile("<div>(.*?)\n</div>", Pattern.DOTALL);
		m = p.matcher(lyricsResult);
		if(!m.find())
			return null;
		String lyrics = m.group(1).trim();
		lyrics = lyrics.replaceAll("<.*?>", "");
		lyrics = lyrics.replace("’", "'");
		lyrics = lyrics.replace("&quot;", "\"");
		System.out.println(lyrics);
		
		lyrics = lyrics.replace("���������", "'");
		lyrics = lyrics.trim();
		memo.put(url, lyrics);
		return lyrics;
		//System.out.println("LYRICS: " + callback);
		//Pattern p = Pattern.compile("<Lyric>(.*?)</Lyric>", Pattern.DOTALL);
		//Matcher m = p.matcher(callback);
		//if(m.find()) {
		//	return m.group(1).replace("���������", "'");
		//}
		//return null;
	}
	public static void main(String[] args) {
		System.out.println(Lyrics.search("I am", "AWOLNATION"));
	}
}
