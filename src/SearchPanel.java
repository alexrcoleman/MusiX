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
	//public JTextField autocompleteTextField;
	public JButton searchButton;
	public JScrollPane scrollPane;
	public JPanel panelList;

	/**
	 * Create the panel.
	 */
	public SearchPanel() {
		setLayout(new BorderLayout(0,0));
		
		searchTextField = new JTextField();
		searchTextField.setBounds(0, 0, 370, 28);
		searchTextField.setBackground(new Color(255, 255, 255, 100));
		searchTextField.getDocument().addDocumentListener(
				new DocumentListener() {
					public boolean IGNORE_NEXT = false;
					private Object recentLock = null;
					private Client client = new Client();

					public void changedUpdate(DocumentEvent e) {
						//processEvent(e);
					}

					public void removeUpdate(DocumentEvent e) {
						//processEvent(e);
					}

					public void insertUpdate(DocumentEvent e) {
						processEvent(e);
					}

					public void processEvent(final DocumentEvent e) {
						if(IGNORE_NEXT) {
							IGNORE_NEXT = false;
							return;
						}
						if(e.getOffset() != e.getDocument().getLength()-1) {
							return;
						}
						new Thread() {
							public void run() {
								Object thisLock = new Object();
								recentLock = thisLock;
								
								String query = searchTextField.getText();
								if (query.trim().isEmpty()) {
									//autocompleteTextField.setText("");
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
								String callback = new String(client
										.readSite(url));
								callback = callback.substring(1,
										callback.length() - 1);
								callback = "{" + callback + "}";
								callback = callback.replaceFirst("\".*?\",",
										"\"values\":");
								JSONObject object = new JSONObject(
										new JSONTokener(callback));
								JSONArray array = object.getJSONArray("values");
								if(recentLock != thisLock)
									return;
								if (array.length() == 0) {
								//	autocompleteTextField.setText("");
								} else {
									String match = array.getString(0);
									char[] matchArray = match.toCharArray();
									char[] queryArray = query.toCharArray();
									if(matchArray.length < queryArray.length) {
										//autocompleteTextField.setText("");
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
									if(recentLock != thisLock)
										return;
									if (isValid) {
										//autocompleteTextField.setText(match);
										IGNORE_NEXT = true;
										searchTextField.setText(match);
										searchTextField.setSelectionStart(query.length());
										searchTextField.setSelectionEnd(match.length());
									}
									searchTextField.setName(match);
								}
							}
						}.start();
					}
				});
		searchTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == '\t') {
					@SuppressWarnings("unchecked")
					HashMap<String, List<String>> predictions = Analytics.database
							.get("predictions",
									HashMap.class,
									new HashMap<String, String>());
					if(predictions.size() <= 0 || (searchTextField.getName() != null && !searchTextField.getName().trim().isEmpty())) {
						searchTextField.setText(searchTextField.getName());
					} else {
						List<Entry<String,String>> entries = new ArrayList<Entry<String,String>>();
						Set<Entry<String, List<String>>> values = predictions.entrySet();
						for(Entry<String, List<String>> entry : values) {
							for(String s : entry.getValue()) {
								entries.add(new MyEntry<String,String>(entry.getKey(), s));
							}
						}
						Random generator = new Random();
						Entry<String,String> randomValue = entries.get(generator.nextInt(entries.size()));
						String artist = randomValue.getKey();
						String song = randomValue.getValue();
						//autocompleteTextField.setText("");
						searchTextField.setName(null);
						searchTextField.setText(song + " by " + artist);
					}
				}
			}
		});
		searchTextField.setFocusTraversalKeysEnabled(false);
		
		/*autocompleteTextField = new JTextField();
		autocompleteTextField.setBounds(0, 0, 370, 28);
		autocompleteTextField.setFocusable(false);
		autocompleteTextField.setEnabled(false);*/

		searchButton = new JButton("Search");
		searchButton.setBounds(371, 1, 79, 29);
		
		JPanel topRow = new JPanel(new BorderLayout(0,0));
		JPanel searchArea = new JPanel(new BorderLayout(0,0));
		searchArea.add(searchTextField, BorderLayout.CENTER);
		//searchArea.add(autocompleteTextField, BorderLayout.CENTER);
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
		
		//System.out.println(Arrays.toString(UIManager.getInstalledLookAndFeels()));
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