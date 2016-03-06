package orca.ahab.libtransport;

import java.util.Collections;
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
	protected Map<String, Map<String, List<T>>> tokens;
	
	/**
	 * Add access token for user id on element id of the slice
	 * @param userid
	 * @param elementId
	 * @param token
	 */
	public void addToken(String userid, String elementId, T token) {
		if (tokens.containsKey(userid)) {
			if (tokens.get(userid).containsKey(elementId)) {
				tokens.get(userid).get(elementId).add(token);
			} else
				tokens.get(userid).put(elementId, Collections.singletonList(token));
		} else {
			Map<String, List<T>> insert = new HashMap<>();
			insert.put(elementId, Collections.singletonList(token));
			tokens.put(userid, insert);
		}
	}
	
	public void removeToken(String userid, String elementId) {
		if (tokens.containsKey(userid)) {
			tokens.get(userid).remove(elementId);
		}
	}
	
	public void removeToken(String userid) {
		tokens.remove(userid);
	}

	/**
	 * Apply transport adapter to get a transport-compatible map of tokens
	 * @return
	 */
	public Object getTokens(ISliceAccessAPIAdapter<T> adapter) {
		if (adapter != null)
			return adapter.convertTokens(tokens);
		return null;
	}
}
