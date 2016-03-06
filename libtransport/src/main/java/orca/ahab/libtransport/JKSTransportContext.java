package orca.ahab.libtransport;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import orca.ahab.libtransport.util.ContextTransportException;
import orca.ahab.libtransport.util.StaticUtil;

/**
 * Context that relies on a JKS for authentication
 * @author ibaldin
 *
 */
public class JKSTransportContext extends SSLTransportContext {
	final File keyStorePath;
	
	public JKSTransportContext(String alias, String pass, String ksp) throws ContextTransportException {
		super(alias, pass);
		try {
			keyStorePath = StaticUtil.getUserFileName(ksp);
			if (keyStorePath.exists()) {
				// load keystore and get the right cert from it
				FileInputStream jksIS = new FileInputStream(keyStorePath);
				ks = loadJKSData(jksIS, keyAlias, keyPassword);
				jksIS.close();
			}
		} catch (Exception e) {
			throw new ContextTransportException(e.getMessage());
		}
	}
		
	protected KeyStore loadJKSData(FileInputStream jksIS, String keyAlias, String keyPassword)
			throws Exception {

		KeyStore ks = KeyStore.getInstance("jks");
		ks.load(jksIS, keyPassword.toCharArray());

		return ks;
	}
}
