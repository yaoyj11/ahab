package orca.ahab.libtransport;

import java.util.List;

/**
 * A bean that holds SSH access information
 * @author ibaldin
 *
 */
public class SSHAccessToken extends AccessToken {
	List<String> publicKeys;
	boolean sudo;
	
	public SSHAccessToken(List<String> k, boolean s) {
		sudo = s;
		publicKeys = k;
	}
	
	public List<String> getKeys() {
		return publicKeys;
	}
	
	public boolean getSudo() {
		return sudo;
	}
}
