import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.coleman.utilities.http.Client;
import com.coleman.utilities.http.DownloadProgressHandler;
import com.echonest.api.v4.EchoNestAPI;

import youtube.Youtube;
import youtube.YoutubeDownloader;
import youtube.YoutubeMP3;
import youtube.YoutubeVideo;

public class DownloadGUI extends JFrame {
	public static final Color COLOR_BORDER = new Color(200, 200, 200);
	private static final long serialVersionUID = -605159229467520421L;
	public static YoutubeDownloader downloader = new YoutubeMP3();
	public static EchoNestAPI en = new EchoNestAPI("6P55ZC4OQHQI5HHNZ");
	public static DownloadGUI instance;

	public JPanel contentPane;
	public SearchPanel searchPanel;
	public JPanel currentPanel;
	public JLabel statusLabel;
	static List<ItemPanel> panelList = new ArrayList<ItemPanel>();
	static {
		/*
		 * try { UIManager.installLookAndFeel("SeaGlass", "com.seaglasslookandfeel.SeaGlassLookAndFeel");
		 * UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel"); } catch (Exception e) {
		 * System.err.println("Seaglass LAF not available using Ocean.");
		 */
		try {
			// UIManager.setLookAndFeel(new NimbusLookAndFeel());
		} catch (Exception e2) {
			System.err.println("Unable to use Ocean LAF using default.");
		}
		// }
	}

	String getDayOfMonthSuffix(final int n) {
		if (n >= 11 && n <= 13) {
			return "th";
		}
		switch (n % 10) {
		case 1:
			return "st";
		case 2:
			return "nd";
		case 3:
			return "rd";
		default:
			return "th";
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		Logger.getLogger("org.jaudiotagger.tag.datatype").setLevel(Level.SEVERE);
		instance = new DownloadGUI();
		instance.setVisible(true);
		instance.setStatus("Search for a song");
		ActionListener searchAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String keywords = instance.searchPanel.searchTextField.getText();
				if (!keywords.trim().isEmpty()) {
					LoadThread thread = new LoadThread(instance, keywords);
					thread.start();
				}
			}
		};
		instance.searchPanel.searchButton.addActionListener(searchAction);
		instance.searchPanel.searchTextField.addActionListener(searchAction);
		final ArrayList<File> files = new ArrayList<File>();
		for (File artistFolder : Analytics.getPublicFolder().listFiles()) {
			if (artistFolder.isDirectory())
				for (File songFile : artistFolder.listFiles()) {
					if (songFile.getName().endsWith(".mp3"))
						files.add(songFile);
				}
		}
		Collections.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return -Long.compare(o1.lastModified(), o2.lastModified());
			}
		});
		int i = 0;
		Calendar currentDay = null;
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, YYYY");
		sdf.setTimeZone(Calendar.getInstance().getTimeZone());
		for (File songFile : files) {
			if (currentDay == null) {
				currentDay = Calendar.getInstance();
				currentDay.setTime(new Date(songFile.lastModified()));
				currentDay.set(Calendar.HOUR_OF_DAY, 0);
				currentDay.set(Calendar.MINUTE, 0);
				currentDay.set(Calendar.SECOND, 0);
				currentDay.set(Calendar.MILLISECOND, 0);
			}
			final SongInfo info = new SongInfo();
			info.setTitle(songFile.getName().replace(".mp3", ""));
			info.setArtist(songFile.getParentFile().getName());
			final ItemPanel itemPanel = new ItemPanel(info.getTitle() + " by " + info.getArtist(), null);
			itemPanel.setInfo(info);
			instance.searchPanel.panelList.add(itemPanel);
			if (songFile.lastModified() < currentDay.getTimeInMillis()) {// + (1000*60*60*24)) {
				currentDay.setTime(new Date(songFile.lastModified()));
				currentDay.set(Calendar.HOUR_OF_DAY, 0);
				currentDay.set(Calendar.MINUTE, 0);
				currentDay.set(Calendar.SECOND, 0);
				currentDay.set(Calendar.MILLISECOND, 0);

				String formatted = sdf.format(currentDay.getTime());
				JPanel tmp = new JPanel(new GridLayout(1, 1));
				JLabel label = new JLabel(formatted);
				// tmp.setBackground(new Color(200,200,200));

				Border border = BorderFactory.createMatteBorder(0, 0, 1, 0, DownloadGUI.COLOR_BORDER);
				border = BorderFactory.createCompoundBorder(border, new EmptyBorder(5, 5, 5, 0));

				tmp.setBorder(border);
				label.setFont(new Font("Helvetica", Font.BOLD, 20));
				label.setForeground(new Color(50, 50, 50));
				tmp.add(label);
				instance.searchPanel.panelList.add(tmp);

			}
			itemPanel.id = i + 1;// panels.length - i;
			itemPanel.updateColor();
			DownloadGUI.panelList.add(0, itemPanel);
			final int j = i;
			Thread processThread = new Thread() {
				public void run() {
					// for (int i = fileArray.length - 1; i >= 0; i--) {
					File songFile = files.get(j);
					SongInfo info = new SongInfo();
					MP3File audio;
					try {
						audio = new MP3File(songFile);
					} catch (Exception e1) {
						songFile.delete();
						e1.printStackTrace();
						return;
					}
					Tag tag = audio.getTagOrCreateAndSetDefault();

					info.setAlbum(tag.getFirst(FieldKey.ALBUM));
					if (tag.getArtworkList().size() > 0)
						info.setAlbumArtwork(tag.getArtworkList().get(tag.getArtworkList().size() - 1).getBinaryData());
					info.setArtist(tag.getFirst(FieldKey.ARTIST));
					info.setLyrics(tag.getFirst(FieldKey.LYRICS));
					String custom2 = tag.getFirst(FieldKey.CUSTOM2);
					info.setPublic(songFile);
					info.setTitle(tag.getFirst(FieldKey.TITLE));

					String track = tag.getFirst(FieldKey.TRACK);
					String disc = tag.getFirst(FieldKey.DISC_NO);
					String rating = tag.getFirst(FieldKey.RATING);
					String year = tag.getFirst(FieldKey.YEAR);
					if (custom2 != null && !custom2.isEmpty())
						info.setYoutubeVideo(new YoutubeVideo(custom2, info.getArtist(), info.getTitle()));
					if (info.getYoutubeVideo() != null)
						info.setCache(new File(Analytics.getPrivateFolder(), "cache/" + info.getYoutubeVideo().getVideoId() + ".mp3"));
					if (track != null && !track.isEmpty())
						info.setTrackNumber(Integer.parseInt(track));
					if (disc != null && !disc.isEmpty())
						info.setDiscNumber(Integer.parseInt(disc));
					if (rating != null && !rating.isEmpty())
						info.setRating(Integer.parseInt(rating) / 255.0);
					if (year != null && !year.isEmpty()) {
						try {
							info.setYear(Integer.parseInt(year));
						} catch (NumberFormatException e) {

						}
					}
					itemPanel.completeInfo(info);

				}
			};
			processThread.start();
			i++;

		}

	}

	/**
	 * Create the frame.
	 */
	public DownloadGUI() {
		setTitle("MusiX");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 550, 347);
		setMinimumSize(new Dimension(300, 400));
		contentPane = new JPanel();
		// contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		searchPanel = new SearchPanel();
		currentPanel = searchPanel;
		contentPane.add(searchPanel, BorderLayout.CENTER);

		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		statusLabel = new JLabel();
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(statusLabel);
		statusPanel.setLocation(0, 306);
		statusPanel.setSize(new Dimension(462, 19));
		contentPane.add(statusPanel, BorderLayout.SOUTH);
	}

	public void setStatus(String text) {
		statusLabel.setText(" " + text);
	}

	public void changePanel(JPanel newPanel) {
		if (currentPanel instanceof ResultPanel) {
			ResultPanel rp = (ResultPanel) currentPanel;
			rp.onForeground();
		}
		contentPane.remove(currentPanel);
		contentPane.add(newPanel, BorderLayout.CENTER);
		currentPanel = newPanel;
		if (newPanel instanceof ResultPanel) {
			ResultPanel rp = (ResultPanel) newPanel;
			rp.onResume();
		}
		invalidate();
		validate();
		repaint();
	}
}

class LoadThread extends Thread {
	private SearchPanel searchPanel;
	private DownloadGUI gui;
	private String keywords;
	private DownloadProgressHandler dph;
	private boolean kill = false;

	public void kill() {
		this.kill = true;
		dph.kill();
	}

	public LoadThread(DownloadGUI gui, String keywords) {
		this.searchPanel = gui.searchPanel;
		this.gui = gui;
		this.keywords = keywords;
	}

	private Client client = new Client();

	private SongInfo addTrackByKeywords(String keywords) {
		return addTrackByKeywords(keywords, null);
	}

	private SongInfo addTrackByKeywords(String keywords, SongInfo extraInfo) {
		YoutubeVideo video = Youtube.searchVideo(keywords);
		final ItemPanel itemPanel = new ItemPanel(video.getVideoTitle(), this);
		itemPanel.id = 1;
		for (ItemPanel ip : DownloadGUI.panelList) {
			ip.id++;
			ip.updateColor();
		}
		DownloadGUI.panelList.add(0, itemPanel);
		searchPanel.panelList.add(itemPanel, 0);
		itemPanel.setProgressIndeterminate(true);
		itemPanel.setProgressMax(1000);
		itemPanel.setProgressText("Retrieving info...");
		gui.setStatus("Downloading...");
		dph = new DownloadProgressHandler() {
			long maxBytes;
			long uploadMaxBytes;

			@Override
			public void sizeDetermined(long maxBytes) {
				this.maxBytes = maxBytes;
				progressUpdate(0);
			}

			@Override
			public void kill() {
				super.kill();
				boolean dec = false;
				for (ItemPanel ip : DownloadGUI.panelList) {
					if (ip == itemPanel)
						dec = true;
					if (dec)
						ip.id--;
					ip.updateColor();
				}
				DownloadGUI.panelList.remove(itemPanel);
				itemPanel.getParent().remove(itemPanel);

			}

			@Override
			public void progressUpdate(long bytesRead) {
				itemPanel.setProgressIndeterminate(false);
				if (maxBytes > 0) {
					itemPanel.setProgress((int) (1000 * bytesRead / maxBytes));
				}
				itemPanel.setProgressText("Downloaded " + (bytesRead / 1000) + "KB / " + (maxBytes / 1000) + "KB");
			}

			@Override
			public void downloadFailed(String reason) {
				itemPanel.setProgressIndeterminate(false);
				itemPanel.setProgress(-1);
				itemPanel.setProgressText("Error: " + reason);
			}

			@Override
			public void downloadComplete(byte[] bytes) {
				itemPanel.setProgressIndeterminate(true);
			}

			@Override
			public void uploadSizeDetermined(long maxBytes) {
				uploadMaxBytes = maxBytes;
				itemPanel.setProgressIndeterminate(false);
			}

			@Override
			public void uploadProgressUpdate(long bytesRead) {
				if (uploadMaxBytes > 0) {
					itemPanel.setProgress((int) (1000 * bytesRead / uploadMaxBytes));
				}
				itemPanel.setProgressText("Uploaded " + (bytesRead / 1000) + "KB / " + (uploadMaxBytes / 1000) + "KB");
			}

			@Override
			public void uploadComplete() {
				itemPanel.setProgressIndeterminate(true);
			}

			@Override
			public void uploadFailed(String reason) {
				itemPanel.setProgressIndeterminate(false);
				itemPanel.setProgress(-1);
				itemPanel.setProgressText("Error: " + reason);
			}

			@Override
			public void updateStatus(String status) {
				itemPanel.setProgressIndeterminate(true);
				itemPanel.setProgressText(status);
			}
		};
		if (kill)
			return null;
		final SongInfo info;
		if (extraInfo == null)
			info = Analytics.downloadAndProcess(dph, keywords);
		else
			info = Analytics.downloadAndProcess(dph, keywords, extraInfo);
		if (info == null)
			return null;
		info.setItemPanel(itemPanel);
		if (kill)
			return null;
		gui.setStatus("Download complete.");
		itemPanel.completeInfo(info);
		return info;
	}

	public boolean doesSongExist(String title, String artist) {
		for (Component c : DownloadGUI.instance.searchPanel.panelList.getComponents()) {
			if (c instanceof ItemPanel) {
				ItemPanel p = (ItemPanel) c;
				SongInfo info = p.getInfo();
				if (info == null)
					continue;
				// System.out.println("*"+info.getTitle() + " by " + info.getArtist());
				if (info.getTitle().equalsIgnoreCase(title) && info.getArtist().equalsIgnoreCase(artist))
					return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		gui.setStatus("Searching for song...");
		try {
			if (keywords.startsWith("spotify:")) {
				String type = keywords.split(":")[1];
				if (type.equals("album")) {
					String callback = new String(client.readSite("http://ws.spotify.com/lookup/1/.json?extras=trackdetail&uri=" + keywords));
					JSONObject json = new JSONObject(new JSONTokener(callback)).getJSONObject("album");
					final String albumName = json.getString("name");
					final String artist = json.getString("artist");
					final int year = Integer.parseInt(json.getString("released"));
					JSONArray tracks = json.getJSONArray("tracks");
					HashMap<Integer, Integer> discToTrackMaxMap = new HashMap<Integer, Integer>();
					int maxDisc = 1;
					for (int i = 0; i < tracks.length(); i++) {
						JSONObject track = tracks.getJSONObject(i);
						if (!track.has("disc-number") || !track.has("track-number"))
							continue;
						final int discNumber = Integer.parseInt(track.getString("disc-number"));
						if (discNumber <= 0)
							continue;
						maxDisc = Math.max(discNumber, maxDisc);
						final int trackNumber = Integer.parseInt(track.getString("track-number"));
						if (trackNumber <= 0)
							continue;
						Integer oldMax = discToTrackMaxMap.get(discNumber);
						if (oldMax == null)
							oldMax = 0;
						discToTrackMaxMap.put(discNumber, Math.max(oldMax, trackNumber));
					}
					for (int i = 0; i < tracks.length(); i++) {
						JSONObject track = tracks.getJSONObject(i);
						final String trackName = track.getString("name");
						if (!trackName.endsWith(" - Acoustic") && !doesSongExist(trackName, artist)) {
							final double popularity = Double.parseDouble(track.getString("popularity"));
							final int discNumber = Integer.parseInt(track.getString("disc-number"));
							final int trackNumber = Integer.parseInt(track.getString("track-number"));
							final int trackMax = discToTrackMaxMap.get(discNumber);
							final int discMax = maxDisc;
							new Thread() {
								public void run() {
									SongInfo extraInfo = new SongInfo();
									extraInfo.setTitle(trackName);
									extraInfo.setArtist(artist);
									extraInfo.setAlbum(albumName);
									extraInfo.setRating(popularity);
									extraInfo.setDiscNumber(discNumber);
									extraInfo.setTrackNumber(trackNumber);
									extraInfo.setDiscMax(discMax);
									extraInfo.setTrackMax(trackMax);
									extraInfo.setYear(year);
									addTrackByKeywords(trackName + " by " + artist + " lyrics", extraInfo);
								}
							}.start();
						}
					}
					return;
				} else if (type.equals("track")) {
				} else {
					gui.setStatus("Unknown spotify protocol '" + type + "'");
				}
				// spotify:album:0G0vIGvVlJ3MtXunjqrbMY
				// spotify:track:1ddcVV6fEuxTDjM6uASCz4

				//
				return;
			}
			addTrackByKeywords(keywords);
		} catch (Exception e) {
			/*
			 * Set<Thread> threadSet = Thread.getAllStackTraces().keySet(); Thread[] threadArray = threadSet.toArray(new
			 * Thread[threadSet.size()]); for (Thread t : threadArray) { System.out.println(t.toString() + ":"); for
			 * (StackTraceElement ste : t.getStackTrace()) { System.out.println("\t" + ste.toString()); } }
			 */
			e.printStackTrace();
		}
	}
}
