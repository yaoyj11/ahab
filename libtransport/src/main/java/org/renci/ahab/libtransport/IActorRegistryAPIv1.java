package org.renci.ahab.libtransport;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.renci.ahab.libtransport.xmlrpc.XMLRPCTransportException;

public interface IActorRegistryAPIv1 {

	/**
	 * Get known AMs from registry
	 * @param verbose
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public Map<String, Map<String, String>> getAMs(boolean verbose, URL url)
			throws Exception;

	/**
	 * Get known images from registry
	 * @param url
	 * @return
	 * @throws XMLRPCTransportException
	 */
	public List<Map<String, String>> getImages(URL url)
			throws XMLRPCTransportException;

	/**
	 * Get known controllers from registry
	 * @param url
	 * @return
	 * @throws XMLRPCTransportException
	 */
	public List<Map<String, String>> getControllers(URL url)
			throws XMLRPCTransportException;

}