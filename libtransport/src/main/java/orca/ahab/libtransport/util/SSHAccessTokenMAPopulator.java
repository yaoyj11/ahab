package orca.ahab.libtransport.util;

import orca.ahab.libtransport.SSHAccessToken;

/**
 * Populates SSH token from member authority
 * @author ibaldin
 *
 */
public class SSHAccessTokenMAPopulator implements ISSHAccessTokenFactory<SSHAccessToken> {

	@Override
	public SSHAccessToken getPopulatedToken() {
		
	/*
		if ((GUI.getInstance().getPreference(GUI.PrefsEnum.ENABLE_GENIMA).equalsIgnoreCase("true") ||
				GUI.getInstance().getPreference(GUI.PrefsEnum.ENABLE_GENIMA).equalsIgnoreCase("yes")) &&
				("portal".equals(GUI.getInstance().getPreference(GUI.PrefsEnum.SSH_KEY_SOURCE)))) {
			// get our urn based on the established cert
			String urn = getAltNameUrn();
			if (urn == null) {
				throw new Exception("Unable to obtain user GENI URN from the certificate file, cannot query GENI portal for the SSH keys, please change the '" + 
						GUI.PrefsEnum.SSH_KEY_SOURCE.name() + "' property to  'file'");
			}

			GUI.logger().info("Querying CH MA for SSH keys");
			GENICHXMLRPCProxy chp = GENICHXMLRPCProxy.getInstance();
			// this is a map that has a bunch of entries, including KEY_PRIVATE and KEY_PUBLIC
			// Map looks like this: SSH Keys: {_GENI_KEY_MEMBER_UID=c7578309-b14c-4b4a-b555-1eff10f1b092, 
			// _GENI_KEY_FILENAME=id_dsa.pub, 
			// KEY_PUBLIC=ssh-dss <key material>= user@hostname, 
			// KEY_TYPE=<key material> or null, KEY_DESCRIPTION=Kobol, KEY_MEMBER=urn:publicid:IDN+ch.geni.net+user+ibaldin, KEY_PRIVATE=null, KEY_ID=13}
			// we should not use it unless both entries are present
			Map<String, Object> keys = chp.maLookupLatestSSHKeys(urn);

			if ((keys == null) || 
					(keys.get(GENICHXMLRPCProxy.SSH_KEY_PUBLIC) == null)) {
				throw new Exception("Unable to obtain public SSH key from the portal for user " + urn + ", please change the '" +
						GUI.PrefsEnum.SSH_KEY_SOURCE.name() + "' property to  'file'");
			}
			GUI.logger().info("Using public SSH key obtained from the portal");
			userKey = (String)keys.get(GENICHXMLRPCProxy.SSH_KEY_PUBLIC);

			// if private key is available, save it and change the ssh.key preference property to point to it
			if (keys.get(GENICHXMLRPCProxy.SSH_KEY_PRIVATE) != null) {
				String portalPrivateKey = (String)keys.get(GENICHXMLRPCProxy.SSH_KEY_PRIVATE);
				String keyFileName = "portal-" + (String)keys.get(GENICHXMLRPCProxy.SSH_KEY_ID) + "-key";
				File portalKeyFile = File.createTempFile(keyFileName, "");
				GUI.logger().info("Saving private key from the portal to " + portalKeyFile.getAbsolutePath());
				portalKeyFile.deleteOnExit();
				PrintWriter out = new PrintWriter(portalKeyFile.getAbsolutePath());
				GUI.getInstance().setPreference(GUI.PrefsEnum.SSH_KEY, portalKeyFile.getAbsolutePath());
				out.println(portalPrivateKey);
				out.close();
			}
			*/
		return null;
	}

	
}
