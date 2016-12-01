package org.renci.ahab.libtransport;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.renci.ahab.libtransport.SSHAccessToken;
import org.renci.ahab.libtransport.SliceAccessContext;
import org.renci.ahab.libtransport.util.SSHAccessTokenFileFactory;
import org.renci.ahab.libtransport.xmlrpc.XMLRPCAPIAdapter;

import junit.framework.TestCase;

public class TestSliceContext extends TestCase {

	public void testAT() {
		SSHAccessToken at = new SSHAccessToken(Collections.singletonList("testkey"), true);
		System.out.println(at);
	}

	public void testSliceContext1() {
		try {
			SliceAccessContext<SSHAccessToken> sctx = new SliceAccessContext<>();

			SSHAccessTokenFileFactory fac = new SSHAccessTokenFileFactory("files/id_dsa.pub", false);
			SSHAccessToken t = fac.getPopulatedToken();

			fac = new SSHAccessTokenFileFactory("files/id_rsa1.pub", false);
			SSHAccessToken t1 = fac.getPopulatedToken();
			
			sctx.addToken("testuser", "some", t);
			sctx.addToken("testuser", "some1", t1);
			
			sctx.addToken("testuser", t);
			
			System.out.println(sctx);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("SliceContext1 failed: " + e);
			assert(false);
		}
	}
	
	public void testSliceContext2() {
		try {
			SliceAccessContext<SSHAccessToken> sctx = new SliceAccessContext<>();

			SSHAccessTokenFileFactory fac = new SSHAccessTokenFileFactory("files/id_dsa.pub", false);
			SSHAccessToken t = fac.getPopulatedToken();

			fac = new SSHAccessTokenFileFactory("files/id_rsa1.pub", false);
			SSHAccessToken t1 = fac.getPopulatedToken();
			
			sctx.addToken("testuser", "some", t);
			sctx.addToken("testuser", "some1", t1);
			
			sctx.addToken("testuser", t);
			
			Object o = sctx.getTokens(new XMLRPCAPIAdapter());
			
			List<Map<String, Object>> finalTokens = (List<Map<String, Object>>)o;
			
			System.out.println("\n\nFINAL MAP");
			for(Map<String, Object> e: finalTokens) {
				for(Map.Entry<String, Object> e1: e.entrySet()) {
					System.out.println(e1.getKey() + " " + e1.getValue());
				}
				System.out.println("\n");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("SliceContext1 failed: " + e);
			assert(false);
		}
	}
}
