package core;
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
/*
Agency FB
Algerian
AlienCaret
Arial
Arial Black
Arial Narrow
Arial Rounded MT Bold
Arial Unicode MS
Baskerville Old Face
Bauhaus 93
Bell MT
Berlin Sans FB
Berlin Sans FB Demi
Bernard MT Condensed
Blackadder ITC
Bodoni MT
Bodoni MT Black
Bodoni MT Condensed
Bodoni MT Poster Compressed
Book Antiqua
Bookman Old Style
Bookshelf Symbol 7
Bradley Hand ITC
Britannic Bold
Broadway
Brush Script MT
Calibri
Calibri Light
Californian FB
Calisto MT
Cambria
Cambria Math
Candara
Castellar
Centaur
Century
Century Gothic
Century Schoolbook
Chiller
Colonna MT
Comic Sans MS
Consolas
Constantia
Cooper Black
Copperplate Gothic Bold
Copperplate Gothic Light
Corbel
Courier New
Curlz MT
Dialog
DialogInput
Ebrima
Edwardian Script ITC
Elephant
Engravers MT
Eras Bold ITC
Eras Demi ITC
Eras Light ITC
Eras Medium ITC
Felix Titling
Footlight MT Light
Forte
Franklin Gothic Book
Franklin Gothic Demi
Franklin Gothic Demi Cond
Franklin Gothic Heavy
Franklin Gothic Medium
Franklin Gothic Medium Cond
Freestyle Script
French Script MT
Gabriola
Gadugi
Garamond
Georgia
Gigi
Gill Sans MT
Gill Sans MT Condensed
Gill Sans MT Ext Condensed Bold
Gill Sans Ultra Bold
Gill Sans Ultra Bold Condensed
Gloucester MT Extra Condensed
Goudy Old Style
Goudy Stout
Haettenschweiler
Harlow Solid Italic
Harrington
High Tower Text
Impact
Imprint MT Shadow
Informal Roman
InputMono Light
InputSans Light
InputSerif Light
Javanese Text
Jokerman
Juice ITC
Kristen ITC
Kunstler Script
Leelawadee UI
Leelawadee UI Semilight
Lucida Bright
Lucida Calligraphy
Lucida Console
Lucida Fax
Lucida Handwriting
Lucida Sans
Lucida Sans Typewriter
Lucida Sans Unicode
Magneto
Maiandra GD
Malgun Gothic
Malgun Gothic Semilight
Marlett
Matura MT Script Capitals
Microsoft Himalaya
Microsoft JhengHei
Microsoft JhengHei Light
Microsoft JhengHei UI
Microsoft JhengHei UI Light
Microsoft New Tai Lue
Microsoft PhagsPa
Microsoft Sans Serif
Microsoft Tai Le
Microsoft YaHei
Microsoft YaHei Light
Microsoft YaHei UI
Microsoft YaHei UI Light
Microsoft Yi Baiti
MingLiU-ExtB
MingLiU_HKSCS-ExtB
Mistral
Modern No. 20
Mongolian Baiti
Monospaced
Monotype Corsiva
MS Gothic
MS Outlook
MS PGothic
MS Reference Sans Serif
MS Reference Specialty
MS UI Gothic
MT Extra
MV Boli
Myanmar Text
Niagara Engraved
Niagara Solid
Nirmala UI
Nirmala UI Semilight
NSimSun
OCR A Extended
Old English Text MT
Onyx
Palace Script MT
Palatino Linotype
Papyrus
Parchment
Perpetua
Perpetua Titling MT
Pill Gothic 600mg Light
Pill Gothic 600mg Semibd
Playbill
PMingLiU-ExtB
Poor Richard
Pristina
Rage Italic
Ravie
Rockwell
Rockwell Condensed
Rockwell Extra Bold
SansSerif
Script MT Bold
Segoe MDL2 Assets
Segoe Print
Segoe Script
Segoe UI
Segoe UI Black
Segoe UI Emoji
Segoe UI Historic
Segoe UI Light
Segoe UI Semibold
Segoe UI Semilight
Segoe UI Symbol
Serif
Showcard Gothic
SimSun
SimSun-ExtB
Sitka Banner
Sitka Display
Sitka Heading
Sitka Small
Sitka Subheading
Sitka Text
Snap ITC
Stencil
Sylfaen
Symbol
Tahoma
TeamViewer10
Tempus Sans ITC
Times New Roman
Trebuchet MS
Tw Cen MT
Tw Cen MT Condensed
Tw Cen MT Condensed Extra Bold
Verdana
Viner Hand ITC
Vivaldi
Vladimir Script
Webdings
Wide Latin
Wingdings
Wingdings 2
Wingdings 3
Yu Gothic
Yu Gothic Light
Yu Gothic Medium
Yu Gothic UI
Yu Gothic UI Light
Yu Gothic UI Semibold
Yu Gothic UI Semilight
 */
public class ItemPanel extends JPanel implements ComponentListener {
	public static final Color ALBUM_COLOR = new Color(150,150,150);
	public static final String FONT_FAMILY = "Helvetica";
	public static final Font ALBUM_FONT = new Font(FONT_FAMILY, Font.PLAIN, 17);
	public static final Font ARTIST_FONT = new Font(FONT_FAMILY, Font.PLAIN, 17);
	public static final Font TITLE_FONT = new Font(FONT_FAMILY, Font.BOLD, 23);

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

		songLabel = new JLabelSmooth("Song title");
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

		artistLabel = new JLabelSmooth("The Artist");
		artistLabel.setFont(ARTIST_FONT);
		// artistLabel.setPreferredSize(new Dimension(1000, 20));
		this.albumLabel = new JLabelSmooth("Test album");
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

		videoLabel = new JLabelSmooth(videoTitle);
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
