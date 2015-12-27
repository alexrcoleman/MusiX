import javax.imageio.ImageIO;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.awt.Font;

import javax.swing.ScrollPaneConstants;

public class ResultPanel extends JPanel {
	private static final long serialVersionUID = 7328795968051318676L;
	public JLabel songLabel;
	public JTextField songField;
	public JLabel artistLabel;
	public JTextField artistField;
	public JLabel albumLabel;
	public JTextPane lyricsEditor;
	public ImagePanel albumPanel;
	public ImagePanel closePanel;
	public JScrollPane scrollPane;
	private JButton backButton;
	private SongInfo info;
	private ItemPanel itemPanel;

	public static BufferedImage iTunesPlusImg;
	public static BufferedImage iTunesMinusImg;
	public static BufferedImage clipboardImg;
	public static BufferedImage mp3Img;
	public static BufferedImage youtubeImg;
	{
		try {
			iTunesPlusImg = ImageIO.read(ResultPanel.class.getResourceAsStream("/resources/iTunes_Plus.png"));
			iTunesMinusImg = ImageIO.read(ResultPanel.class.getResourceAsStream("/resources/iTunes_Minus.png"));
			clipboardImg = ImageIO.read(ResultPanel.class.getResourceAsStream("resources/clipboard.png"));
			mp3Img = ImageIO.read(ResultPanel.class.getResourceAsStream("resources/Music Folder.png"));
			youtubeImg = ImageIO.read(ResultPanel.class.getResourceAsStream("resources/YoutubeLogo.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private boolean escapeLast = false;

	private void finishTitle(boolean success) {
		if (success) {
			if (escapeLast) {
				escapeLast = false;
				return;
			}
			songLabel.setText(songField.getText());

			MP3File audio;
			Tag tag;
			try {
				if (info.getCache() != null) {
					audio = new MP3File(info.getCache());
					tag = audio.getTagOrCreateAndSetDefault();
					tag.setField(FieldKey.TITLE, songField.getText());
					audio.commit();
				}
				audio = new MP3File(info.getPublic());
				tag = audio.getTagOrCreateAndSetDefault();
				tag.setField(FieldKey.TITLE, songField.getText());
				audio.commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
			info.setTitle(songField.getText());
			itemPanel.completeInfo(info);
			handleTransfer();
		} else {
			songField.setText(songLabel.getText());
			escapeLast = true;
		}
		titleLayout.show(titleHolder, "label");
	}

	private void finishArtist(boolean success) {
		if (success) {
			if (escapeLast) {
				escapeLast = false;
				return;
			}
			artistLabel.setText(artistField.getText());

			MP3File audio;
			Tag tag;
			try {
				if (info.getCache() != null && !info.getCache().exists())
					return;
				if (info.getCache() != null) {
					audio = new MP3File(info.getCache());
					tag = audio.getTagOrCreateAndSetDefault();
					tag.setField(FieldKey.ARTIST, artistField.getText());
					audio.commit();
				}
				audio = new MP3File(info.getPublic());
				tag = audio.getTagOrCreateAndSetDefault();
				tag.setField(FieldKey.ARTIST, artistField.getText());
				audio.commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
			info.setArtist(artistField.getText());
			itemPanel.completeInfo(info);
			handleTransfer();
		} else {
			artistField.setText(artistLabel.getText());
			escapeLast = true;
		}
		artistLayout.show(artistHolder, "label");
	}

	private void handleTransfer() {
		info.getPublic().delete();
		File newFile = new File(new File(Analytics.getPublicFolder(), info.getArtist()), info.getTitle() + ".mp3");
		info.setPublic(newFile);
		newFile.getParentFile().mkdirs();
		if (newFile.exists()) // TODO: Maybe make this optional
			newFile.delete();
		try {
			Analytics.copyFile(info.getCache(), newFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private LyricsChangeListener listener;

	private JPanel titleHolder;
	private CardLayout titleLayout;
	private JPanel artistHolder;
	private CardLayout artistLayout;
	/**
	 * Create the panel.
	 */
	public ResultPanel(final ItemPanel itemPanel, final SongInfo info) {
		this.setLayout(new BorderLayout());
		this.itemPanel = itemPanel;
		this.info = info;
		this.setFocusable(true);
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ResultPanel.this.requestFocus();
			}
		});
		
		
		closePanel = new ImagePanel(ItemPanel.class.getResourceAsStream("resources/close.png"));

		closePanel.setPreferredSize(new Dimension(22,22));
		closePanel.makeButton();
		closePanel.setToolTipText("Delete");
		closePanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				info.getPublic().delete();
				Analytics.database.remove(info.getArtist());

				boolean dec = false;
				for(ItemPanel ip : DownloadGUI.panelList)  {
					if(ip == itemPanel)
						dec = true;
					if(dec)
						ip.id--;
					ip.updateColor();
				}
				itemPanel.getParent().remove(itemPanel);
				DownloadGUI.panelList.remove(itemPanel);
				DownloadGUI.instance.changePanel(DownloadGUI.instance.searchPanel);
				DownloadGUI.instance.repaint();
			}
		});
		closePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		
		// add(closePanel);

		songLabel = new JLabel(info.getTitle());
		songLabel.setFont(new Font("Helvetica", Font.BOLD, 20));
		songLabel.setBounds(10, 40, 391, 16);
		
		songField = new JTextField(info.getTitle());
		songField.setFont(new Font("Helvetica", Font.PLAIN, 13));
		songField.setLocation(5, 32);
		songField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				event(e);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				event(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				event(e);
			}

			private void event(DocumentEvent e) {
				//songField.setSize(songField.getPreferredSize());
			}
		});
		songField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 27) {
					finishTitle(false);
				}
			}
		});
		songField.setSize(songField.getPreferredSize());
		songLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 1) {
					songField.setText(songLabel.getText());
					songField.selectAll();
					titleLayout.show(titleHolder, "field");
					songField.requestFocusInWindow();
				}
			}
		});
		songField.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				finishTitle(true);
			}

			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});
		songField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				songField.setVisible(false);
			}
		});
		// add(songLabel);
		// add(songField);
		
		artistLabel = new JLabel(info.getArtist());
		artistLabel.setBounds(58, 60, 265, 16);
		artistLabel.setFont(new Font("Helvetica", Font.PLAIN, 15));
		artistField = new JTextField(info.getArtist());
		artistField.setFont(new Font("Helvetica", Font.PLAIN, 13));
		artistField.setLocation(53, 55);
		artistField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				event(e);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				event(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				event(e);
			}

			private void event(DocumentEvent e) {
				//artistField.setSize(artistField.getPreferredSize());
			}
		});
		artistField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 27) {
					finishArtist(false);
				}
			}
		});
		artistField.setSize(artistField.getPreferredSize());
		artistLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 1) {
					artistField.setText(artistLabel.getText());
					artistField.selectAll();
					artistLayout.show(artistHolder, "field");
					artistField.requestFocusInWindow();
				}
			}
		});
		artistField.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				finishArtist(true);
			}

			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});
		artistField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				artistField.setVisible(false);
			}
		});
		// add(artistField);
		// add(artistLabel);

		// add(scrollPane);

		lyricsEditor = new JTextPane();
		//lyricsEditor.setText("THIS IS A TEST.");
		lyricsEditor.setText(info.getLyrics());
		lyricsEditor.setFont(new Font("Courier", Font.PLAIN, 12));
		listener = new LyricsChangeListener();
		lyricsEditor.getDocument().addDocumentListener(listener);
		lyricsEditor.setCaretPosition(0);
		listener.start();
		scrollPane = new JScrollPane(lyricsEditor);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//scrollPane.setBounds(10, 88, 285, 206);
		scrollPane.getVerticalScrollBar().setUnitIncrement(5);

		albumPanel = new ImagePanel();
		albumPanel.setAsync(false);
		albumPanel.setImage(info.getAlbumArtwork());
		albumPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 3, 3));
		albumPanel.setPreferredSize(new Dimension(157, 157));
		// add(albumPanel);

		albumLabel = new JLabel(info.getAlbum());
		albumLabel.setHorizontalAlignment(SwingConstants.CENTER);
		albumLabel.setPreferredSize(new Dimension(150,28));
		// add(albumLabel);
		
		backButton = new JButton("Back");
		backButton.setBounds(0, 0, 67, 29);
		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DownloadGUI.instance.changePanel(DownloadGUI.instance.searchPanel);
			}
		});
		// add(backButton);

		/*
		 * try { final BufferedImage playImage = ImageIO.read(ResultPanel.class
		 * .getResourceAsStream("resources/play-button.png")); final BufferedImage pauseImage =
		 * ImageIO.read(ResultPanel.class .getResourceAsStream("resources/pause-button.png")); final BufferedImage stopImage
		 * = ImageIO.read(ResultPanel.class .getResourceAsStream("resources/stop-button.png")); final ImagePanel playButton =
		 * new ImagePanel(playImage); playButton.setBounds(392, 6, 32, 32); playButton.addMouseListener(new MouseAdapter() {
		 * int mode = 0; Clip clip;
		 * 
		 * @Override public void mouseClicked(MouseEvent e) { if (mode == 0) try { System.out.println(ResultPanel.this.info
		 * .getPublic()); AudioInputStream audioIn = AudioSystem .getAudioInputStream(new BufferedInputStream( new
		 * FileInputStream( ResultPanel.this.info .getPublic()))); Clip clip = AudioSystem.getClip(); clip.open(audioIn);
		 * clip.start(); mode = 1; playButton.setImage(pauseImage);
		 * 
		 * } catch (IOException e1) { e1.printStackTrace(); } catch (LineUnavailableException e1) { e1.printStackTrace(); }
		 * catch (UnsupportedAudioFileException e1) { e1.printStackTrace(); } else if (mode == 1) { clip.stop(); mode = 2;
		 * playButton.setImage(playImage); } else if (mode == 2) { clip.start(); mode = 1; playButton.setImage(pauseImage); }
		 * } }); // add(playButton); } catch (IOException e2) { e2.printStackTrace(); }
		 */

		ImagePanel youtubeImage = new ImagePanel(youtubeImg);
		youtubeImage.setPreferredSize(new Dimension(47,50));
		youtubeImage.setToolTipText("Open YouTube link");
		youtubeImage.makeButton();
		youtubeImage.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=" + ResultPanel.this.info.getYoutubeVideo().getVideoId()));
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		});
		youtubeImage.setCursor(new Cursor(Cursor.HAND_CURSOR));
		

		ImagePanel mp3Image = new ImagePanel(mp3Img);
		mp3Image.setPreferredSize(new Dimension(45,45));
		mp3Image.makeButton();
		mp3Image.setToolTipText("Open in file browser");
		mp3Image.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().open(ResultPanel.this.info.getPublic().getParentFile());
					if (Analytics.isWindows()) {
						Runtime rt = Runtime.getRuntime();
						rt.exec("explorer /select," + ResultPanel.this.info.getPublic());
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		mp3Image.setCursor(new Cursor(Cursor.HAND_CURSOR));
		// add(mp3Image);

		/*ImagePanel clipboardImage = new ImagePanel(clipboardImg);
		clipboardImage.setBounds(375, 272, 22, 22);
		clipboardImage.setToolTipText("Copy file to clipboard");
		clipboardImage.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				File file = ResultPanel.this.info.getPublic();
				ResultFileTransferable transfer = new ResultFileTransferable();
				transfer.addFile(file);
				transfer.setClipboard();
			}
		});
		clipboardImage.setCursor(new Cursor(Cursor.HAND_CURSOR));*/
		// add(clipboardImage);

		final ImagePanel iTunesImagePanel = new ImagePanel();
		iTunesImagePanel.makeButton();
		iTunesImagePanel.setPreferredSize(new Dimension(45,45));
		if (iTunes.iTunesExists(info)) {
			iTunesImagePanel.setToolTipText("Remove from iTunes");
			iTunesImagePanel.setImage(iTunesMinusImg);
		} else {
			iTunesImagePanel.setToolTipText("Add to iTunes");
			iTunesImagePanel.setImage(iTunesPlusImg);
		}
		iTunesImagePanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (iTunes.iTunesExists(info)) {
					iTunes.removeFromiTunes(info);
					iTunesImagePanel.setToolTipText("Add to iTunes");
					iTunesImagePanel.setImage(iTunesPlusImg);
					iTunesImagePanel.repaint();
				} else {
					try {
						iTunes.addToiTunes(info);
						iTunesImagePanel.setToolTipText("Remove from iTunes");
						iTunesImagePanel.setImage(iTunesMinusImg);
						iTunesImagePanel.repaint();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		iTunesImagePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		// add(iTunesImagePanel);

		refreshButton = new JButton("Scrape Metadata");
		refreshButton.setPreferredSize(new Dimension(87, 29));
		
		refreshButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				MP3File audio;
				Tag tag;

				String lyrics = Lyrics.search(info.getTitle(), info.getArtist());
				if(lyrics == null)
					lyrics = "";
				System.out.println("Got lyrics: " + lyrics.length());
				try {
					info.setSpotifyID(null);
					if (info.getCache() != null && info.getCache().exists()) {
						
						audio = new MP3File(info.getCache());
						tag = audio.getTagOrCreateAndSetDefault();
						Analytics.addSpotifyData(info.getTitle(), info.getArtist(), info, tag);
						System.out.println("Cache found, album is " + info.getAlbum());
						if(lyrics.isEmpty())
							tag.deleteField(FieldKey.LYRICS);
						else
							tag.setField(FieldKey.LYRICS, lyrics);
						audio.commit();
					}
					
					audio = new MP3File(info.getPublic());
					tag = audio.getTagOrCreateAndSetDefault();
					Analytics.addSpotifyData(info.getTitle(), info.getArtist(), info, tag);
					System.out.println("NO CACHE found, album is " + info.getAlbum());
					if(lyrics.isEmpty())
						tag.deleteField(FieldKey.LYRICS);
					else
						tag.setField(FieldKey.LYRICS, lyrics);
					audio.commit();
				} catch (Exception e) {
					e.printStackTrace();
				}
				info.setLyrics(lyrics);
				albumPanel.setImage(info.getAlbumArtwork());
				albumLabel.setText(info.getAlbum());
				lyricsEditor.setText(info.getLyrics());
				lyricsEditor.setCaretPosition(0);
				itemPanel.completeInfo(info);
			}
		});
		// add(refreshButton);
		
		JPanel northPanel = new JPanel(new BorderLayout());
		
		JPanel northnorthPanel = new JPanel(new BorderLayout());
		northnorthPanel.add(closePanel, BorderLayout.EAST);
		northnorthPanel.add(backButton, BorderLayout.WEST);
		
		northPanel.add(northnorthPanel, BorderLayout.NORTH);
		// northPanel.setBackground(new Color(0,0,255));
		
		JPanel eastPanel = new JPanel(new BorderLayout());
		// eastPanel.setBackground(new Color(0,255,0));
		
		JPanel fullButtons = new JPanel();
		fullButtons.setLayout(new BoxLayout(fullButtons, BoxLayout.Y_AXIS));
		JPanel buttons = new JPanel(new GridLayout(1,3));
		buttons.add(iTunesImagePanel);
		buttons.add(mp3Image);
		if (ResultPanel.this.info.getYoutubeVideo() != null)
			buttons.add(youtubeImage);
		fullButtons.add(refreshButton);
		fullButtons.add(buttons);
		eastPanel.add(fullButtons, BorderLayout.SOUTH);
		JPanel albumHolder = new JPanel();
		albumHolder.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		JPanel bufferAlbumArtwork = new JPanel(new BorderLayout());
		bufferAlbumArtwork.add(albumPanel, BorderLayout.SOUTH);
		bufferAlbumArtwork.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		
		albumHolder.add(bufferAlbumArtwork, c);
		//albumHolder.add(Box.createVerticalGlue());
		JPanel albumLabelHolder = new JPanel(new BorderLayout());
		albumLabelHolder.add(albumLabel, BorderLayout.NORTH);
		// albumLabelHolder.setBackground(new Color(255,0,0));
		c.gridx = 0;
		c.gridy = 1;
		albumHolder.add(albumLabelHolder, c);
		eastPanel.add(albumHolder, BorderLayout.CENTER);
		
		JPanel centerPanel = new JPanel(new BorderLayout());

		JPanel bufferScrollPane = new JPanel(new GridLayout(1,1));
		bufferScrollPane.add(scrollPane);
		bufferScrollPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		
		centerPanel.add(bufferScrollPane, BorderLayout.CENTER);
		
		// centerPanel.setBackground(new Color(255,0,0));

		JPanel heading = new JPanel(new GridLayout(2,1));
		heading.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		
		titleHolder = new JPanel();
		titleHolder.setLayout(titleLayout = new CardLayout());
		titleHolder.add(songLabel, "label");
		titleHolder.add(songField, "field");
		artistHolder = new JPanel();
		artistHolder.setLayout(artistLayout = new CardLayout());
		artistHolder.add(artistLabel, "label");
		artistHolder.add(artistField, "field");
		
		heading.add(titleHolder);
		heading.add(artistHolder);
		
		northPanel.add(heading, BorderLayout.CENTER);
		
		this.add(northPanel, BorderLayout.NORTH);
		this.add(centerPanel, BorderLayout.CENTER);
		this.add(eastPanel, BorderLayout.EAST);
		
		// initializeAlbumArtwork();
		
		// albumPanel.setImage(bi);
	}
	public void setTrueBG(Container container, Color c) {
		for(Component comp : container.getComponents()) {
			if(comp instanceof JTextField || comp instanceof JTextPane)
				continue;
			comp.setBackground(c);
			if(comp instanceof Container) {
				setTrueBG((Container) comp, c);
			}
		}
	}
	public void setTrueBG(Color c) {
		setTrueBG(this, c);
	}
	protected void initializeAlbumArtwork() {
		BufferedImage bi = albumPanel.getImage();
		if (bi != null) {
			long r = 0, g = 0, b = 0, c = 0;
			for (int x = 0; x < bi.getWidth(); x++) {
				for (int y = 0; y < bi.getHeight(); y++) {
					if (x == 0 || y == 0 || x == bi.getWidth() || y == bi.getHeight()) {
						Color color = new Color(bi.getRGB(x, y));
						r += color.getRed();
						g += color.getGreen();
						b += color.getBlue();
						c++;
					}
				}
			}
			r /= c;
			g /= c;
			b /= c;
			Color border = new Color((int) r, (int) g, (int) b);
			HashMap<Color, Integer> colorMap = new HashMap<Color, Integer>();
			for (int x = 0; x < bi.getWidth(); x += 3) {
				for (int y = 0; y < bi.getHeight(); y += 3) {
					Color color = new Color(bi.getRGB(x, y));
					Color match = color;
					for (Entry<Color, Integer> entry : colorMap.entrySet()) {
						if (ResultPanel.getColorDistance(color, entry.getKey()) <= 15000 || ResultPanel.getColorDistance(color.brighter(), entry.getKey()) <= 15000) {
							match = entry.getKey();
							break;
						}
					}
					colorMap.put(match, (colorMap.containsKey(match) ? colorMap.get(match) : 0) + 1);
				}
			}
			List<Entry<Color, Integer>> list = new LinkedList<Entry<Color, Integer>>(colorMap.entrySet());
			Collections.sort(list, new Comparator<Entry<Color, Integer>>() {
				public int compare(Entry<Color, Integer> m1, Entry<Color, Integer> m2) {
					return (m2.getValue()).compareTo(m1.getValue());
				}
			});
			// System.out.println(list);
			if (list.size() < 3) {
				System.out.println("Forced into using border (lack of color scheme)");
				System.out.println(list);
				Color opposing = ResultPanel.getOpposingColor(border);
				songLabel.setForeground(opposing);
				artistLabel.setForeground(opposing);
				albumLabel.setForeground(opposing);
				this.setBackground(border);
				for(Component comp : this.getComponents())
					comp.setBackground(border);
			} else {
				if (ResultPanel.getColorDistance(border, list.get(1).getKey()) <= 5000) {
					System.out.println("Using avg");
					this.setBackground(list.get(0).getKey());
					for(Component comp : this.getComponents())
						comp.setBackground(list.get(0).getKey());
				} else {
					System.out.println("Using border");
					this.setBackground(border/* list.get(0).getKey() */);
					setTrueBG(border);
				}
				songLabel.setForeground(list.get(1).getKey());
				albumLabel.setForeground(list.get(1).getKey());

				if (ResultPanel.getColorDistance(this.getBackground(), list.get(2).getKey()) > 5000) {
					System.out.println("Using avg");
					artistLabel.setForeground(list.get(2).getKey());
				} else {
					System.out.println("Using complement");
					artistLabel.setForeground(ResultPanel.getOpposingColor(this.getBackground()));
				}
			}
			for (int x = 0; x < bi.getWidth(); x++) {
				for (int y = 0; y < bi.getHeight(); y++) {
					Color color = new Color(bi.getRGB(x, y));
					int borderX = 0;
					if (x > bi.getWidth() / 2.0)
						borderX = bi.getWidth();
					int borderY = 0;
					if (y > bi.getHeight() / 2.0)
						borderY = bi.getHeight();
					int alpha = 175;
					if (Math.abs(borderX - x) <= 25 && Math.abs(borderX - x) < Math.abs(borderY - y))
						alpha = (int) (Math.abs(borderX - x) * 175 / 25.0);
					else if (Math.abs(borderY - y) <= 25)
						alpha = (int) (Math.abs(borderY - y) * 175 / 25.0);
					alpha = Math.max(0, Math.min(255, alpha));
					bi.setRGB(x, y, new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB());
				}
			}
		}
	}

	public static Color getContrastColor(Color color) {
		return getContrastColor(color.getRed(), color.getGreen(), color.getBlue());
	}

	public static Color getContrastColor(int r, int g, int b) {
		int yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;
		return (yiq >= 128) ? Color.BLACK : Color.WHITE;
	}

	public static Color getInverseColor(int r, int g, int b) {
		return new Color(255 - r, 255 - g, 255 - b);
	}

	public static Color getOpposingColor(Color color) {
		return getOpposingColor(color.getRed(), color.getGreen(), color.getBlue());
	}

	public static int getColorDistance(Color a, Color b) {
		return (int) (Math.pow((a.getRed() - b.getRed()), 2) + Math.pow((a.getGreen() - b.getGreen()), 2) + Math.pow((a.getBlue() - b.getBlue()), 2));
	}

	private static int C_THRESHOLD = 30;
	private JButton refreshButton;

	public static Color getOpposingColor(int r, int g, int b) {
		if (Math.abs(r - 127.5) < C_THRESHOLD && Math.abs(g - 127.5) < C_THRESHOLD && Math.abs(b - 127.5) < C_THRESHOLD)
			return getContrastColor(r, g, b);
		return getInverseColor(r, g, b);
	}

	public static Color getMixedColor(Color a, Color b) {
		return new Color((a.getRed() + b.getRed()) / 2, (a.getGreen() + b.getGreen()) / 2, (a.getBlue() + b.getBlue()) / 2);
	}

	public void onForeground() {
		listener.foreground = true;
	}

	public void onResume() {
		if (listener != null) {
			listener.foreground = false;
			if (!listener.isAlive())
				listener.start();
		}
	}

	class LyricsChangeListener extends Thread implements DocumentListener {
		@Override
		public void changedUpdate(DocumentEvent e) {
			onChange(e);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			onChange(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			onChange(e);
		}

		long lastChange = 0;

		private void onChange(DocumentEvent e) {
			lastChange = System.currentTimeMillis();
		}

		boolean foreground = false;

		@Override
		public void run() {
			while (!foreground) {
				if (lastChange != 0 && System.currentTimeMillis() > lastChange + 3000) {
					lastChange = 0;
					System.out.println("Updating lyrics");
					info.setLyrics(lyricsEditor.getText());

					MP3File audio;
					Tag tag;
					try {
						if (info.getCache() != null && info.getCache().exists()) {
							audio = new MP3File(info.getCache());
							tag = audio.getTagOrCreateAndSetDefault();
							tag.setField(FieldKey.LYRICS, lyricsEditor.getText());
							audio.commit();
						}

						audio = new MP3File(info.getPublic());
						tag = audio.getTagOrCreateAndSetDefault();
						tag.setField(FieldKey.LYRICS, lyricsEditor.getText());
						audio.commit();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	class ResultFileTransferable implements Transferable {
		private List<File> files = new ArrayList<File>();

		public ResultFileTransferable() {

		}

		public ResultFileTransferable(File file) {
			addFile(file);
		}

		public ResultFileTransferable(List<File> files) {
			addFiles(files);
		}

		public void addFile(File file) {
			this.files.add(file);
		}

		public void addFiles(List<File> files) {
			this.files.addAll(files);
		}

		public List<File> getFiles() {
			return files;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor };
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.javaFileListFlavor.equals(flavor) || DataFlavor.stringFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			System.out.println("Transfer flavor: " + flavor.getHumanPresentableName());
			if (flavor.isFlavorJavaFileListType())
				return files;
			StringBuilder sb = new StringBuilder();
			long i = 0;
			for (File f : files)
				sb.append(i++ > 0 ? "\n" : "").append(f.getName());
			return sb.toString();
		}

		public void setClipboard() {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(this, null);
		}
	}
}
