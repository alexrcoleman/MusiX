package youtube;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.coleman.utilities.http.Client.Downloader;
import com.coleman.utilities.http.ClientUtils;
import com.coleman.utilities.http.DownloadProgressHandler;

import core.Analytics;
import core.DownloadGUI;

public class ManualDownloader extends YoutubeDownloader {
	public static void main(String[] args) {
		ManualDownloader manual = new ManualDownloader();
		SongData sd = manual.downloadSong(null, new YoutubeVideo("http://www.youtube.com/watch?v=KOFw2UPLdPk"));
		System.out.println(sd.songBytes.length);
	}

	@Deprecated
	public String getMP3URL(DownloadProgressHandler dph, YoutubeVideo video) {
		return null;
	}

	public String getVideoURL(DownloadProgressHandler dph, YoutubeVideo video) {
		String html = new String(c.readSite(video.getVideoUrl()));
		Matcher m = VIDEO_PATTERN.matcher(html);
		if (m.find()) {
			// String s = URLDecoder.decode(m.group(1));
			String s = m.group(1);
			s = s.replace("\\u0026", "&");
			String[] ss = s.split(",");
			for (String line : ss) {
				String[] ps = line.split("&");
				HashMap<String, String> map = new HashMap<>();
				for (String p : ps) {
					String[] cs = p.split("=");
					try {
						map.put(cs[0], URLDecoder.decode(cs[1], "UTF-8"));
					} catch (Exception e) {

					}
				}
				// System.out.println(map.get("type"));
				// if(!map.get("type").contains("mp4"))
				
				if (map.get("type").contains("mp4")) {
					System.out.println(map);
					System.out.println(map.get("quality") + " " + map.get("type"));
					if(map.containsKey("s"))
						return map.get("url") + "&signature=" + xm(map.get("s"));
					return map.get("url");
				}
			}
			for (String line : ss) {
				String[] ps = line.split("&");
				HashMap<String, String> map = new HashMap<>();
				for (String p : ps) {
					String[] cs = p.split("=");
					try {
						map.put(cs[0], URLDecoder.decode(cs[1], "UTF-8"));
					} catch (Exception e) {
					}
				}
				System.out.println(map);
				System.out.println(map.get("quality") + " " + map.get("type"));
				if(map.containsKey("s"))
					return map.get("url") + "&signature=" + xm(map.get("s"));
				return map.get("url");
			}
			System.out.println("couldn't find a video?");
		} else {
			System.out.println("failed to match pattern, html:");
			System.out.println("[[" + html + "]]");
		}

		return null;
	}

	@Override
	public SongData downloadSong(DownloadProgressHandler dph, YoutubeVideo video) {
		try {
			File audioFile = new File(Analytics.getPrivateFolder(),
					"cache/_" + Analytics.validateFileName(video.getVideoId()) + ".mp3");
			File videoFile = new File(Analytics.getPrivateFolder(),
					"cache/_" + Analytics.validateFileName(video.getVideoId()) + ".vid");
			if (videoFile.exists())
				videoFile.delete();
			if (audioFile.exists())
				audioFile.delete();
			videoFile.createNewFile();
			videoFile.deleteOnExit();
			audioFile.deleteOnExit();
			if (dph != null)
				dph.updateStatus("Fetching video URL");
			String videoURL = getVideoURL(dph, video);
			if (dph != null)
				dph.updateStatus("Downloading video " + videoURL);
			Downloader thread = c.readSiteAsync(videoURL, dph);
			byte[] bs = c.waitForComplete(thread);
			if (dph != null && !dph.isAlive()) {
				return null;
			}
			if (dph != null)
				dph.updateStatus("Writing video to file");
			try {
				FileOutputStream fos = new FileOutputStream(videoFile);
				fos.write(bs);
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
				if (dph != null) {
					dph.downloadFailed(e.getMessage());
				}
				return null;
			}
			try {
				if (dph != null)
					dph.updateStatus("Converting to MP3");
				convertToMP3(videoFile, audioFile);
				if (!audioFile.exists()) {
					throw new IllegalStateException("Audio conversion failed");
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (dph != null) {
					dph.downloadFailed(e.getMessage());
				}
				return null;
			}
			if (dph != null)
				dph.updateStatus("Reading back audio");
			FileInputStream fis = new FileInputStream(audioFile);
			bs = ClientUtils.readInputStream(fis);
			return new SongData(bs);
		} catch (Exception e) {
			e.printStackTrace();
			if (dph != null) {
				dph.downloadFailed(e.getMessage());
			}
			return null;
		}

	}

	static void convertToMP3(File in, File out) throws IOException {
		File ffmpeg = new File(Analytics.getPrivateFolder(), "ffmpeg.exe");
		if (!ffmpeg.exists()) {
			if(!extractResource("core/resources/ffmpeg.exe", ffmpeg))
				return;
		}
		ProcessBuilder pb = new ProcessBuilder(ffmpeg.getAbsolutePath(), "-i", in.getAbsolutePath(), "-b:a", "256k",
				out.getAbsolutePath());
		pb.redirectErrorStream(true);
		Process p = pb.start();
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while ((line = br.readLine()) != null) {
			// System.out.println(line);
		}
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	static final Pattern VIDEO_PATTERN = Pattern.compile("\"url_encoded_fmt_stream_map\":\"(.*?)\"");

	static int getRating(String quality) {
		if (quality.equals("small"))
			return 144;
		if (quality.equals("medium"))
			return 360;
		if (quality.startsWith("hd"))
			return Integer.parseInt(quality.substring(2));
		return 0;
	}

	static String xm(String a) {
		char[] cs = a.toCharArray();
		zO(cs, 47);
		cs = vY(cs, 1);
		z9(cs, 68);
		zO(cs, 21);
		z9(cs, 34);
		zO(cs, 16);
		z9(cs, 41);
		return new String(cs);
	}

	static void z9(char[] a, int b) {
		// reverse a
		for (int i = 0; i < a.length / 2; i++) {
			int j = a.length - 1 - i;
			a[i] ^= a[j] ^ (a[j] = a[i]);
		}
	}

	static void zO(char[] a, int b) {
		char c = a[0];
		a[0] = a[b % a.length];
		a[b] = c;
	}

	static char[] vY(char[] cs, int b) {
		char[] ncs = new char[cs.length - b];
		for (int i = b; i < cs.length; i++)
			ncs[i - b] = cs[i];
		return ncs;
	}

	static boolean extractResource(String resource, File dest) {
		try {
			URL url = ClassLoader.getSystemClassLoader().getResource(resource);
			InputStream in = url.openStream();
			if (dest.exists())
				dest.delete();
			dest.createNewFile();
			FileOutputStream out = new FileOutputStream(dest);
			ClientUtils.readInputStream(in, out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			dest.delete();
			return false;
		}
		return true;
	}
}
