package orca.ahab.libtransport;

import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import orca.ahab.libtransport.util.ContextTransportException;
import orca.util.ssl.ContextualSSLProtocolSocketFactory;
import orca.util.ssl.MultiKeyManager;
import orca.util.ssl.MultiKeySSLContextFactory;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

public class SSLTransportContext extends TransportContext {
	protected final String keyAlias;
	protected final String keyPassword;
	protected KeyStore ks;

	private static final int HTTPS_PORT = 443;
	private static MultiKeyManager mkm = null;
	private static ContextualSSLProtocolSocketFactory regSslFact = null;
	Collection<List<?>> altNames = null;

	static {
		mkm = new MultiKeyManager();
		regSslFact = new ContextualSSLProtocolSocketFactory();

		// register the protocol (Note: All xmlrpc clients must use XmlRpcCommonsTransportFactory
		// for this to work). See ContextualSSLProtocolSocketFactory.
		Protocol reghhttps = new Protocol("https", (ProtocolSocketFactory)regSslFact, HTTPS_PORT); 
		Protocol.registerProtocol("https", reghhttps);
	}

	TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					// return 0 size array, not null, per spec
					return new X509Certificate[0];
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
					// Trust always
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateExpiredException, CertificateNotYetValidException {
					// Trust always, unless expired
					// FIXME: should check the cert of controller we're talking to
					for(X509Certificate c: certs) {
						c.checkValidity();	
					}
				}
			}
	};

	public SSLTransportContext(String a, String p) {
		keyAlias = a;
		keyPassword = p;
	}

	public String getCurrentAlias() {
		return keyAlias;
	}

	/**
	 * In multi-threaded applications this should be called to be sure a thread owns the established identity.
	 */
	public void setThreadCurrentAlias() {
		mkm.setCurrentGuid(keyAlias);
	}


	@Override
	protected void _establishIdentity(URL ctrlrUrl) throws ContextTransportException {
		try {
			String realKeyAlias = keyAlias;
			// check that the spelling of key alias is proper
			Enumeration<String> as = ks.aliases();
			while (as.hasMoreElements()) {
				String a = as.nextElement();
				if (keyAlias.toLowerCase().equals(a.toLowerCase())) {
					realKeyAlias = a;
					break;
				}
			}

			// alias has to exist and have a key and cert present
			if (!ks.containsAlias(realKeyAlias)) {
				throw new ContextTransportException("Alias " + realKeyAlias + " does not exist in keystore");
			}

			if (ks.getKey(realKeyAlias, keyPassword.toCharArray()) == null)
				throw new Exception("Key with alias " + realKeyAlias + " does not exist in keystore");

			if (ks.getCertificate(realKeyAlias) == null) {
				throw new Exception("Certificate with alias " + realKeyAlias + " does not exist in keystore");
			}

			if (ks.getCertificate(realKeyAlias).getType().equals("X.509")) {
				X509Certificate x509Cert = (X509Certificate)ks.getCertificate(realKeyAlias);
				altNames = x509Cert.getSubjectAlternativeNames();
				try {
					x509Cert.checkValidity();
				} catch (Exception e) {
					throw new Exception("Certificate with alias " + realKeyAlias + " is not yet valid or has expired.");
				}
			}

			// add the identity into it
			mkm.addPrivateKey(realKeyAlias, 
					(PrivateKey)ks.getKey(realKeyAlias, keyPassword.toCharArray()), 
					ks.getCertificateChain(realKeyAlias));

			// before we do SSL to this controller, set our identity
			mkm.setCurrentGuid(realKeyAlias);

			// add this multikey context factory for the controller host/port
			int port = ctrlrUrl.getPort();
			if (port <= 0)
				port = HTTPS_PORT;
			regSslFact.addHostContextFactory(new MultiKeySSLContextFactory(mkm, trustAllCerts), 
					ctrlrUrl.getHost(), port);

			setIdentity();
		} catch (Exception e) {
			throw new ContextTransportException(e.getMessage());
		}
	}

	/**
	 * Get all alt names contained in a cert (only invocable after SSL identity is set)
	 * @return
	 * @throws Exception
	 */
	public Collection<List<?>> getAltNames() throws ContextTransportException {
		if (identityIsSet()) 
			return altNames;
		else
			throw new ContextTransportException("SSL Identity is not set, alternative names are not known");
	}	

	/**
	 * Get the GENI URN in a cert, if available (only invocable after SSL identity is set)
	 * @return - the URN that matches "urn:publicid:IDN.+\\+user\\+.+" or null
	 * @throws Exception
	 */
	public String getAltNameUrn() throws ContextTransportException {
		Collection<List<?>> altNames = getAltNames();

		String urn = null;
		Iterator <List<?>> it = altNames.iterator();
		while(it.hasNext()) {
			List<?> altName = it.next();
			if ((Integer)altName.get(0) != 6)
				continue;
			Pattern pat = Pattern.compile("urn:publicid:IDN.+\\+user\\+.+");
			Matcher mat = pat.matcher((String)altName.get(1));
			if (mat.matches())  {
				urn = (String)altName.get(1);
			}
		}
		return urn;
	}
}
