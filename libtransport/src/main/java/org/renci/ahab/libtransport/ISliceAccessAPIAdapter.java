package org.renci.ahab.libtransport;

import java.util.List;
import java.util.Map;

/**
 * An adapter that knows how to convert things for a particular transport
 * Takes token type
 * @author ibaldin
 *
 */
public interface ISliceAccessAPIAdapter<T> {
	
	public List<Map<String, Object>> convertTokens(Map<String, Map<String, List<T>>> m);
}
