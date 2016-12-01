package org.renci.ahab.libtransport;

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
	
	public String toString() {
		StringBuilder sb  = new StringBuilder();
		sb.append("{ ");
		for(String s: publicKeys) {
			sb.append("[ " + s + " ] ");
		}
		sb.append(" }/" + sudo);
		return sb.toString();
	}
}
