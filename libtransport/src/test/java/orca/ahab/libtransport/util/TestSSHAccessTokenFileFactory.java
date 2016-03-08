package orca.ahab.libtransport.util;

import junit.framework.TestCase;
import orca.ahab.libtransport.SSHAccessToken;

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
