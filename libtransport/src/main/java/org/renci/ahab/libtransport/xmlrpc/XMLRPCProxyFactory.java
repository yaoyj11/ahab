package org.renci.ahab.libtransport.xmlrpc;

import java.net.URL;
import java.util.List;

import org.renci.ahab.libtransport.IActorRegistryAPIv1;
import org.renci.ahab.libtransport.IConverterAPIv1;
import org.renci.ahab.libtransport.IGENICHAPIv1;
import org.renci.ahab.libtransport.ISliceTransportAPIv1;
import org.renci.ahab.libtransport.ITransportProxyFactory;
import org.renci.ahab.libtransport.SSLTransportContext;
import org.renci.ahab.libtransport.TransportContext;
import org.renci.ahab.libtransport.util.ContextTransportException;
import org.renci.ahab.libtransport.util.TransportException;

/**
 * Generate various XMLRPC proxies
 * @author ibaldin
 *
 */
public class XMLRPCProxyFactory implements ITransportProxyFactory {
	
	@Override
	public ISliceTransportAPIv1 getSliceProxy(TransportContext tc, URL url)
			throws TransportException, ContextTransportException {
		return new OrcaSMXMLRPCProxy((SSLTransportContext)tc, url);
	}

	@Override
	public IGENICHAPIv1 getGENICHProxy(TransportContext tc, URL url)
			throws TransportException, ContextTransportException {
		return new GENICHXMLRPCProxy((SSLTransportContext)tc, url);
	}

	@Override
	public IActorRegistryAPIv1 getRegistryProxy(String fp, URL url)
			throws TransportException {
		return new RegistryXMLRPCProxy(fp, url);
	}

	@Override
	public IConverterAPIv1 getConverterProxy(List<URL> urls)
			throws TransportException {
		return new NDLConverterXMLRPCProxy(urls);
	}

}
