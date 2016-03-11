package orca.ahab.libtransport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context maintaining access tokens to elements of the slice
 * @author ibaldin
 *
 * @param <T>
 */
public class SliceAccessContext<T extends AccessToken> {
	private static final String ALL_ELEMENTS = "ALL";
	protected Map<String, Map<String, List<T>>> elementTokens = new HashMap<>();
	protected Map<String, List<T>> globalTokens = new HashMap<>();
	
	/**
	 * Add access token for user id on element id of the slice
	 * @param userid
	 * @param elementId
	 * @param token
	 */
	public void addToken(String userid, String elementId, T token) {
		if (elementTokens.containsKey(userid)) {
			if (elementTokens.get(userid).containsKey(elementId)) {
				elementTokens.get(userid).get(elementId).add(token);
			} else
				elementTokens.get(userid).put(elementId, new ArrayList<T>(Arrays.asList(token)));
		} else {
			Map<String, List<T>> insert = new HashMap<>();
			insert.put(elementId, new ArrayList<T>(Arrays.asList(token)));
			elementTokens.put(userid, insert);
		}
	}
	
	/**
	 * Add access token for user id on all elements of the slice. Will override
	 * anything specified with addToken
	 * @param userid
	 * @param token
	 */
	public void addToken(String userid, T token) {
		if (globalTokens.containsKey(userid))
			globalTokens.get(userid).add(token);
		else
			globalTokens.put(userid, new ArrayList<T>(Arrays.asList(token)));
	}
	
	/**
	 * Remove tokens for this userid and element
	 * @param userid
	 * @param elementId
	 */
	public void removeToken(String userid, String elementId) {
		if (elementTokens.containsKey(userid)) {
			elementTokens.get(userid).remove(elementId);
		}
	}
	
	/**
	 * Remove all tokens for this userid
	 * @param userid
	 */
	public void removeToken(String userid) {
		elementTokens.remove(userid);
		globalTokens.remove(userid);
	}

	/**
	 * Apply transport adapter to get a transport-compatible map of tokens
	 * @return
	 */
	public Object getTokens(ISliceAccessAPIAdapter<T> adapter) {
		if (adapter != null) {
			// replace all specific entries with global ones
			for(Map.Entry<String, List<T>> entry: globalTokens.entrySet()) {
				elementTokens.remove(entry.getKey());
				Map<String, List<T>> insert = new HashMap<>();
				insert.put(ALL_ELEMENTS, entry.getValue());
				elementTokens.put(entry.getKey(), insert);
			}
			return adapter.convertTokens(elementTokens);
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("SliceAccessContext Per-element tokens:\n");
		for(Map.Entry<String, Map<String, List<T>>> e: elementTokens.entrySet()) {
			sb.append("\t" + e.getKey() + "\n");
			for(Map.Entry<String, List<T>> e1: e.getValue().entrySet()) {
				sb.append("\t\t" + e1.getKey() + " " + e1.getValue() + "\n");
			}
			sb.append("\n");
		}
		
		sb.append("\nSliceAccessContext Global tokens:\n");
		for(Map.Entry<String, List<T>> e: globalTokens.entrySet()) {
			sb.append("\t" + e.getKey() + " " + e.getValue());
		}
		return sb.toString();
	}
}
