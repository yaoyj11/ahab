package org.renci.ahab.libtransport;

import java.net.URL;
import java.util.Collections;

import org.renci.ahab.libtransport.ISliceTransportAPIv1;
import org.renci.ahab.libtransport.ITransportProxyFactory;
import org.renci.ahab.libtransport.JKSTransportContext;
import org.renci.ahab.libtransport.TransportContext;
import org.renci.ahab.libtransport.xmlrpc.XMLRPCProxyFactory;

import junit.framework.TestCase;


public class TestXMLRPCTransport extends TestCase {

	public void testXMLRPCFactory() {
		
		try {
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			TransportContext ctx = new JKSTransportContext("selfsigned", "selfpassword", 
					"files/testkeystore.jks");
			ifac.getConverterProxy(Collections.singletonList(new URL("http://geni.renci.org:15080/convert")));
			ifac.getGENICHProxy(ctx, new URL("http://portal.geni.net/ch"));
			ifac.getRegistryProxy("78:B6:1A:F0:6C:F8:C7:0F:C0:05:10:13:06:79:E0:AC", new URL("https://geni.renci.org:15443/registry/"));
			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL("https://geni.renci.org:15443/orca/xmlrpc"));
			
			// now you can do things like 
			//sliceProxy.getVersion();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Proxy factory test failed");
			assert(false);
		}
		System.out.println("Proxy factory test succeeded");
	}
	
//	public void testXMLRPCGetVersion() {
//		try {
//			Logger logger = Logger.getRootLogger();//Logger.getLogger("org.apache.commons.httpclient.HttpClient");
//			logger.setLevel(Level.INFO);
//			ConsoleAppender capp = new ConsoleAppender();
//			capp.setImmediateFlush(true);
//			capp.setName("Test Console Appender");
//			org.apache.log4j.SimpleLayout sl = new SimpleLayout();
//			capp.setLayout(sl);
//			capp.setWriter(new PrintWriter(System.out));
//			logger.addAppender(capp);
//
//			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
//			TransportContext ctx = new PEMTransportContext("", "ddd.pem", 
//					"ddd.key");
//			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL("https://geni.renci.org:11443/orca/xmlrpc"));
//			
//			System.out.println(sliceProxy.getVersion());
//			// now you can do things like 
//			//sliceProxy.getVersion();
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.err.println("XMLRPC GetVersion test failed");
//			assert(false);
//		}
//		System.out.println("XMLRPC GetVersion test succeeded");
//	}
}
