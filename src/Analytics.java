import com.coleman.utilities.http.Client;
import com.coleman.utilities.http.ClientUtils;
import com.coleman.utilities.http.DownloadProgressHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.valuepair.ImageFormats;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import utilities.database.FileDatabase;
import youtube.SongData;
import youtube.Youtube;
import youtube.YoutubeMP3;
import youtube.YoutubeVideo;

/**
 * This example will demonstrate uploading an MP3, analyzing it and getting the audio summary and the timing info for all of
 * the beats
 * 
 * @author plamere
 */
public class Analytics {
	public static Client client = new Client();
	static {
		client.setUserAgent("Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36");
	}
	public static File iTunesMusic = new File(System.getProperty("user.home") + "/Music/iTunes/iTunes Media/Music");

	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

	public static File getPrivateFolder() {
		File appdata;
		if (isWindows())
			appdata = new File(System.getenv("AppData"));
		else
			appdata = new File(System.getProperty("user.home") + "/Library/Application Support/");
		appdata = new File(appdata, "YoutubeToMP3");
		appdata.mkdirs();
		return appdata;
	}

	public static File getPublicFolder() {
		File f = new File(System.getProperty("user.home") + "/Desktop/Music/");
		f.mkdirs();
		return f;
	}

	public static SongInfo downloadSong(DownloadProgressHandler dph, String keywords) throws IOException {
		return downloadSong(dph, keywords, null);
	}

	public static SongInfo downloadSong(DownloadProgressHandler dph, String keywords, File audio) throws IOException {
		// http://www.youtube.com/watch?v=Q67UtuUgTpo
		YoutubeVideo video = Youtube.searchVideo(keywords);
		File cacheFile = new File(getPrivateFolder(), "cache/" + validateFileName(video.getVideoId()) + ".mp3");
		System.out.println(video.getVideoUrl());
		cacheFile.getParentFile().mkdirs();
		if (audio == null)
			audio = cacheFile;
		SongData song;
		if (cacheFile.exists() && cacheFile.getTotalSpace() > 0) {
			song = new SongData(dph, cacheFile);
			if (dph.isCancelled())
				return null;
		} else {
			song = new YoutubeMP3().downloadSong(dph, video);
			if (dph.isCancelled())
				return null;
			if (song == null || song.getSongBytes() == null) {
				System.err.println("Null song reference...");
				return null;
			}
			long startTimeCode = System.currentTimeMillis();
			cacheFile.createNewFile();
			FileOutputStream out = new FileOutputStream(cacheFile);
			try {
				out.write(song.getSongBytes());
			} catch (IOException e) {
				out.close();
				throw e;
			}
			out.close();
			if (!dph.isAlive()) {
				return null;
			}
			System.out.println("Took " + (System.currentTimeMillis() - startTimeCode) / 1000.0 + " seconds to save audio");
		}
		song.setYoutubeSource(video);

		if (audio != cacheFile) {
			copyFile(cacheFile, audio);
		}
		SongInfo info = new SongInfo();
		info.setCache(audio);
		info.setYoutubeVideo(video);
		info.setLink(song.getLink());
		return info;
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	public static SongInfo downloadAndProcess(DownloadProgressHandler dph, String keywords) {
		return downloadAndProcess(dph, keywords, null);
	}

	public static String validateFileName(String fileName) {
		if (System.getProperty("os.name").startsWith("Windows")) {
			return fileName.replace("/", "-").replace("\\", "-").replace(":", "-").replace("*", "-").replace("?", "-").replace("<", "-").replace(">", "-").replace("|", "-");
		}
		return fileName.replace(":", "-").replace("/", "-").replace("\\", "-");
	}

	public static SongInfo downloadAndProcess(DownloadProgressHandler dph, String keywords, SongInfo extraInfo) {
		try {
			System.out.println("Initiating download");
			SongInfo oldInfo = downloadSong(dph, keywords);
			if (!dph.isAlive() || oldInfo == null)
				return null;
			File cache = oldInfo.getCache();
			System.out.println("Download complete. Processing file...");
			if (extraInfo != null) {
				extraInfo.setCache(oldInfo.getCache());
				extraInfo.setLink(oldInfo.getLink());
				extraInfo.setYoutubeVideo(oldInfo.getYoutubeVideo());
				oldInfo = extraInfo;
			}
			SongInfo info = process(dph, oldInfo.getYoutubeVideo(), oldInfo);
			if (info == null) {
				return null;
			}
			//if (!dph.isAlive())
			//	return null;
			info.setYoutubeVideo(oldInfo.getYoutubeVideo());
			info.setCache(cache);
			System.out.println("Process complete. Renaming file... (" + info.getTitle() + " by " + info.getArtist() + ")");

			File newFile = new File(new File(getPublicFolder(), validateFileName(info.getArtist())), validateFileName(info.getTitle()) + ".mp3");
			info.setPublic(newFile);
			newFile.getParentFile().mkdirs();
			if (newFile.exists()) // TODO: Maybe make this optional
				newFile.delete();
			copyFile(cache, newFile);
			return info;
		} catch (IOException e) {
			dph.uploadFailed("IO Error: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			if (dph != null)
				dph.uploadFailed("Random Error: " + e.getLocalizedMessage());
		}
		return null;
	}

	public static FileDatabase<Object, Object> database = new FileDatabase<Object, Object>(new File(getPrivateFolder().toString(), "database.dat"));
	static {
		// System.out.println("> " + database.getMap());
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Saving database...");
				database.writeToFile();
				// System.out.println("< " + database.getMap());
			}
		});
	}

	public static String capitalize(String s) {
		s = StringUtils.capitalize(s.toLowerCase());
		if (s.toUpperCase().equals(s))
			s = s.toLowerCase();
		return s;
	}


	public static SongInfo process(DownloadProgressHandler dph, YoutubeVideo video, SongInfo basicInfo) throws Exception {
		//EchoNestAPI en = new EchoNestAPI("6P55ZC4OQHQI5HHNZ");
		//en.setDPH(dph);
		File file = basicInfo.getCache();
		try {
			MP3File audio = new MP3File(file);
			Tag tag = audio.getTag();
			/*
			 * if(tag.getValue(FieldKey.LYRICS, 1).length() > 0 && tag.getValue(FieldKey.LYRICS, 0).length() > 0) {
			 * System.out.println("----"); String real = tag.getValue(FieldKey.LYRICS, 1); tag.deleteField(FieldKey.LYRICS);
			 * tag.setField(FieldKey.LYRICS, real); audio.commit(); }
			 */
			if (tag != null && !tag.isEmpty() && tag.getFirst(FieldKey.ARTIST).length() > 0 && tag.getFirst(FieldKey.ALBUM).length() > 0) {
				//Track track = en.uploadTrack(file);
				SongInfo info = new SongInfo();
				info.setAlbum(tag.getFirst(FieldKey.ALBUM));
				info.setArtist(tag.getFirst(FieldKey.ARTIST));
				info.setTitle(tag.getFirst(FieldKey.TITLE));
				info.setLyrics(tag.getFirst(FieldKey.LYRICS));
				info.setYoutubeVideo(new YoutubeVideo(tag.getFirst(FieldKey.CUSTOM2), info.getArtist(), info.getTitle()));
				info.setAlbumArtwork(tag.getArtworkList().get(tag.getArtworkList().size() - 1).getBinaryData());
				return info;
			}
		} catch (Exception e1) {
		}
		System.out.println("Processing " + file.getName());
		if (video.getSongName() != null) {
			basicInfo.setTitle(video.getSongName());
			basicInfo.setArtist(video.getSongArtist());
			System.err.println("Using video song info");
		} else if (video.getVideoTitle().contains(" - ")) {
			basicInfo.setTitle(video.getVideoTitle().split(" - ")[1].replaceAll("\\(.*?\\)", "").trim());
			basicInfo.setArtist(video.getVideoTitle().split(" - ")[0].replaceAll("\\(.*?\\)", "").trim());
			System.err.println("Using video title ( - )");
		} else {
			basicInfo.setTitle("Unknown Song");
			basicInfo.setArtist("Unknown Artist");
		}
		SongInfo info = setMP3Tags(video, basicInfo);

		return info;
		/*try {
			System.out.println("Uploading file");
			Track track = null;
			try {
				track = en.uploadTrack(file);
				// track.fetchBucket("id:spotify-WW");
				// Force set info here:
				// track.setArtist("Preston Pohl");
				// track.setTitle("Electric Feel");
				System.out.println("Upload complete!");
				dph.uploadComplete();
				dph.updateStatus("Waiting for analysis");

				try {
					track.waitForAnalysis(30000);
				} catch (EchoNestException e) {
					e.printStackTrace();
				}
				dph.updateStatus("Loading track information");
				System.out.println("Analysis complete!");
			} catch (Exception e) {
				// track = null;
				track = new Track(en);
				e.printStackTrace();
				System.out.println("Error uploading track (" + e.getMessage() + ")");

			}
			boolean echoNestHasMatch = !track.isError() && track.getTitle() != null;

			if (!echoNestHasMatch) {
				if (video.getSongName() != null) {
					track.setTitle(video.getSongName());
					track.setArtist(video.getSongArtist());
					System.err.println("Using video song info");
				} else if (video.getVideoTitle().contains(" - ")) {
					track.setTitle(video.getVideoTitle().split(" - ")[1].replaceAll("\\(.*?\\)", "").trim());
					track.setArtist(video.getVideoTitle().split(" - ")[0].replaceAll("\\(.*?\\)", "").trim());
					System.err.println("Using video title ( - )");
				} else if (video.getVideoTitle().contains("-")) {
					track.setTitle(video.getVideoTitle().split("-")[1].replaceAll("\\(.*?\\)", "").trim());
					track.setArtist(video.getVideoTitle().split("-")[0].replaceAll("\\(.*?\\)", "").trim());
					System.err.println("Using video title (-)");
				} else if (video.getVideoTitle().contains(":")) {
					track.setTitle(video.getVideoTitle().split(":")[1].replaceAll("\\(.*?\\)", "").trim());
					track.setArtist(video.getVideoTitle().split(":")[0].replaceAll("\\(.*?\\)", "").trim());
					System.err.println("Using video title (:)");
				} else if (video.getVideoTitle().contains("|")) {
					track.setTitle(video.getVideoTitle().split("|")[1].replaceAll("\\(.*?\\)", "").trim());
					track.setArtist(video.getVideoTitle().split("|")[0].replaceAll("\\(.*?\\)", "").trim());
					System.err.println("Using video title (|)");
				} else {
					track.setTitle("Unknown Song");
					track.setArtist("Unknown Artist");
				}
				System.err.println("No title found - " + track.getTitle() + " / " + track.getAnalysisURL());
			} else if (track.getTitle().equals(video.getVideoId() + ".mp3")) {
				track.setTitle("Unknown Title");
				track.setArtist("Unknown Artist");
				System.err.println("Video ID found as title");
			}*/
	}

	public static boolean addSpotifyData(String title, String artist, SongInfo basicInfo, Tag tag, boolean inquire) throws Exception {
		String search;
		if(title.equals("Unknown Song")) {
			search = basicInfo.getYoutubeVideo().getVideoTitle();
		} else {
			search = title + " " + artist;
		}
		search = search.replaceAll("\\[.*?\\]", "").trim().replace(" ", "%20").replace("'", "%27").replace("&", "%26");
		byte[] bytes = client.readSite("https://api.spotify.com/v1/search?type=track&q=" + search);
		boolean spotify = false;
		// System.out.println("SpOtIfY||| " + track.getForeignID());
		if (bytes != null) {
			String callback = new String(bytes);
			JSONArray json = new JSONObject(new JSONTokener(callback)).getJSONObject("tracks").getJSONArray("items");
			if (json.length() > 0) {
				for (int i = 0; i < json.length(); i++) {
					JSONObject obj = json.getJSONObject(i);
					boolean hasSpotifyID = basicInfo.getSpotifyID() != null;
					boolean passesSpotifyID = basicInfo.getSpotifyID() != null && basicInfo.getSpotifyID().equals(obj.getString("href"));
					boolean isExactMatch = obj.getString("name").equals(title) && obj.getJSONArray("artists").getJSONObject(0).getString("name").equals(artist);
					isExactMatch |= obj.getString("name").equals(artist) && obj.getJSONArray("artists").getJSONObject(0).getString("name").equals(title);
					if(obj.getJSONObject("album").getString("name").toLowerCase().contains("greatest hits"))
						continue;
					if ((hasSpotifyID && passesSpotifyID)
							|| (!hasSpotifyID && (/*isExactMatch || */(inquire && i < 3 && 0 == JOptionPane.showConfirmDialog(DownloadGUI.instance,
									"\"" + obj.getString("name") + "\" by " + obj.getJSONArray("artists").getJSONObject(0).getString("name") + " on \"" + obj.getJSONObject("album").getString("name") + "\"", "Confirm spotify song match", JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE))))) {
						
						JSONObject albumData = new JSONObject(new JSONTokener(new String(client.readSite("https://api.spotify.com/v1/albums/" + obj.getJSONObject("album").getString("id")))));
						JSONArray albumArr = albumData.getJSONObject("tracks").getJSONArray("items");
						int maxDisc = obj.getInt("disc_number");
						int maxTrack = obj.getInt("track_number");
						for(int k=0;k<albumArr.length();k++) {
							JSONObject track = albumArr.getJSONObject(k);
							maxDisc = Math.max(maxDisc, track.getInt("disc_number"));
							if(track.getInt("disc_number") == obj.getInt("disc_number"))
								maxTrack = Math.max(maxTrack, track.getInt("track_number"));
						}
						
						String href = obj.getString("href");
						basicInfo.setTitle(obj.getString("name"));
						basicInfo.setArtist(obj.getJSONArray("artists").getJSONObject(0).getString("name"));
						
						basicInfo.setSpotifyID(href);
						System.out.println("Spotify href: " + href);
						// JSONObject oEmbed = new JSONObject(new JSONTokener(new String(client.readSite("https://embed.spotify.com/oembed/?url=" + href))));
						basicInfo.setAlbum(obj.getJSONObject("album").getString("name"));
						byte[] imageBytes = client.readSite(obj.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url"));
						basicInfo.setAlbumArtwork(imageBytes);
						basicInfo.setYear(Integer.parseInt(albumData.getString("release_date").split("-")[0]));
						basicInfo.setDiscNumber(obj.getInt("disc_number"));
						basicInfo.setDiscMax(maxDisc);
						basicInfo.setTrackNumber(obj.getInt("track_number"));
						basicInfo.setTrackMax(maxTrack);
						basicInfo.setRating(obj.getInt("popularity")/100.0);
						System.out.println("Popularity: " + obj.getInt("popularity"));
						
						Artwork artwork = ArtworkFactory.getNew();
						artwork.setBinaryData(imageBytes);
						artwork.setMimeType(ImageFormats.getMimeTypeForBinarySignature(imageBytes));
						artwork.setDescription("Album artwork");
						artwork.setPictureType(0x03);
						tag.setField(artwork);
						basicInfo.setLyrics(addLyrics(tag, client, basicInfo.getTitle(), basicInfo.getArtist()));

						spotify = true;
						break;
					}
				}
			}
		}
		if (spotify) {
			if (basicInfo.getTrackNumber() > 0) {
				tag.setField(FieldKey.TRACK, String.valueOf(basicInfo.getTrackNumber()));
				tag.setField(FieldKey.TRACK_TOTAL, String.valueOf(basicInfo.getTrackMax()));
				tag.setField(FieldKey.DISC_NO, String.valueOf(basicInfo.getDiscNumber()));
				tag.setField(FieldKey.DISC_TOTAL, String.valueOf(basicInfo.getDiscMax()));
				System.out.println("DISC | TRACK: " + basicInfo.getDiscNumber() + "/" + basicInfo.getDiscMax() + " | " + basicInfo.getTrackNumber() + "/" + basicInfo.getTrackMax());
			}
			tag.setField(FieldKey.TITLE, basicInfo.getTitle());
			tag.setField(FieldKey.ARTIST, basicInfo.getArtist());
			tag.setField(FieldKey.ALBUM, String.valueOf(basicInfo.getAlbum()));
			System.out.println("ALBUM: " + basicInfo.getAlbum());
			tag.setField(FieldKey.RATING, String.valueOf((int) (basicInfo.getRating() * 255)));
			tag.setField(FieldKey.YEAR, String.valueOf(basicInfo.getYear()));
		}
		return spotify;
	}

	public static SongInfo setMP3Tags(YoutubeVideo video, SongInfo basicInfo) throws Exception {
		File file = basicInfo.getCache();
		MP3File audio = new MP3File(file);
		String title = basicInfo.getTitle();
		String artist = basicInfo.getArtist();
		
		Tag tag = audio.getTagOrCreateAndSetDefault();

		boolean spotify = addSpotifyData(title, artist, basicInfo, tag, true);
		
		title = basicInfo.getTitle();
		artist = basicInfo.getArtist();
		
		tag.setField(FieldKey.TITLE, title);
		System.out.println("TITLE: " + title);
		// }
		tag.setField(FieldKey.ARTIST, artist);
		System.out.println("ARTIST: " + artist);

		if (basicInfo.getTrackNumber() > 0 && !spotify) {
			if (basicInfo.getTrackNumber() > 0) {
				tag.setField(FieldKey.TRACK, String.valueOf(basicInfo.getTrackNumber()));
				tag.setField(FieldKey.DISC_NO, String.valueOf(basicInfo.getDiscNumber()));
				System.out.println("DISC/TRACK: " + basicInfo.getDiscNumber() + "/" + basicInfo.getTrackNumber());
			}
			tag.setField(FieldKey.ALBUM, String.valueOf(basicInfo.getAlbum()));
			System.out.println("ALBUM: " + basicInfo.getAlbum());
			tag.setField(FieldKey.RATING, String.valueOf(Math.round(basicInfo.getRating() * 255)));
			tag.setField(FieldKey.YEAR, String.valueOf(basicInfo.getYear()));
		}

		tag.setField(FieldKey.CUSTOM2, video.getVideoUrl());
		System.out.println("VIDEO: " + video.getVideoUrl());
		// if (tag.getFirst(FieldKey.ARTIST).length() > 0) {
		// artist = tag.getFirst(FieldKey.ARTIST);
		// System.out.println("Ignoring artist");
		// } else {
		// }
		SongInfo info = basicInfo;
		System.out.println("RETURNING: " + info);
		/*if (!basicInfo.getTitle().equals("Unknown Song") && !basicInfo.getArtist().equals("Unknown Artist") && !spotify) {
			System.out.println("Looking up extra info since spotify failed");
			client.setUserAgent("Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			info = addAlbumInfo(tag, client, title, artist);
			if (info == null)
				info = new SongInfo();
			String lyrics = addLyrics(tag, client, title, artist);
			info.setLyrics(lyrics);
		}*/
		audio.commit();
		return info;
	}

	public static String addLyrics(Tag tag, Client httpClient, String title, String artist) throws KeyNotFoundException, FieldDataInvalidException {
		String lyrics = null;
		try {
			if (tag.hasField(FieldKey.LYRICS))
				lyrics = tag.getValue(FieldKey.LYRICS, 0);
		} catch (Exception e) {

		}
		if (lyrics == null || lyrics.isEmpty()) {
			lyrics = Lyrics.search(title.replaceAll("\\(.*?\\)", "").replaceAll("\\[.*?\\]", "").replace("  ", " ").trim(), artist);
			if (lyrics != null) {
				tag.setField(FieldKey.LYRICS, lyrics);
				// System.out.println("Updating lyrics to " + lyrics);
			} else {
				// System.out.println("Couldnt find lyrics");
			}
		} else {
			// System.out.println("Ignoring lyrics");
		}
		return lyrics;

	}

	public static SongInfo addAlbumInfo(Tag tag, Client httpClient, String title, String artist) throws FieldDataInvalidException {
		String url = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=8c89aa4877448aa3ac58b7ade01ae42a" + "&artist=" + ClientUtils.encode(artist) + "&track="
				+ ClientUtils.encode(title.replaceAll("\\(.*?\\)", "").replaceAll("\\[.*?\\]", "").replace("  ", " ").trim()) + "&format=json";
		String callback = new String(client.readSite(url));
		JSONObject object = new JSONObject(new JSONTokener(callback));
		if (!object.has("track")) {
			return null;
		}
		JSONObject track = object.getJSONObject("track");
		if (track.has("album")) {
			JSONObject album = track.getJSONObject("album");
			JSONArray images = album.getJSONArray("image");
			String albumTitle = album.getString("title");
			tag.setField(FieldKey.ALBUM, albumTitle);
			String artworkURL = images.getJSONObject(images.length() - 1).getString("#text");
			if (tag.getArtworkList().size() == 0) {
				byte[] imageBytes = httpClient.readSite(artworkURL);

				Artwork artwork = ArtworkFactory.getNew();
				artwork.setBinaryData(imageBytes);
				artwork.setMimeType(ImageFormats.getMimeTypeForBinarySignature(imageBytes));
				artwork.setDescription("Album artwork");
				artwork.setPictureType(0x03);
				tag.setField(artwork);
			} else {
				// System.out.println("Ignoring artwork");
			}
			// if (tag.getFirst(FieldKey.ALBUM).length() > 0) {
			// System.out.println("Ignoring album");
			// } else {
			// tag.setField(FieldKey.ALBUM, albumTitle);
			System.out.println("ALBUM: " + albumTitle);
			// }
			SongInfo info = new SongInfo();
			info.setAlbum(albumTitle);
			info.setAlbumArtwork(tag.getArtworkList().get(tag.getArtworkList().size() - 1).getBinaryData());
			return info;
		}
		return null;

	}
}