package youtube;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.coleman.utilities.http.DownloadProgressHandler;

public class SongData {
	protected YoutubeVideo youtubeSource;
	protected String songName;
	protected String artist;
	protected String lyrics;
	protected byte[] songBytes;
	protected String link;

	public SongData(byte[] songBytes) {
		this.songBytes = songBytes;
	}

	public SongData(DownloadProgressHandler dph, File file) {
		if (dph != null)
			dph.sizeDetermined(file.length());
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024 * 4];
			int len;
			long totalBytes = 0;
			if (dph != null)
				while ((len = bis.read(buffer)) > 0) {
					baos.write(buffer, 0, len);
					totalBytes += len;
					dph.progressUpdate(totalBytes);
					if(!dph.isAlive()) {
						return;
					}
				}
			else
				while ((len = bis.read(buffer)) > 0) {
					baos.write(buffer, 0, len);
				}
			this.songBytes = baos.toByteArray();
			dph.downloadComplete(this.songBytes);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bis != null)
				try {
					bis.close();
				} catch (IOException e) {
				}
		}
	}

	public SongData() {

	}

	public void setYoutubeSource(YoutubeVideo youtubeSource) {
		this.youtubeSource = youtubeSource;
	}

	public YoutubeVideo getYoutubeSource() {
		return youtubeSource;
	}

	public byte[] getSongBytes() {
		return songBytes;
	}

	public String getSongName() {
		return songName;
	}

	public String getArtist() {
		return artist;
	}

	public String getLyrics() {
		return lyrics;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public void setLyrics(String lyrics) {
		this.lyrics = lyrics;
	}

	public void setSongName(String songName) {
		this.songName = songName;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getLink() {
		return link;
	}

	public String toString() {
		return songName + " by " + artist;
	}
}
