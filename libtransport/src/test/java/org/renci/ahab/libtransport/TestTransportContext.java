package org.renci.ahab.libtransport;

import java.net.URL;

import org.renci.ahab.libtransport.JKSTransportContext;
import org.renci.ahab.libtransport.PEMTransportContext;
import org.renci.ahab.libtransport.TransportContext;

import junit.framework.TestCase;

public class TestTransportContext extends TestCase {

	public void testJKS1() {
		try {
			TransportContext ctx = new JKSTransportContext("selfsigned", "selfpassword", 
					"files/testkeystore.jks");
			System.out.println("JKS2 " + ctx);
		} catch(Exception e) {
			System.err.println("JKS1 Test failed: " + e);
			assert(false);
		}
	}
	
	public void testJKS2() {
		try {
			TransportContext ctx = new JKSTransportContext("alias", "pass", 
					"path/to/jks");
			System.out.println("JKS2 " + ctx);
		} catch(Exception e) {
			System.out.println("JKS2 Negative test passed");
			return;
		}
		System.out.println("JKS2 Negative test failed");
		assert(false);
	}
	
	public void testJKS3() {
		try {
			TransportContext ctx = new JKSTransportContext("selfsigned", "selfpassword", 
					"files/testkeystore.jks");
			System.out.println(ctx);
			ctx.establishIdentity(new URL("https://geni.renci.org:15443/orca"));
		} catch(Exception e) {
			System.err.println("JKS3 Test failed: " + e);
			assert(false);
		}
	}
	
	public void testPEM1() {
		try {
			TransportContext ctx = new PEMTransportContext("", "files/selftest.crt", "files/selftest.key");
			System.out.println("PEM1 " + ctx);
			
		} catch(Exception e) {
			System.err.println("PEM1 Test failed: " + e);
			assert(false);
		}
	}
	
	public void testPEM2() {
		try {
			TransportContext ctx = new PEMTransportContext("", " /home/geni-orca/.ssl/geni-pruth.pem", " /home/geni-orca/.ssl/geni_ssl_portal.key");
			System.out.println("PEM2 " + ctx);
			
		} catch(Exception e) {
			System.out.println("PEM2 Test succeeded");
			return;
		}
		assert(false);
	}
	
	public void testPEM3() {
		try {
			TransportContext ctx = new PEMTransportContext("", "files/combined.pem");
			System.out.println("PEM3 " + ctx);
			
		} catch(Exception e) {
			System.err.println("PEM3 Test failed: " + e);
			assert(false);
		}
	}
}
