package utilities.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class Database<K, V> implements Serializable {
	private static final long serialVersionUID = -4330535745101300738L;
	
	private HashMap<K, V> map;
	
	public Database() {
		this.map = new HashMap<K, V>();
	}
	
	public synchronized void store(K key, V value) {
		assertSerializable( key.getClass() );
		assertSerializable( value.getClass() );
		
		map.put( key, value );
	}
	
	public synchronized void remove(K key) {
		map.remove( key );
	}
	
	public synchronized void addDefault(K key, V value) {
		assertSerializable( key.getClass() );
		assertSerializable( value.getClass() );
		if (!map.containsKey( key ))
			map.put( key, value );
	}
	
	public synchronized V get(K key) {
		V value = map.get( key );
		return value;
	}
	
	public synchronized V get(K key, V defaultValue) {
		if (!map.containsKey( key ))
			return defaultValue;
		V value = map.get( key );
		return value;
	}
	
	public synchronized <T> T get(K key, Class<T> clazz) {
		V value = map.get( key );
		return clazz.cast( value );
	}
	
	public synchronized <T> T get(K key, Class<T> clazz, T defaultValue) {
		if (!map.containsKey( key ))
			return defaultValue;
		V value = map.get( key );
		return clazz.cast( value );
	}
	
	public synchronized boolean containsKey(K key) {
		return map.containsKey( key );
	}
	
	public synchronized boolean containsValue(V value) {
		return map.containsValue( value );
	}
	
	public synchronized HashMap<K, V> getMap() {
		return map;
	}
	
	public void writeTo(OutputStream output) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream( output );
		oos.writeObject( this );
	}
	
	@SuppressWarnings("unchecked")
	public void readFrom(InputStream input) throws IOException {
		ObjectInputStream oos = new ObjectInputStream( input );
		try {
			Database<?, ?> database = (Database<?, ?>) oos.readObject();
			this.map = (HashMap<K, V>) database.map;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	static {
		assertSerializable( Database.class );
	}
	
	public static void assertSerializable(Class<?> clazz) {
		if (clazz.isArray()) {
			assertSerializable( clazz.getComponentType() );
		}
		for (Field field : clazz.getDeclaredFields()) {
			if ((field.getModifiers() & Modifier.STATIC) != 0 | (field.getModifiers() & Modifier.TRANSIENT) != 0)
				continue;
			if (field.getType().isPrimitive())
				continue;
			if (Serializable.class.isAssignableFrom( field.getType() )) {
				assertSerializable( field.getType() );
				continue;
			}
			
			throw new IllegalArgumentException( "Class " + field.getType().getName() + " is not serializable in field " + field.getName() + "!" );
		}
	}
}
