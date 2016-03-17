package orca.ahab.libtransport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PrivilegedAction;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.ArrayList;

import orca.ahab.libtransport.util.ContextTransportException;
import orca.ahab.libtransport.util.StaticUtil;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;

/**
 * Context that relies on one a PEM cert file and a keyfile for authentication
 * @author ibaldin
 *
 */
public class PEMTransportContext extends SSLTransportContext {

	private static final String DEFAULT_X509_ALIAS = "x509convert";

	private void initKS(String certP, String keyP) throws ContextTransportException {
		try {
			File certFilePath = StaticUtil.getUserFileName(certP);
			File certKeyFilePath = StaticUtil.getUserFileName(keyP);
			if (certFilePath.exists() && certKeyFilePath.exists()) {
				FileInputStream certIS = new FileInputStream(certFilePath);
				FileInputStream keyIS = new FileInputStream(certKeyFilePath);
				ks = loadX509Data(certIS, keyIS, keyAlias, keyPassword);
				certIS.close();
				keyIS.close();
			} else {
				throw new ContextTransportException("PEMTransportContext unable to find cert or key file " + certP + " " + keyP);
			}
		} catch(Exception e) {
			throw new ContextTransportException(e.getMessage());
		}
	}
	
	/**
	 * Password and path to cert pem file and key file
	 * @param pass password
	 * @param certP path to certificate
	 * @param keyP path to key file
	 * @throws ContextTransportException
	 */
	public PEMTransportContext(String pass, String certP, String keyP) throws ContextTransportException {
		super(DEFAULT_X509_ALIAS, pass);
		
		initKS(certP, keyP);
	}

	/**
	 * Password and path to unified pem file
	 * @param pass
	 * @param certP
	 * @throws ContextTransportException
	 */
	public PEMTransportContext(String pass, String certP) throws ContextTransportException {
		super(DEFAULT_X509_ALIAS, pass);
		
		initKS(certP, certP);
	}
	
	/**
	 * Load PEM file data using bouncycastle
	 * @param certIS
	 * @param keyIS
	 * @param keyAlias
	 * @param keyPassword
	 * @return
	 * @throws Exception
	 */
	protected KeyStore loadX509Data(FileInputStream certIS, FileInputStream keyIS, String keyAlias,
			String keyPassword) throws Exception {

		AccessController.doPrivileged(new PrivilegedAction<Void>() {
			public Void run() {
				if (Security.getProvider("BC") == null) {
					Security.addProvider(new BouncyCastleProvider());
				}
				return null;
			}
		});

		JcaPEMKeyConverter keyConverter =
				new JcaPEMKeyConverter().setProvider("BC");
		JcaX509CertificateConverter certConverter =
				new JcaX509CertificateConverter().setProvider("BC");

		Object object;

		PEMParser pemParser = new PEMParser(new BufferedReader(new InputStreamReader(keyIS, "UTF-8")));

		PrivateKey privKey = null;

		while ((object = pemParser.readObject()) != null) {
			if (object instanceof PrivateKeyInfo) {
				PrivateKeyInfo pki = (PrivateKeyInfo)object;
				privKey = keyConverter.getPrivateKey(pki);
				break;
			}
			if (object instanceof PKCS8EncryptedPrivateKeyInfo) {
				InputDecryptorProvider decProv =
						new JceOpenSSLPKCS8DecryptorProviderBuilder().build(keyPassword.toCharArray());
				privKey =
						keyConverter.getPrivateKey(((PKCS8EncryptedPrivateKeyInfo) object).decryptPrivateKeyInfo(decProv));
				break;
			}
			else if (object instanceof PEMEncryptedKeyPair) {
				PEMDecryptorProvider decProv =
						new JcePEMDecryptorProviderBuilder().build(keyPassword.toCharArray());
				privKey =
						keyConverter.getPrivateKey((((PEMEncryptedKeyPair) object).decryptKeyPair(decProv)).getPrivateKeyInfo());
				break;
			}
			else if (object instanceof PEMKeyPair) {
				privKey =
						keyConverter.getPrivateKey(((PEMKeyPair) object).getPrivateKeyInfo());
				break;
			}
		}

		if (privKey == null) 
			throw new Exception("Private key file did not contain a private key.");

		pemParser = new PEMParser(new BufferedReader(new InputStreamReader(certIS, "UTF-8")));

		ArrayList<Certificate> certs = new ArrayList<Certificate>();

		while ((object = pemParser.readObject()) != null) {
			if (object instanceof X509CertificateHolder) {
				certs.add(certConverter.getCertificate((X509CertificateHolder) object));
			}
		}

		if (certs.isEmpty())
			throw new Exception("Certificate file contained no certificates.");

		KeyStore ks = KeyStore.getInstance("jks");
		ks.load(null);
		ks.setKeyEntry(keyAlias, privKey,
				keyPassword.toCharArray(), certs.toArray(new Certificate[certs.size()]));

		return ks;
	}

}
