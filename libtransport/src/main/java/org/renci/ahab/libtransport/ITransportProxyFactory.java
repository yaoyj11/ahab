package org.renci.ahab.libtransport;

import java.net.URL;
import java.util.List;

import org.renci.ahab.libtransport.util.ContextTransportException;
import org.renci.ahab.libtransport.util.TransportException;

/**
 * Factory to generate various proxies for a particular transport
 * @author ibaldin
 *
 */
public interface ITransportProxyFactory {
	
	public ISliceTransportAPIv1 getSliceProxy(TransportContext tc, URL url) throws TransportException, ContextTransportException;
	
	public IGENICHAPIv1 getGENICHProxy(TransportContext tc, URL url) throws TransportException, ContextTransportException;
	
	public IActorRegistryAPIv1 getRegistryProxy(String fingerprint, URL url) throws TransportException;
	
	public IConverterAPIv1 getConverterProxy(List<URL> urls) throws TransportException;
}
