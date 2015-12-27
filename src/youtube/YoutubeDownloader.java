package youtube;

import com.coleman.utilities.http.Client;
import com.coleman.utilities.http.Client.Downloader;
import com.coleman.utilities.http.DownloadProgressHandler;

public abstract class YoutubeDownloader {
	public Client c = new Client();
	public abstract String getMP3URL(DownloadProgressHandler dph, YoutubeVideo video);
	public SongData downloadSong(DownloadProgressHandler dph, String downloadLink) {
		long startTime = System.currentTimeMillis();
		System.out.println("Downloading... (link: " + downloadLink + ")");
		System.out.println("Using DPH? " + (dph != null));
		Downloader thread = c.readSiteAsync(downloadLink, dph);
		byte[] bytes = c.waitForComplete(thread);
		if(!dph.isAlive()) {
			return null;
		}
		System.out.println("Downloaded! Took " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds to download");
		return new SongData(bytes);
	}
	public SongData downloadSong(DownloadProgressHandler dph, YoutubeVideo video) {
		String downloadLink = getMP3URL(dph, video);
		SongData songdata = downloadSong(dph, downloadLink);
		if(!dph.isAlive()) {
			return null;
		}
		songdata.setLink(downloadLink);
		return songdata;
	}
}
