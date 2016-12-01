package org.renci.ahab.libtransport.util;

import org.renci.ahab.libtransport.SSHAccessToken;
import org.renci.ahab.libtransport.util.SSHAccessTokenFileFactory;

import junit.framework.TestCase;

public class TestSSHAccessTokenFileFactory extends TestCase {

	public void testFactory1() {
		try {
			SSHAccessTokenFileFactory fac = new SSHAccessTokenFileFactory("files/id_dsa.pub", false);
			SSHAccessToken t = fac.getPopulatedToken();
			System.out.println(t);
		} catch(Exception e) {
			System.err.println(e);
			assert(false);
		}
	}
	
	/**
	public void testFactory2() {
		try {
			SSHAccessTokenFileFactory fac = new SSHAccessTokenFileFactory("~/.ssh/id_dsa.pub", false);
			SSHAccessToken t = fac.getPopulatedToken();
			System.out.println(t);
		} catch(Exception e) {
			System.err.println(e);
			assert(false);
		}
	}
	*/
}
