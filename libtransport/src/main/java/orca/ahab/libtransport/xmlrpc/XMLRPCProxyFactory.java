package orca.ahab.libtransport.xmlrpc;

import java.net.URL;
import java.util.List;

import orca.ahab.libtransport.IActorRegistryAPIv1;
import orca.ahab.libtransport.IConverterAPIv1;
import orca.ahab.libtransport.IGENICHAPIv1;
import orca.ahab.libtransport.ISliceTransportAPIv1;
import orca.ahab.libtransport.ITransportProxyFactory;
import orca.ahab.libtransport.SSLTransportContext;
import orca.ahab.libtransport.TransportContext;
import orca.ahab.libtransport.util.ContextTransportException;
import orca.ahab.libtransport.util.TransportException;

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
