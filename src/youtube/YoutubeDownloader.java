package youtube;

import com.coleman.utilities.http.Client;
import com.coleman.utilities.http.Client.Downloader;
import com.coleman.utilities.http.DownloadProgressHandler;

public abstract class YoutubeDownloader {
	public Client c = new Client();

	/**
	 * Retrieves a string containing a URL for the MP3 file of the given youtube
	 * video
	 * 
	 * @param dph
	 *            DownloadProgressHandler which will be called to give status
	 *            updates on the download
	 * @param video
	 *            Youtube video to download
	 * @return A String containing the URL for the MP3 file
	 */
	public abstract String getMP3URL(DownloadProgressHandler dph,
			YoutubeVideo video);

	/**
	 * Downloads a song given an mp3 download link (used internally after link
	 * has been retrieved)
	 * 
	 * @param dph
	 *            The DPH to give updates to
	 * @param downloadLink
	 *            Link to download from
	 * @return songdata containing the bytes and some other metadata
	 */
	private SongData downloadSong(DownloadProgressHandler dph,
			String downloadLink) {
		long startTime = System.currentTimeMillis();
		System.out.println("Downloading... (link: " + downloadLink + ")");
		System.out.println("Using DPH? " + (dph != null));
		Downloader thread = c.readSiteAsync(downloadLink, dph);
		byte[] bytes = c.waitForComplete(thread);
		if (!dph.isAlive()) {
			return null;
		}
		System.out.println("Downloaded! Took "
				+ (System.currentTimeMillis() - startTime) / 1000.0
				+ " seconds to download");
		return new SongData(bytes);
	}

	/**
	 * Downloads a song given a YoutubeVideo by using this class to retrieve an MP3 link
	 * 
	 * @param dph
	 *            The DPH to give updates to
	 * @param downloadLink
	 *            Link to download from
	 * @return songdata containing the bytes and some other metadata
	 */
	public SongData downloadSong(DownloadProgressHandler dph, YoutubeVideo video) {
		String downloadLink = getMP3URL(dph, video);
		SongData songdata = downloadSong(dph, downloadLink);
		if (!dph.isAlive()) {
			return null;
		}
		songdata.setLink(downloadLink);
		return songdata;
	}
}
