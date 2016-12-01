package org.renci.ahab.libtransport.xmlrpc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ws.commons.util.NamespaceContextImpl;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcController;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.parser.NullParser;
import org.apache.xmlrpc.parser.TypeParser;
import org.apache.xmlrpc.serializer.NullSerializer;
import org.renci.ahab.libtransport.IGENICHAPIv1;
import org.renci.ahab.libtransport.SSLTransportContext;
import org.renci.ahab.libtransport.util.ContextTransportException;

/**
 * GENI CH API support (SA and others). For more information see SA/MA documentation on groups.geni.net
 * @author ibaldin
 *
 */
public class GENICHXMLRPCProxy implements IGENICHAPIv1 {

	public static final String SSH_KEY_PRIVATE = "KEY_PRIVATE";
	public static final String SSH_KEY_PUBLIC = "KEY_PUBLIC";
	public static final String SSH_KEY_ID = "KEY_ID";
	public static final String SSH_KEY_MEMBER_GUID = "_GENI_KEY_MEMBER_UID";

	private static final String FED_VERSION = "2";

	private static boolean saVersionMatch = false, maVersionMatch = false;
	
	protected SSLTransportContext ctx;
	protected URL url;

	/**
	 * To deal with 'nil' returned by CH
	 * @author ibaldin
	 *
	 */
	public class MyTypeFactory extends TypeFactoryImpl {

		public MyTypeFactory(XmlRpcController pController) {
			super(pController);
		}

		@Override
		public TypeParser getParser(XmlRpcStreamConfig pConfig,
				NamespaceContextImpl pContext, String pURI, String pLocalName) {

			if ("".equals(pURI) && NullSerializer.NIL_TAG.equals(pLocalName)) {
				return new NullParser();
			} else {
				return super.getParser(pConfig, pContext, pURI, pLocalName);
			}
		}
	}

	public GENICHXMLRPCProxy(SSLTransportContext c, URL u) throws ContextTransportException {
		ctx = c;
		url = u;
		
		ctx.establishIdentity(url);
	}

	private XmlRpcClient setupClient(FedAgent a, URL url) throws MalformedURLException {

		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(url);
		XmlRpcClient client = new XmlRpcClient();
		config.setEnabledForExtensions(true);
		client.setConfig(config);
		client.setTypeFactory(new MyTypeFactory(client));

		// set this transport factory for host-specific SSLContexts to work
		XmlRpcCommonsTransportFactory f = new XmlRpcCommonsTransportFactory(client);
		client.setTransportFactory(f);

		return client;
	}

	/* (non-Javadoc)
	 * @see orca.ahab.libtransport.xmlrpc.IGENICHAPIv1#fedGetVersion(orca.ahab.libtransport.SSLTransportContext, orca.ahab.libtransport.xmlrpc.GENICHXMLRPCProxy.FedAgent, java.net.URL)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> fedGetVersion(FedAgent a) throws XMLRPCTransportException, ContextTransportException {

		Map<String, Object> rr = null;
		try {
			XmlRpcClient client = setupClient(a, url);

			// create slice on SA
			rr = (Map<String, Object>)client.execute(FedCall.get_version.name(), new Object[]{});

			checkAPIError(rr);

			return (Map<String, Object>)rr.get(FedField.value.name());
		} catch (XmlRpcException e) {
			e.printStackTrace();
			throw new XMLRPCTransportException("Unable to contact " + a + " " + url + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException("Unable to create slice on " + a + ":  " + url + " due to " + e);
		}
	}


	/* (non-Javadoc)
	 * @see orca.ahab.libtransport.xmlrpc.IGENICHAPIv1#fedCheckVersion(orca.ahab.libtransport.SSLTransportContext, orca.ahab.libtransport.xmlrpc.GENICHXMLRPCProxy.FedAgent, java.net.URL)
	 */
	@Override
	public void fedCheckVersion(FedAgent a) throws XMLRPCTransportException, ContextTransportException {
		try {
			Map<String, Object> fields = fedGetVersion(a);

			if (fields == null)
				throw new Exception(a + " returned invalid values");
			if ((fields.get(FedField.VERSION.name()) != null) && ((String)fields.get(FedField.VERSION.name())).startsWith(FED_VERSION))
				return;
			else
				throw new Exception(a + " version [" + fields.get(FedField.VERSION.name()) + "] is not compatible with this version of Flukes");
		} catch (Exception e) {
			throw new XMLRPCTransportException("Unable to communicate with " + a + " due to " + e);
		}
	}


	/* (non-Javadoc)
	 * @see orca.ahab.libtransport.xmlrpc.IGENICHAPIv1#saCreateSlice(orca.ahab.libtransport.SSLTransportContext, java.lang.String, java.lang.String, java.net.URL)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public String saCreateSlice(String name, String projectUrn) 
			throws XMLRPCTransportException, ContextTransportException {

		if ((name == null) || (projectUrn == null) ||
				(name.length() == 0) || (projectUrn.length() == 0))
			throw new XMLRPCTransportException("Invalid slice name or project urn: " + name + "/" + projectUrn);

		saCompatible();

		Map<String, Object> rr = null;
		try {
			XmlRpcClient client = setupClient(FedAgent.SA, url);

			// create slice on SA
			Map<String, Object> options = new HashMap<String, Object>();
			options.put(FedField.fields.name(), new HashMap<String, Object>());

			((Map<String, Object>)options.get(FedField.fields.name())).put(FedField.SLICE_NAME.name(), name);
			((Map<String, Object>)options.get(FedField.fields.name())).put(FedField.SLICE_PROJECT_URN.name(), projectUrn);

			rr = (Map<String, Object>)client.execute(FedCall.create.name(), new Object[]{FedObjectType.SLICE.name(), new Object[]{}, options});

			checkAPIError(rr);

			return (String)((Map<String, Object>)rr.get(FedField.value.name())).get(FedField.SLICE_URN.name());
		} catch (XmlRpcException e) {
			e.printStackTrace();
			throw new XMLRPCTransportException("Unable to contact SA " + url + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException("Unable to create slice on SA:  " + url + " due to " + e);
		}
	}


	/* (non-Javadoc)
	 * @see orca.ahab.libtransport.xmlrpc.IGENICHAPIv1#saUpdateSlice(orca.ahab.libtransport.SSLTransportContext, java.lang.String, orca.ahab.libtransport.xmlrpc.GENICHXMLRPCProxy.FedField, java.lang.Object, java.net.URL)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void saUpdateSlice(String sliceUrn, FedField field, Object val) 
			throws XMLRPCTransportException, ContextTransportException {
		if ((sliceUrn == null) || (sliceUrn.length() == 0))
			throw new XMLRPCTransportException("Invalid slice Urn: " + sliceUrn);

		saCompatible();

		Map<String, Object> rr = null;
		try {
			XmlRpcClient client = setupClient(FedAgent.SA, url);

			// create slice on SA
			Map<String, Object> options = new HashMap<String, Object>();
			options.put(FedField.fields.name(), new HashMap<String, Object>());

			((Map<String, Object>)options.get(FedField.fields.name())).put(field.name(), val);

			rr = (Map<String, Object>)client.execute(FedCall.update.name(), new Object[]{FedObjectType.SLICE.name(), sliceUrn, new Object[]{}, options});

			checkAPIError(rr);

		} catch (XmlRpcException e) {
			e.printStackTrace();
			throw new XMLRPCTransportException("Unable to contact SA " + url + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException("Unable to update " + field.name() + " of slice " + sliceUrn + " on SA:  " + url + " due to " + e);
		}
	}

	/* (non-Javadoc)
	 * @see orca.ahab.libtransport.xmlrpc.IGENICHAPIv1#saLookupSlice(orca.ahab.libtransport.SSLTransportContext, java.lang.String, orca.ahab.libtransport.xmlrpc.GENICHXMLRPCProxy.FedField[], java.net.URL)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> saLookupSlice(String sliceUrn, FedField[] fields) 
			throws XMLRPCTransportException, ContextTransportException {
		if ((sliceUrn == null) || (sliceUrn.length() == 0))
			throw new XMLRPCTransportException("Invalid slice Urn: " + sliceUrn);

		saCompatible();

		Map<String, Object> rr = null;
		try {
			XmlRpcClient client = setupClient(FedAgent.SA, url);

			// create slice on SA
			Map<String, Object> options = new HashMap<String, Object>();
			options.put(FedField.match.name(), new HashMap<String, Object>());

			((Map<String, Object>)options.get(FedField.match.name())).put(FedField.SLICE_URN.name(), new String[] {sliceUrn});

			if ((fields != null) && (fields.length > 0)) {
				String[] fieldNames = new String[fields.length];
				for(int i = 0; i < fields.length; i++)
					fieldNames[i] = fields[i].name();
				options.put(FedField.filter.name(), fieldNames);
			}

			rr = (Map<String, Object>)client.execute(FedCall.lookup.name(), new Object[]{FedObjectType.SLICE.name(), new Object[]{}, options});

			checkAPIError(rr);

			return (Map<String, Object>)rr.get(FedField.value.name());
		} catch (XmlRpcException e) {
			e.printStackTrace();
			throw new XMLRPCTransportException("Unable to contact SA " + url + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException("Unable to lookup slice " + sliceUrn + " on SA:  " + url + " due to " + e);
		}
	}

	/* (non-Javadoc)
	 * @see orca.ahab.libtransport.xmlrpc.IGENICHAPIv1#saLookupSlice(orca.ahab.libtransport.SSLTransportContext, java.lang.String, java.util.List, java.net.URL)
	 */
	@Override
	public Map<String, Object> saLookupSlice(String sliceUrn, List<FedField> fields) 
			throws XMLRPCTransportException, ContextTransportException {
		return saLookupSlice(sliceUrn, fields.toArray(new FedField[fields.size()]));
	}

	/* (non-Javadoc)
	 * @see orca.ahab.libtransport.xmlrpc.IGENICHAPIv1#maLookupAllSSHKeys(orca.ahab.libtransport.SSLTransportContext, java.lang.String, java.net.URL)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> maLookupAllSSHKeys(String userUrn) 
			throws XMLRPCTransportException, ContextTransportException {
		if ((userUrn == null) || (userUrn.length() == 0))
			throw new XMLRPCTransportException("Invalid user Urn: " + userUrn);

		maCompatible();

		Map<String, Object> rr = null;
		try {
			XmlRpcClient client = setupClient(FedAgent.MA, url);

			Map<String, Object> options = new HashMap<String, Object>();
			options.put(FedField.match.name(), new HashMap<String, Object>());

			((Map<String, Object>)options.get(FedField.match.name())).put(FedField.KEY_MEMBER.name(), new String[] {userUrn});

			rr = (Map<String, Object>)client.execute(FedCall.lookup.name(), new Object[]{FedObjectType.KEY.name(), new Object[]{}, options});

			checkAPIError(rr);

			return (Map<String, Object>)rr.get(FedField.value.name());
		} catch (XmlRpcException e) {
			e.printStackTrace();
			throw new XMLRPCTransportException("Unable to contact MA " + url + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException("Unable to lookup SSH keys for " + userUrn + " on MA:  " + url + " due to " + e);
		}
	}


	/* (non-Javadoc)
	 * @see orca.ahab.libtransport.xmlrpc.IGENICHAPIv1#maLookupLatestSSHKeys(orca.ahab.libtransport.SSLTransportContext, java.lang.String, java.net.URL)
	 */
	@Override
	public Map<String, Object> maLookupLatestSSHKeys(String userUrn) 
			throws XMLRPCTransportException, ContextTransportException {

		Map<String, Object> ret = maLookupAllSSHKeys(userUrn);

		List<String> keys = new ArrayList<String>(ret.keySet());
		java.util.Collections.sort(keys, new Comparator<String>() {

			public int compare(String o1, String o2) {
				try {
					// these are numbers
					return Integer.parseInt(o1) - Integer.parseInt(o2);
				} catch (NumberFormatException nfe) {
					// default to lexicographic comparison
					return o1.compareTo(o2);
				}
			}
		});
		if (keys.size() < 1) {
			throw new XMLRPCTransportException("No SSH keys available for user " + userUrn + " from MA " + url);
		}
		Map<String, Object> latestKeys = (Map<String, Object>)ret.get(keys.get(0));

		return latestKeys;
	}

	private void checkAPIError(Map<String, Object> r) throws Exception {
		if ((r.get(FedField.code.name()) != null) &&
				((Integer)r.get(FedField.code.name()) != 0)) 
			throw new Exception("FED API Error: [" + r.get(FedField.code.name()) + "]: " + r.get(FedField.output.name()));
	}

	/**
	 * Ensure we're compatible with this SA
	 * @throws Exception
	 */
	private synchronized void saCompatible() throws XMLRPCTransportException, ContextTransportException {
		if (!saVersionMatch) { 
			fedCheckVersion(FedAgent.SA);
			saVersionMatch = true;
		}
	}

	/**
	 * Ensure we're compatible with this SA
	 * @throws Exception
	 */
	private synchronized void maCompatible() throws XMLRPCTransportException, ContextTransportException {
		if (!maVersionMatch) { 
			fedCheckVersion(FedAgent.MA);
			maVersionMatch = true;
		}
	}
}
