import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.coleman.utilities.http.Client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

public class SearchPanel extends JPanel {
	private static final long serialVersionUID = -1270810964594759400L;
	public JTextField searchTextField;
	// public JTextField autocompleteTextField;
	public JButton searchButton;
	public JScrollPane scrollPane;
	public JPanel panelList;

	private SearchListener searchListener;

	/**
	 * Create the panel.
	 */
	public SearchPanel() {
		setLayout(new BorderLayout(0, 0));

		searchTextField = new JTextField();
		searchTextField.setBounds(0, 0, 370, 28);
		searchTextField.setBackground(new Color(255, 255, 255, 100));
		searchListener = new SearchListener();
		searchTextField.getDocument().addDocumentListener(searchListener);
		searchTextField.addKeyListener(new KeyAdapter() {
			// TODO Cache the search results OR just do a manual cache type
			// thing OR both
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == '\t') {
					e.consume();
					String activeOption = searchListener.getActiveOption();
					if (activeOption != null && !activeOption.trim().isEmpty()) {
						searchTextField.setText(activeOption);
					}
					return;
				}
				if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					e.consume();
					searchListener.nextOption();
					return;
				}
				if (e.getKeyCode() == KeyEvent.VK_UP) {
					e.consume();
					searchListener.previousOption();
					return;
				}
			}
		});
		searchTextField.setFocusTraversalKeysEnabled(false);

		/*
		 * autocompleteTextField = new JTextField();
		 * autocompleteTextField.setBounds(0, 0, 370, 28);
		 * autocompleteTextField.setFocusable(false);
		 * autocompleteTextField.setEnabled(false);
		 */

		searchButton = new JButton("Search");
		searchButton.setBounds(371, 1, 79, 29);

		JPanel topRow = new JPanel(new BorderLayout(0, 0));
		JPanel searchArea = new JPanel(new BorderLayout(0, 0));
		searchArea.add(searchTextField, BorderLayout.CENTER);
		// searchArea.add(autocompleteTextField, BorderLayout.CENTER);
		topRow.add(searchArea, BorderLayout.CENTER);
		topRow.add(searchButton, BorderLayout.EAST);
		this.add(topRow, BorderLayout.NORTH);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(6, 40, 438, 254);
		scrollPane.getVerticalScrollBar().setUnitIncrement(5);
		this.add(scrollPane, BorderLayout.CENTER);

		panelList = new JPanel();
		scrollPane.setViewportView(panelList);
		GridBagLayout gbl_panelList = new GridBagLayout();
		gbl_panelList.columnWidths = new int[] { 50 };
		gbl_panelList.rowHeights = new int[] { 50 };
		gbl_panelList.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_panelList.rowWeights = new double[] { Double.MIN_VALUE };
		panelList.setLayout(new BoxLayout(panelList, BoxLayout.PAGE_AXIS));

		// System.out.println(Arrays.toString(UIManager.getInstalledLookAndFeels()));
	}

	class SearchListener implements DocumentListener {
		protected int searchIndex = 0;
		public boolean IGNORE_NEXT = false;
		public boolean IGNORE_NEXT_DEL = false;
		private Object recentLock = null;
		private Client client = new Client();
		private ArrayList<String> options = new ArrayList<String>();
		private String query;
		public SearchListener() {
			cachedCompletion.setMaxSize(100);
		}
		public void changedUpdate(DocumentEvent e) {

		}

		public void removeUpdate(DocumentEvent e) {
			if (IGNORE_NEXT_DEL) {
				IGNORE_NEXT_DEL = false;
				return;
			}
			onUserChange();
			synchronized (queryUseLock) {
				searchIndex = 0;
			}
		}

		public void insertUpdate(DocumentEvent e) {
			if (IGNORE_NEXT) {
				IGNORE_NEXT = false;
				return;
			}
			onUserChange();
			processEvent(e);
		}

		private void onUserChange() {
			synchronized (queryUseLock) {
				searchIndex = 0;
				options.clear();
				query = null;
			}
		}

		public void nextOption() {
			synchronized (queryUseLock) {
				if (searchIndex < options.size() - 1)
					searchIndex++;
			}
			showOption();
		}

		public void previousOption() {
			synchronized (queryUseLock) {
				if (searchIndex > 0)
					searchIndex--;
			}
			showOption();
		}

		public String getActiveOption() {
			synchronized (queryUseLock) {
				if (options.size() == 0 || query == null) {
					return null;
				}
				return options.get(searchIndex);
			}
		}

		public void showOption() {
			synchronized (queryUseLock) {
				String match = getActiveOption();
				if (match == null)
					return;

				searchListener.IGNORE_NEXT_DEL = true;
				searchListener.IGNORE_NEXT = true;
				searchTextField.setText(match);
				searchTextField.setSelectionStart(query.length());
				searchTextField.setSelectionEnd(match.length());
			}
		}

		private final Object queryUseLock = new Object();

		private CacheMap<String, String[]> cachedCompletion = new CacheMap<>();
		
		public void processEvent(final DocumentEvent e) {
			// If they make the change before the end, ignore it
			if (e != null && e.getOffset() != e.getDocument().getLength() - 1) {
				return;
			}
			synchronized (queryUseLock) {
				searchIndex = 0;
				options.clear();
				query = null;
			}
			final Object thisLock = new Object();
			recentLock = thisLock;
			new Thread() {
				public void run() {
					String url;
					final String QUERY = searchTextField.getText();
					SearchListener.this.query = QUERY;
					if (QUERY.trim().isEmpty()) {
						return;
					}
					
					ArrayList<String> newOptions;
					if (cachedCompletion.containsKey(QUERY)) {
						newOptions = new ArrayList<>();
						for (String s : cachedCompletion.get(QUERY))
							newOptions.add(s);
					} else {

						try {
							url = "http://suggestqueries.google.com/complete/search?hl=en&ds=yt&client=firefox&hjson=t&cp=1&alt=json&q="
									+ URLEncoder.encode(QUERY, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
							return;
						}
						// url =
						// "https://clients1.google.com/complete/search?client=youtube&hl=en&gl=us&gs_rn=11&gs_ri=youtube&ds=yt&cp=1&gs_id=4&q="
						// + searchTextField.getText();
						String callback = new String(client.readSite(url));
						callback = callback.substring(1, callback.length() - 1);
						callback = "{" + callback + "}";
						callback = callback.replaceFirst("\".*?\",",
								"\"values\":");
						JSONObject object = new JSONObject(new JSONTokener(
								callback));
						JSONArray array = object.getJSONArray("values");
						if (array.length() == 0) {
							newOptions = new ArrayList<String>();
							cachedCompletion.put(QUERY,new String[0]);
						} else {
							newOptions = new ArrayList<>(array.length());
							char[] queryArray = QUERY.toCharArray();
							for (int i = 0; i < array.length(); i++) {
								String match = array.getString(i);

								char[] matchArray = match.toCharArray();
								if (matchArray.length < queryArray.length) {
									continue;
								}

								int j = 0;
								boolean isValid = true;
								for (char queryChar : queryArray) {
									char matchChar = matchArray[j];
									if (Character.toLowerCase(matchChar) != Character
											.toLowerCase(queryChar)) {
										isValid = false;
										break;
									}
									matchArray[j] = queryChar;
									j++;
								}
								if (!isValid)
									continue;
								newOptions.add(new String(matchArray));
							}
							cachedCompletion.put(QUERY,
									newOptions.toArray(new String[0]));
						}
					}

					if (recentLock != thisLock) {
						// System.out.println("Cancelled " + QUERY);
						return;
					}
					synchronized (queryUseLock) {
						options = newOptions;
					}
					// System.out.println("Completed " + QUERY);
					// System.out.println("OPTIONS: " + options);
					showOption();

				}
			}.start();
		}

	}
}