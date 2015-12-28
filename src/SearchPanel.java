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
			// TODO Cache the search results OR just do a manual cache type thing OR both
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == '\t') {
					if (searchTextField.getName() != null && !searchTextField
									.getName().trim().isEmpty()) {
						searchTextField.setText(searchTextField.getName());
					}
					return;
				}
				System.out.println(e.getKeyCode());
				if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					searchListener.IGNORE_NEXT_DEL = true;
					searchListener.IGNORE_NEXT = true;
					searchTextField.setText(searchTextField.getText().substring(0,searchTextField.getSelectionStart()));
					e.consume();
					searchListener.searchIndex++;
					searchListener.processEvent(null);
					return;
				}
				if (e.getKeyCode() == KeyEvent.VK_UP) {
					searchListener.IGNORE_NEXT_DEL = true;
					searchListener.IGNORE_NEXT = true;
					searchTextField.setText(searchTextField.getText().substring(0,searchTextField.getSelectionStart()));
					searchListener.searchIndex--;
					if (searchListener.searchIndex < 0)
						searchListener.searchIndex = 0;
					searchListener.processEvent(null);
					e.consume();
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
		protected int searchIndex;
		public boolean IGNORE_NEXT = false;
		public boolean IGNORE_NEXT_DEL = false;
		private Object recentLock = null;
		private Client client = new Client();

		public void changedUpdate(DocumentEvent e) {
			// processEvent(e);
		}

		public void removeUpdate(DocumentEvent e) {
			if (IGNORE_NEXT_DEL) {
				IGNORE_NEXT_DEL = false;
				return;
			}
			// processEvent(e);
			searchIndex = 0;
			System.out.println("RESET B");
		}

		public void insertUpdate(DocumentEvent e) {
			processEvent(e);
		}

		public void processEvent(final DocumentEvent e) {
			if (IGNORE_NEXT) {
				IGNORE_NEXT = false;
				return;
			}
			if(e != null) {
				searchIndex = 0;
				System.out.println("RESET A");
			}
			if (e != null && e.getOffset() != e.getDocument().getLength() - 1) {
				return;
			}
			new Thread() {
				public void run() {
					Object thisLock = new Object();
					recentLock = thisLock;

					String query = searchTextField.getText();
					if (query.trim().isEmpty()) {
						// autocompleteTextField.setText("");
						searchTextField.setName(null);
						return;
					}

					String url;
					try {
						url = "http://suggestqueries.google.com/complete/search?hl=en&ds=yt&client=firefox&hjson=t&cp=1&alt=json&q="
								+ URLEncoder.encode(query, "UTF-8");
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
					callback = callback.replaceFirst("\".*?\",", "\"values\":");
					JSONObject object = new JSONObject(
							new JSONTokener(callback));
					JSONArray array = object.getJSONArray("values");
					if (recentLock != thisLock)
						return;
					if (array.length() == 0) {
						// autocompleteTextField.setText("");
					} else {
						searchIndex = Math.max(searchIndex, 0);
						searchIndex = Math.min(searchIndex,array.length()-1);
						System.out.println("Index " + searchIndex  + " ( size = " + array.length() + ")");
						String match = array.getString(searchIndex);
						//searchIndex = 0;
						char[] matchArray = match.toCharArray();
						char[] queryArray = query.toCharArray();
						if (matchArray.length < queryArray.length) {
							// autocompleteTextField.setText("");
							searchTextField.setName("");
							return;
						}
						int i = 0;
						boolean isValid = true;
						for (char queryChar : queryArray) {
							char matchChar = matchArray[i];
							if (Character.toLowerCase(matchChar) != Character
									.toLowerCase(queryChar)) {
								isValid = false;
								break;
							}
							matchArray[i] = queryChar;
							i++;
						}
						match = new String(matchArray);
						if (recentLock != thisLock)
							return;
						if (isValid) {
							// autocompleteTextField.setText(match);
							IGNORE_NEXT = true;
							IGNORE_NEXT_DEL = true;
							searchTextField.setText(match);
							searchTextField.setSelectionStart(query.length());
							searchTextField.setSelectionEnd(match.length());
						}
						searchTextField.setName(match);
					}
				}
			}.start();
		}

	}
}

final class MyEntry<K, V> implements Map.Entry<K, V> {
	private final K key;
	private V value;

	public MyEntry(K key, V value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V value) {
		V old = this.value;
		this.value = value;
		return old;
	}
}