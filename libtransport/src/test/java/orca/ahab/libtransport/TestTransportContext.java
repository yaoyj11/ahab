package orca.ahab.libtransport;

import java.net.URL;

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
			System.err.println("PEM Test failed: " + e);
			assert(false);
		}
	}
	
	public void testPEM2() {
		try {
			TransportContext ctx = new PEMTransportContext("", "files/selfest.crt", "files/selftest.key");
			System.out.println("PEM2 " + ctx);
			
		} catch(Exception e) {
			System.out.println("PEM2 Test succeeded");
			return;
		}
		assert(false);
	}
}
