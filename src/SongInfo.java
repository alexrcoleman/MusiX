import java.io.File;

import youtube.YoutubeVideo;

public class SongInfo {
	private byte[] bytes;
	private String title;
	private String artist;
	private String album;
	private byte[] albumArtwork;
	private String lyrics;
	private YoutubeVideo youtubeVideo;
	private File cache;
	private File publicFile;
	private String link;
	private ItemPanel itemPanel;
	private double rating;
	private int trackNumber;
	private int discNumber;
	private int year;
	private int discMax;
	private int trackMax;
	private String spotifyID;

	public SongInfo(byte[] bytes, String title, String artist, String album,
			byte[] albumArtwork, String lyrics) {
		this.bytes = bytes;
		this.title = title;
		this.artist = artist;
		this.album = album;
		this.albumArtwork = albumArtwork;
		this.lyrics = lyrics;
	}

	public SongInfo() {

	}

	public String getAlbum() {
		return album;
	}

	public byte[] getAlbumArtwork() {
		return albumArtwork;
	}

	public String getArtist() {
		return artist;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public String getLyrics() {
		return lyrics;
	}

	public String getTitle() {
		return title;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public void setAlbumArtwork(byte[] albumArtwork) {
		this.albumArtwork = albumArtwork;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public void setLyrics(String lyrics) {
		this.lyrics = lyrics;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setYoutubeVideo(YoutubeVideo youtubeVideo) {
		this.youtubeVideo = youtubeVideo;
	}

	public YoutubeVideo getYoutubeVideo() {
		return youtubeVideo;
	}

	public File getCache() {
		return cache;
	}

	public void setCache(File cache) {
		this.cache = cache;
	}

	public File getPublic() {
		return publicFile;
	}

	public void setPublic(File publicFile) {
		this.publicFile = publicFile;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getLink() {
		return link;
	}

	public ItemPanel getItemPanel() {
		return itemPanel;
	}

	public void setItemPanel(ItemPanel itemPanel) {
		this.itemPanel = itemPanel;
	}

	public int getDiscNumber() {
		return discNumber;
	}

	public double getRating() {
		return rating;
	}

	public int getTrackNumber() {
		return trackNumber;
	}

	public void setDiscNumber(int discNumber) {
		this.discNumber = discNumber;
	}

	public void setTrackNumber(int trackNumber) {
		this.trackNumber = trackNumber;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getDiscMax() {
		return discMax;
	}

	public int getTrackMax() {
		return trackMax;
	}

	public void setDiscMax(int discMax) {
		this.discMax = discMax;
	}

	public void setTrackMax(int trackMax) {
		this.trackMax = trackMax;
	}

	public String getSpotifyID() {
		return spotifyID;
	}

	public void setSpotifyID(String spotifyID) {
		this.spotifyID = spotifyID;
	}

	@Override
	public String toString() {
		return "[title=" + title + ",art=" + artist + ",alb=" + album + "]";
	}
}
