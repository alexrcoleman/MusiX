import java.io.File;
import java.io.IOException;

public class iTunes {
	/**
	 * iTunes media folder holding stuff for the iTunes library
	 */
	public static File MEDIA_FOLDER = new File(System.getProperty("user.home")
			+ "/Music/iTunes/iTunes Media/");
	/**
	 * Music folder where artist folders lie
	 */
	public static File MUSIC_FOLDER = new File(MEDIA_FOLDER, "Music");
	/**
	 * Folder where you can drop mp3 files to haev them added to your library
	 */
	public static File ADD_FOLDER = new File(MEDIA_FOLDER,
			"Automatically Add to iTunes");

	/**
	 * Retrieves the file for a given song by looking for it in the iTunes music
	 * folder
	 * 
	 * @param info
	 *            Song to look for
	 * @return File of the mp3 file in the music folder
	 */
	public static File getFile(SongInfo info) {
		File artist = new File(MUSIC_FOLDER, info.getArtist().replace("/", "_"));
		File album = new File(artist, (info.getAlbum() == null || info
				.getAlbum().trim().isEmpty()) ? "Unknown Album" : info
				.getAlbum().replace("/", "_"));
		final File song;
		String filename;
		System.out.println(info.getDiscNumber() + " / " +info.getDiscMax());
		System.out.println(info.getTrackNumber() + " / " +info.getTrackMax());
		if(info.getDiscMax() > 1) {
			filename = info.getDiscNumber() + "-"
					+ (info.getTrackNumber() < 10 ? "0" : "")
					+ info.getTrackNumber() + " " + info.getTitle();
		} else if(info.getDiscMax() == 1) {
			filename = (info.getTrackNumber() < 10 ? "0" : "")
					+ info.getTrackNumber() + " " + info.getTitle();
		} else {
			filename = info.getTitle();
		}
		song = new File(album,Analytics.validateFileName(filename + ".mp3"));
		System.out.println(info.getTitle() + " -> " + song.getAbsolutePath());

		return song;
	}

	/**
	 * Retrieves the file for a given song by looking for it in the iTunes
	 * Auto-add folder
	 * 
	 * @param info
	 *            Song to look for
	 * @return File of the mp3 file in the music folder
	 */
	public static File getTempFile(SongInfo info) {
		File song = new File(ADD_FOLDER, Analytics.validateFileName(info
				.getTitle() + " by " + info.getArtist())
				+ ".mp3");
		return song;
	}

	/**
	 * Checks if a file is in queue to be added (auto-add folder) or has already
	 * been added (iTunes library)
	 * 
	 * @param info
	 *            Song to look for
	 * @return Whether or not the file can be found in one of the locations
	 */
	public static boolean iTunesExists(SongInfo info) {
		return getFile(info).exists() || getTempFile(info).exists();
	}

	/**
	 * Adds a file to iTunes by ensuring the auto-add folder exists and putting in there (deletes any file there with its name)
	 * @param info Song to add which must contain a file location to copy from
	 * @throws IOException If the copy fails, this exception is thrown
	 */
	public static void addToiTunes(SongInfo info) throws IOException {
		File file = getTempFile(info);
		file.getParentFile().mkdirs();
		if (file.exists())
			file.delete();
		Analytics.copyFile(info.getPublic(), file);
	}

	/**
	 * Removes a file from iTunes by deleting anything in the auto-add folder or the library
	 * @param info Song to delete
	 */
	public static void removeFromiTunes(SongInfo info) {
		File file = getFile(info);
		if (file.exists())
			file.delete();
		file = getTempFile(info);
		if (file.exists())
			file.delete();
	}
}
