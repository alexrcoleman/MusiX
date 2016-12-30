import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ItemPanel extends JPanel implements ComponentListener {
	public static final Color ALBUM_COLOR = new Color(150,150,150);
	public static final Font ALBUM_FONT = new Font("Helvetica", Font.PLAIN, 15);
	public static final Font ARTIST_FONT = new Font("Helvetica", Font.PLAIN, 15);
	public static final Font TITLE_FONT = new Font("Helvetica", Font.BOLD, 20);

	private static final long serialVersionUID = -2104991772995031145L;
	private JProgressBar progressBar;
	private JLabel videoLabel;
	private ImagePanel albumArtworkPanel;
	private JLabel songLabel;
	private JLabel albumLabel;
	private JLabel artistLabel;
	private SongInfo info;
	private ImagePanel closePanel;

	private JPanel completedContent;
	private JPanel loadingContent;
	private List<JPanel> panels;
	private ResultPanel resultPanel;

	public int id;

	/**
	 * Create the panel.
	 */
	public ItemPanel(String videoTitle, final LoadThread loadThread) {
		this.panels = new ArrayList<>();
		this.panels.add(this);
		this.setLayout(new GridLayout(1, 1));
		// this.setBackground(new Color(0,255,0));
		this.addComponentListener(this);

		completedContent = new JPanel(new BorderLayout(20, 0));
		this.panels.add(completedContent);
		loadingContent = new JPanel(new BorderLayout(20, 0));
		this.panels.add(loadingContent);

		completedContent.setPreferredSize(new Dimension(410, 57));
		loadingContent.setPreferredSize(new Dimension(410, 57));

		JPanel loadingContentNorth = new JPanel(new BorderLayout());
		this.panels.add(loadingContentNorth);
		closePanel = new ImagePanel(ItemPanel.class.getResourceAsStream("resources/close.png"));
		this.panels.add(closePanel);
		closePanel.makeButton();

		closePanel.setPreferredSize(new Dimension(15, 15));
		closePanel.setToolTipText("Cancel Download");
		closePanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (loadThread != null) {
					loadThread.kill();
				}
				DownloadGUI.instance.searchPanel.revalidate();
			}
		});
		closePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		loadingContentNorth.add(closePanel, BorderLayout.EAST);
		loadingContent.add(loadingContentNorth, BorderLayout.NORTH);

		albumArtworkPanel = new ImagePanel();
		albumArtworkPanel.setPreferredSize(new Dimension(57, 57));
		completedContent.add(albumArtworkPanel, BorderLayout.WEST);

		JPanel center = new JPanel(new GridLayout(2, 1));
		this.panels.add(center);
		center.setBackground(new Color(255, 0, 0, 0));

		JPanel centerTop = new JPanel(new BorderLayout());
		this.panels.add(centerTop);
		centerTop.setBackground(new Color(255, 0, 0, 0));

		songLabel = new JLabel("Song title");
		songLabel.setFont(TITLE_FONT);
		songLabel.setPreferredSize(new Dimension(1000, 23));
		centerTop.add(songLabel, BorderLayout.SOUTH);

		center.add(centerTop);

		JPanel centerBottom = new JPanel(new BorderLayout());
		this.panels.add(centerBottom);

		JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottomRow.setSize(500, 20);
		// bottomRow.setBackground(new Color(255,0,0));

		this.panels.add(bottomRow);

		artistLabel = new JLabel("The Artist");
		artistLabel.setFont(ARTIST_FONT);
		// artistLabel.setPreferredSize(new Dimension(1000, 20));
		this.albumLabel = new JLabel("Test album");
		albumLabel.setFont(ALBUM_FONT);
		albumLabel.setForeground(ALBUM_COLOR);
		bottomRow.add(artistLabel);
		bottomRow.add(albumLabel);

		centerBottom.add(bottomRow, BorderLayout.NORTH);

		center.add(centerBottom);

		completedContent.add(center, BorderLayout.CENTER);

		progressBar = new JProgressBar();
		progressBar.setBounds(6, 29, 396, 20);
		progressBar.setStringPainted(true);
		// add(progressBar, BorderLayout.CENTER);

		videoLabel = new JLabel(videoTitle);
		videoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		videoLabel.setBounds(6, 6, 414, 16);

		JPanel loadingContentCenter = new JPanel(new BorderLayout());
		this.panels.add(loadingContentCenter);
		loadingContentCenter.add(videoLabel, BorderLayout.NORTH);
		loadingContentCenter.add(progressBar, BorderLayout.CENTER);

		loadingContent.add(loadingContentNorth, BorderLayout.NORTH);
		loadingContent.add(loadingContentCenter, BorderLayout.CENTER);
		
		this.add(loadingContent);
		// add(videoLabel, BorderLayout.CENTER);
		// this.setBorder(BorderFactory.createLineBorder(Color.black));
		for(JPanel panel : panels) {
			panel.setBackground(new Color(245,245,245));
		}
	}

	public void completeInfo(SongInfo info) {
		this.info = info;
		this.remove(loadingContent);
		this.add(completedContent);

		setArtist(info.getArtist());
		setTitle(info.getTitle());
		setAlbum(info.getAlbum());
		setAlbumArtwork(info.getAlbumArtwork());
		this.resultPanel = new ResultPanel(ItemPanel.this, ItemPanel.this.info);
		this.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent arg0) {
			}

			public void mousePressed(MouseEvent arg0) {
				 DownloadGUI.instance.changePanel(ItemPanel.this.resultPanel);
				//DownloadGUI.instance.changePanel(new ResultPanel(ItemPanel.this, ItemPanel.this.info));
			}

			public void mouseExited(MouseEvent arg0) {
			}

			public void mouseEntered(MouseEvent arg0) {
			}

			public void mouseClicked(MouseEvent event) {
			}
		});
	}

	public void setTitle(String title) {
		songLabel.setText(title);
	}

	public void setArtist(String artist) {
		artistLabel.setText(artist);
	}

	public void setAlbum(String album) {
		if(album == null) {
			albumLabel.setText("");
			return;
		}
		albumLabel.setText(album);
	}

	public void setAlbumArtwork(byte[] artwork) {
		albumArtworkPanel.setImage(artwork);
	}

	public void setProgressMin(int min) {
		progressBar.setMinimum(min);
	}

	public void setProgress(int value) {
		progressBar.setValue(value);
	}

	public void setProgressMax(int max) {
		progressBar.setMaximum(max);
	}

	public void setProgressIndeterminate(boolean value) {
		progressBar.setIndeterminate(value);
	}

	public void setProgressText(String text) {
		progressBar.setString(text);
	}

	public static Color cachedColor;

	public int getId() {
		return id;
	}

	public SongInfo getInfo() {
		return info;
	}

	public void setInfo(SongInfo info) {
		this.info = info;
	}
	public void updateColor() {
		int id = getId();
		if (id != -1) {
			Border border = BorderFactory.createMatteBorder(0, 0, 1, 0, DownloadGUI.COLOR_BORDER);
			border = BorderFactory.createCompoundBorder(border, new EmptyBorder(5, 5, 5, 0));
			this.setBorder(border);
		} else {
			this.setBorder(null);
		}
		/*
		 * if (id % 2 == 0) { if (cachedColor == null) { cachedColor = this.getParent().getBackground(); cachedColor =
		 * col(cachedColor.getRed() + 30, cachedColor.getGreen() + 30, cachedColor.getBlue() + 30); } for(JPanel p : panels)
		 * p.setBackground(cachedColor); this.repaint(); } else { for(JPanel p : panels) if(p != null)
		 * p.setBackground(this.getParent().getBackground()); this.repaint(); }
		 */
	}

	@Override
	public void componentResized(ComponentEvent e) {
		updateColor();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		updateColor();
	}

	@Override
	public void componentShown(ComponentEvent e) {
		updateColor();
	}

	@Override
	public void componentHidden(ComponentEvent e) {

	}
}
