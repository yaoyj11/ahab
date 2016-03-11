package orca.ahab.libtransport;

import java.net.URL;
import java.util.Collections;

import junit.framework.TestCase;
import orca.ahab.libtransport.xmlrpc.XMLRPCProxyFactory;


public class TestXMLRPCTransport extends TestCase {

	public void testXMLRPCFactory() {
		
		try {
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			TransportContext ctx = new JKSTransportContext("selfsigned", "selfpassword", 
					"files/testkeystore.jks");
			ifac.getConverterProxy(Collections.singletonList(new URL("http://geni.renci.org:15080/convert")));
			ifac.getGENICHProxy(ctx, new URL("http://portal.geni.net/ch"));
			ifac.getRegistryProxy("78:B6:1A:F0:6C:F8:C7:0F:C0:05:10:13:06:79:E0:AC", new URL("https://geni.renci.org:15443/registry/"));
			ifac.getSliceProxy(ctx, new URL("https://geni.renci.org:15443/orca/xmlrpc"));
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Proxy factory test failed");
			assert(false);
		}
		System.out.println("Proxy factory test succeeded");
	}
}
