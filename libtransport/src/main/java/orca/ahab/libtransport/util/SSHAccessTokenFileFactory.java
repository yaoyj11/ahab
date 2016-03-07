package orca.ahab.libtransport.util;

import java.io.File;
import java.util.Collections;
import java.util.Properties;

import orca.ahab.libtransport.SSHAccessToken;

/**
 * Populates an SSH token bean from a file containing SSH key
 * @author ibaldin
 *
 */
public class SSHAccessTokenFileFactory implements ISSHAccessTokenFactory<SSHAccessToken> {
	SSHAccessToken token;
	private static final String SSH_DSA_PUBKEY_FILE = "id_dsa.pub";
	private static final String SSH_RSA_PUBKEY_FILE = "id_rsa.pub";
	
	/**
	 * Read specific public SSH key file
	 * @param keyPathStr
	 * @param sudo
	 * @throws UtilTransportException
	 */
	public SSHAccessTokenFileFactory(String keyPathStr, boolean sudo) throws UtilTransportException {
		File keyPath = null;
		
		if (keyPathStr.startsWith("~/")) {
			keyPathStr = keyPathStr.replaceAll("~/", "/");
			keyPath = new File(System.getProperty("user.home"), keyPathStr);
		}
		else {
			keyPath = new File(keyPathStr);
		}

		String userKey = StaticUtil.readTextFile(keyPath);

		if (userKey == null) 
			throw new UtilTransportException("Unable to load user public ssh key " + keyPath);
		
		token = new SSHAccessToken(Collections.singletonList(userKey), sudo);
	}
	
	/**
	 * Try to load one of public SSH key files in home directory, DSA, then RSA
	 * @param sudo
	 * @throws UtilTransportException
	 */
	public SSHAccessTokenFileFactory(boolean sudo) throws UtilTransportException {
		token = new SSHAccessToken(Collections.singletonList(getAnyUserPubKey()), sudo);
	}
	
	@Override
	public SSHAccessToken getPopulatedToken() {
		return token;
	}
	
	private String getAnyUserPubKey() throws UtilTransportException {
		Properties p = System.getProperties();

		String keyFilePathStr = "" + p.getProperty("user.home") + p.getProperty("file.separator") + ".ssh" +
		p.getProperty("file.separator") + SSH_DSA_PUBKEY_FILE;
		File keyFilePath = new File(keyFilePathStr);

		String userKey = StaticUtil.readTextFile(keyFilePath);
		if (userKey == null) {
			keyFilePathStr = "" + p.getProperty("user.home") + p.getProperty("file.separator") + ".ssh" + 
			p.getProperty("file.separator") + SSH_RSA_PUBKEY_FILE;
			keyFilePath = new File(keyFilePathStr);
			userKey = StaticUtil.readTextFile(keyFilePath);
			if (userKey == null) {
				throw new UtilTransportException ("Unable to locate ssh public keys, you will not be able to login to the resources!");
			}
		}
		return userKey;
	}
}
