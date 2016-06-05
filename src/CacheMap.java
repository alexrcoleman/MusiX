import java.util.ArrayDeque;
import java.util.HashMap;

public class CacheMap<K, V> extends HashMap<K, V> {
	private static final long serialVersionUID = 659436330396147028L;
	private ArrayDeque<K> deque;
	private HashMap<K, Integer> freq;
	private int maxSize;

	public CacheMap() {
		deque = new ArrayDeque<>();
		freq = new HashMap<>();
		maxSize = 50;
	}
	
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	public V put(K key, V value) {
		V ret = super.put(key, value);
		if (deque.isEmpty() || !deque.peekLast().equals(key)) {
			freq.put(key, (freq.containsKey(key) ? freq.get(key) : 0) + 1);
			deque.offer(key);
		}
		if (this.size() > maxSize) {
			K next;
			int fq;
			do {
				next = deque.poll();
				fq = freq.get(next);
				freq.put(next, --fq);
				if (fq == 0) {
					freq.remove(next);
				}
			} while (fq > 0 || !this.containsKey(next));
			this.remove(next);
		}

		return ret;

	}
}
