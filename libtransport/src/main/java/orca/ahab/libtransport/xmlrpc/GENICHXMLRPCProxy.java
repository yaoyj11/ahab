package orca.ahab.libtransport.xmlrpc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import orca.ahab.libtransport.SSLTransportContext;
import orca.ahab.libtransport.util.ContextTransportException;

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

/**
 * GENI CH API support (SA and others). For more information see SA/MA documentation on groups.geni.net
 * @author ibaldin
 *
 */
public class GENICHXMLRPCProxy {

	public static final String SSH_KEY_PRIVATE = "KEY_PRIVATE";
	public static final String SSH_KEY_PUBLIC = "KEY_PUBLIC";
	public static final String SSH_KEY_ID = "KEY_ID";
	public static final String SSH_KEY_MEMBER_GUID = "_GENI_KEY_MEMBER_UID";

	private static final String FED_VERSION = "2";

	private static boolean saVersionMatch = false, maVersionMatch = false;

	public enum FedField {
		VERSION, 
		code, 
		value,
		output,
		// used in get_version return
		FIELDS, 
		// used in create/lookup/update
		fields, 
		match,
		filter,
		SLICE_NAME, 
		SLICE_PROJECT_URN,
		SLICE_URN,
		SLICE_EXPIRATION,
		KEY_MEMBER;
	}

	public enum FedAgent {
		SA, MA
	}

	public enum FedCall {
		get_version, create, 
		lookup, update;
	}

	public enum FedObjectType {
		SLICE, PROJECT, SLIVER, KEY;
	}

	private static GENICHXMLRPCProxy instance = new GENICHXMLRPCProxy();

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

	GENICHXMLRPCProxy() {

	}

	public static GENICHXMLRPCProxy getInstance() {
		return instance;
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

	/**
	 * Get version of the MA or SA
	 * @param ctx
	 * @param a
	 * @param url
	 * @return
	 * @throws XMLRPCTransportException
	 * @throws ContextTransportException
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> fedGetVersion(SSLTransportContext ctx, FedAgent a, URL url) throws XMLRPCTransportException, ContextTransportException {
		ctx.establishIdentity(url);

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


	/**
	 * Check we are talking to SA of correct version
	 * @param ctx
	 * @param a
	 * @param url
	 * @throws XMLRPCTransportException
	 * @throws ContextTransportException
	 */
	public void fedCheckVersion(SSLTransportContext ctx, FedAgent a, URL url) throws XMLRPCTransportException, ContextTransportException {
		try {
			Map<String, Object> fields = fedGetVersion(ctx, a, url);

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


	/**
	 * Create slice on SA using URN and project id
	 * @param ctx
	 * @param name
	 * @param projectUrn
	 * @param url
	 * @return
	 * @throws XMLRPCTransportException
	 * @throws ContextTransportException
	 */
	@SuppressWarnings("unchecked")
	public String saCreateSlice(SSLTransportContext ctx, String name, String projectUrn, URL url) 
			throws XMLRPCTransportException, ContextTransportException {

		if ((name == null) || (projectUrn == null) ||
				(name.length() == 0) || (projectUrn.length() == 0))
			throw new XMLRPCTransportException("Invalid slice name or project urn: " + name + "/" + projectUrn);

		ctx.establishIdentity(url);

		saCompatible(ctx, url);

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


	/**
	 * Update slice field to specific value
	 * @param ctx
	 * @param sliceUrn
	 * @param field
	 * @param val
	 * @param url
	 * @throws XMLRPCTransportException
	 * @throws ContextTransportException
	 */
	@SuppressWarnings("unchecked")
	public void saUpdateSlice(SSLTransportContext ctx, String sliceUrn, FedField field, Object val, URL url) 
			throws XMLRPCTransportException, ContextTransportException {
		if ((sliceUrn == null) || (sliceUrn.length() == 0))
			throw new XMLRPCTransportException("Invalid slice Urn: " + sliceUrn);

		ctx.establishIdentity(url);

		saCompatible(ctx, url);

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

	/**
	 * Lookup a slice on SA
	 * @param ctx
	 * @param sliceUrn
	 * @param fields - filter
	 * @param url
	 * @return
	 * @throws XMLRPCTransportException
	 * @throws ContextTransportException
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> saLookupSlice(SSLTransportContext ctx, String sliceUrn, FedField[] fields, URL url) 
			throws XMLRPCTransportException, ContextTransportException {
		if ((sliceUrn == null) || (sliceUrn.length() == 0))
			throw new XMLRPCTransportException("Invalid slice Urn: " + sliceUrn);

		ctx.establishIdentity(url);

		saCompatible(ctx, url);

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

	/**
	 * Lookup either a list of fields or all fields in a slice
	 * @param ctx
	 * @param sliceUrn
	 * @param fields
	 * @param url
	 * @return
	 * @throws XMLRPCTransportException
	 * @throws ContextTransportException
	 */
	public Map<String, Object> saLookupSlice(SSLTransportContext ctx, String sliceUrn, List<FedField> fields, URL url) 
			throws XMLRPCTransportException, ContextTransportException {
		return saLookupSlice(ctx, sliceUrn, fields.toArray(new FedField[fields.size()]), url);
	}

	/**
	 * MA Call to get all public and private SSH keys of a user
	 * @param ctx
	 * @param userUrn
	 * @param url
	 * @return
	 * @throws XMLRPCTransportException
	 * @throws ContextTransportException
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> maLookupAllSSHKeys(SSLTransportContext ctx, String userUrn, URL url) 
			throws XMLRPCTransportException, ContextTransportException {
		if ((userUrn == null) || (userUrn.length() == 0))
			throw new XMLRPCTransportException("Invalid user Urn: " + userUrn);

		ctx.establishIdentity(url);

		maCompatible(ctx, url);

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


	/**
	 * Get the latest pair of SSH keys from MA
	 * @param ctx
	 * @param userUrn
	 * @param url
	 * @return
	 * @throws XMLRPCTransportException
	 * @throws ContextTransportException
	 */
	public Map<String, Object> maLookupLatestSSHKeys(SSLTransportContext ctx, String userUrn, URL url) 
			throws XMLRPCTransportException, ContextTransportException {

		Map<String, Object> ret = maLookupAllSSHKeys(ctx, userUrn, url);

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
	private synchronized void saCompatible(SSLTransportContext ctx, URL url) throws XMLRPCTransportException, ContextTransportException {
		if (!saVersionMatch) { 
			fedCheckVersion(ctx, FedAgent.SA, url);
			saVersionMatch = true;
		}
	}

	/**
	 * Ensure we're compatible with this SA
	 * @throws Exception
	 */
	private synchronized void maCompatible(SSLTransportContext ctx, URL url) throws XMLRPCTransportException, ContextTransportException {
		if (!maVersionMatch) { 
			fedCheckVersion(ctx, FedAgent.MA, url);
			maVersionMatch = true;
		}
	}
}
