package utilities;
import java.io.Serializable;

public class Session implements Serializable {
	private static final long serialVersionUID = -8965128402111732485L;
	
	private long sessionStart;
	
	private String username;
	
	public Session() {
		renewSession();
	}
	
	public long getSessionStart() {
		return sessionStart;
	}
	
	public long getSessionLength() {
		return System.currentTimeMillis() - sessionStart;
	}
	
	public boolean hasExpired() {
		return getSessionLength() >= (30 * 60 * 1000);
	}
	
	public void renewSession() {
		sessionStart = System.currentTimeMillis();
	}
	
	public String getUsername() {
		return username;
	}
	
}
