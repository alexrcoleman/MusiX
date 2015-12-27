import java.io.File;
import java.io.IOException;

public class iTunes {
	public static File MusicFolder = new File(System.getProperty("user.home") + "/Music/iTunes/iTunes Media/Music");

	public static File getFile(SongInfo info) {
		File artist = new File(MusicFolder, info.getArtist().replace("/", "_"));
		File album = new File(artist, (info.getAlbum() == null || info.getAlbum().trim().isEmpty()) ? "Unknown Album" : info.getAlbum().replace("/", "_"));
		final File song;
		if (info.getDiscNumber() <= 0)
			song = new File(album, Analytics.validateFileName(info.getTitle()) + ".mp3");
		else
			song = new File(album, Analytics.validateFileName(info.getDiscNumber() + "-" + (info.getTrackNumber() < 10 ? "0" : "") + info.getTrackNumber() + " " + info.getTitle()) + ".mp3");

		return song;
	}

	public static File getTempFile(SongInfo info) {
		File autoAdd = new File(MusicFolder.getParentFile(), "Automatically Add to iTunes");
		File song = new File(autoAdd, Analytics.validateFileName(info.getTitle() + " by " + info.getArtist()) + ".mp3");
		return song;
	}

	public static boolean iTunesExists(SongInfo info) {
		return getFile(info).exists() || getTempFile(info).exists();
	}

	public static void addToiTunes(SongInfo info) throws IOException {
		File file = getTempFile(info);
		file.getParentFile().mkdirs();
		if (file.exists())
			file.delete();
		Analytics.copyFile(info.getPublic(), file);
	}

	public static void removeFromiTunes(SongInfo info) {
		File file = getFile(info);
		if (file.exists())
			file.delete();
		file = getTempFile(info);
		if (file.exists())
			file.delete();
	}
}
