package orca.ahab.libtransport.xmlrpc;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import orca.ahab.libtransport.AccessToken;
import orca.ahab.libtransport.ISliceTransportAPI;
import orca.ahab.libtransport.SSHAccessToken;
import orca.ahab.libtransport.SSLTransportContext;
import orca.ahab.libtransport.SliceAccessContext;
import orca.ahab.libtransport.TransportContext;
import orca.ahab.libtransport.util.ContextTransportException;
import orca.ahab.libtransport.util.TransportException;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

public class OrcaSMXMLRPCProxy implements ISliceTransportAPI {
	private static final String RET_RET_FIELD = "ret";
	private static final String MSG_RET_FIELD = "msg";
	private static final String ERR_RET_FIELD = "err";
	private static final String GET_VERSION = "orca.getVersion";
	private static final String SLICE_STATUS = "orca.sliceStatus";
	private static final String CREATE_SLICE = "orca.createSlice";
	private static final String DELETE_SLICE = "orca.deleteSlice";
	private static final String MODIFY_SLICE = "orca.modifySlice";
	private static final String RENEW_SLICE = "orca.renewSlice";
	private static final String LIST_SLICES = "orca.listSlices";
	private static final String LIST_RESOURCES = "orca.listResources";
	private static final String GET_SLIVER_PROPERTIES = "orca.getSliverProperties";
	private static final String GET_RESERVATION_STATES = "orca.getReservationStates";

	public OrcaSMXMLRPCProxy() {
		;
	}

	public Map<String, Object> getVersion(TransportContext ctx, URL cUrl) throws TransportException, ContextTransportException {
		return _getVersion((SSLTransportContext)ctx, cUrl);
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> _getVersion(SSLTransportContext ctx, URL cUrl) throws XMLRPCTransportException, ContextTransportException {
		Map<String, Object> versionMap = null;
		ctx.establishIdentity(cUrl);

		try {
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(cUrl);
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);

			// set this transport factory for host-specific SSLContexts to work
			XmlRpcCommonsTransportFactory f = new XmlRpcCommonsTransportFactory(client);
			client.setTransportFactory(f);

			// get verbose list of the AMs
			versionMap = (Map<String, Object>)client.execute(GET_VERSION, new Object[]{});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);
		}
		return versionMap;
	}

	@SuppressWarnings("unchecked")
	public String createSlice(TransportContext ctx, String sliceId, String resReq, SliceAccessContext<? extends AccessToken> sliceCtx, URL cUrl) 
			throws TransportException, ContextTransportException {
		return _createSlice((SSLTransportContext)ctx, sliceId, resReq, (SliceAccessContext<SSHAccessToken>)sliceCtx, cUrl);
	}
	
	@SuppressWarnings("unchecked")
	private String _createSlice(SSLTransportContext ctx, String sliceId, String resReq, SliceAccessContext<SSHAccessToken> sliceCtx, URL cUrl) 
			throws XMLRPCTransportException, ContextTransportException {
		assert(sliceId != null);
		assert(resReq != null);

		String result = null;
		ctx.establishIdentity(cUrl);

		Map<String, Object> rr = null;
		try {
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(cUrl);
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);

			// set this transport factory for host-specific SSLContexts to work
			XmlRpcCommonsTransportFactory f = new XmlRpcCommonsTransportFactory(client);
			client.setTransportFactory(f);

			List<Map<String, ?>> users = (List<Map<String, ?>>)sliceCtx.getTokens(new XMLRPCAPIAdapter());
			// create sliver
			rr = (Map<String, Object>)client.execute(CREATE_SLICE, new Object[]{ sliceId, new Object[]{}, resReq, users});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl + " due to " + e);
		} catch (Exception e) {
			return "Unable to submit slice to SM:  " + cUrl + " due to " + e;
		}

		if (rr == null)
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to create slice: " + (String)rr.get(MSG_RET_FIELD));

		result = (String)rr.get(RET_RET_FIELD);
		return result;
	}

	public Boolean renewSlice(TransportContext ctx, String sliceId, Date newDate, URL cUrl) 
			throws TransportException, ContextTransportException {
		return _renewSlice((SSLTransportContext)ctx, sliceId, newDate, cUrl);
	}
	
	@SuppressWarnings("unchecked")
	private Boolean _renewSlice(SSLTransportContext ctx, String sliceId, Date newDate, URL cUrl) 
			throws XMLRPCTransportException, ContextTransportException {
		assert(sliceId != null);

		Boolean result = false;
		ctx.establishIdentity(cUrl);

		Map<String, Object> rr = null;
		try {
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(cUrl);
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);

			// set this transport factory for host-specific SSLContexts to work
			XmlRpcCommonsTransportFactory f = new XmlRpcCommonsTransportFactory(client);
			client.setTransportFactory(f);

			// create sliver
			Calendar ecal = Calendar.getInstance();
			ecal.setTime(newDate);
			String endDateString = DatatypeConverter.printDateTime(ecal); // RFC3339/ISO8601
			rr = (Map<String, Object>)client.execute(RENEW_SLICE, new Object[]{ sliceId, new Object[]{}, endDateString});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to renew slice: " + (String)rr.get(MSG_RET_FIELD));

		result = (Boolean)rr.get(RET_RET_FIELD);
		return result;
	}

	public boolean deleteSlice(TransportContext ctx, String sliceId, URL cUrl)  
			throws TransportException, ContextTransportException {
		return _deleteSlice((SSLTransportContext)ctx, sliceId, cUrl);
	}
	
	@SuppressWarnings("unchecked")
	private boolean _deleteSlice(SSLTransportContext ctx, String sliceId, URL cUrl)  
			throws XMLRPCTransportException, ContextTransportException {
		boolean res = false;
		ctx.establishIdentity(cUrl);

		Map<String, Object> rr = null;
		try {
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(cUrl);
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);

			// set this transport factory for host-specific SSLContexts to work
			XmlRpcCommonsTransportFactory f = new XmlRpcCommonsTransportFactory(client);
			client.setTransportFactory(f);

			// delete sliver
			rr = (Map<String, Object>)client.execute(DELETE_SLICE, new Object[]{ sliceId, new Object[]{}});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to delete slice: " + (String)rr.get(MSG_RET_FIELD));
		else
			res = (Boolean)rr.get(RET_RET_FIELD);

		return res;
	}


	public String sliceStatus(TransportContext ctx, String sliceId, URL cUrl)  throws TransportException, ContextTransportException {
		return _sliceStatus((SSLTransportContext)ctx, sliceId, cUrl);
	}
	
	@SuppressWarnings("unchecked")
	private String _sliceStatus(SSLTransportContext ctx, String sliceId, URL cUrl)  throws XMLRPCTransportException, ContextTransportException {
		assert(sliceId != null);

		String result = null;
		ctx.establishIdentity(cUrl);

		Map<String, Object> rr = null;
		try {
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(cUrl);
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);

			// set this transport factory for host-specific SSLContexts to work
			XmlRpcCommonsTransportFactory f = new XmlRpcCommonsTransportFactory(client);
			client.setTransportFactory(f);

			// sliver status
			rr = (Map<String, Object>)client.execute(SLICE_STATUS, new Object[]{ sliceId, new Object[]{}});

		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to get slice status: " + rr.get(MSG_RET_FIELD));

		result = (String)rr.get(RET_RET_FIELD);

		return result;
	}

	public Map<String, Map<String, String>> getReservationStates(TransportContext ctx, String sliceId, List<String> reservationIds, URL cUrl)  
			throws TransportException, ContextTransportException {
		return _getReservationStates((SSLTransportContext)ctx, sliceId, reservationIds, cUrl);
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Map<String, String>> _getReservationStates(SSLTransportContext ctx, String sliceId, List<String> reservationIds, URL cUrl)  
			throws XMLRPCTransportException, ContextTransportException {
		assert((sliceId != null) && (reservationIds != null));

		ctx.establishIdentity(cUrl);

		Map<String, Object> rr = null;
		try {
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(cUrl);
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);
			// set this transport factory for host-specific SSLContexts to work
			XmlRpcCommonsTransportFactory f = new XmlRpcCommonsTransportFactory(client);
			client.setTransportFactory(f);

			// sliver status
			rr = (Map<String, Object>)client.execute(GET_RESERVATION_STATES, new Object[]{ sliceId, reservationIds, new Object[]{}});

		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to get reservation states: " + rr.get(MSG_RET_FIELD));

		return (Map<String, Map<String, String>>) rr.get(RET_RET_FIELD);
	}

	public List<Map<String, String>> getSliverProperties(TransportContext ctx, String sliceId, String reservationId, URL cUrl) 
			throws TransportException, ContextTransportException {
		return _getSliverProperties((SSLTransportContext)ctx, sliceId, reservationId, cUrl);
	}
	
	@SuppressWarnings("unchecked")
	private List<Map<String, String>> _getSliverProperties(SSLTransportContext ctx, String sliceId, String reservationId, URL cUrl) 
			throws XMLRPCTransportException, ContextTransportException {
		assert((sliceId != null) && (reservationId != null));

		ctx.establishIdentity(cUrl);

		Map<String, Object> rr = null;
		try {
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(cUrl);
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);

			// set this transport factory for host-specific SSLContexts to work
			XmlRpcCommonsTransportFactory f = new XmlRpcCommonsTransportFactory(client);
			client.setTransportFactory(f);

			// sliver status
			rr = (Map<String, Object>)client.execute(GET_SLIVER_PROPERTIES, new Object[]{ sliceId, reservationId, new Object[]{}});

		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to get sliver properties: " + rr.get(MSG_RET_FIELD));

		Object[] tmpL = (Object[]) rr.get(RET_RET_FIELD);
		List<Map<String, String>> t1 = new ArrayList<Map<String, String>>();
		for(Object o: tmpL) {
			t1.add((Map<String, String>)o);
		}

		return t1;
	}

	public String[] listMySlices(TransportContext ctx, URL cUrl) 
			throws TransportException, ContextTransportException {
		return _listMySlices((SSLTransportContext)ctx, cUrl);
	}
	
	@SuppressWarnings("unchecked")
	private String[] _listMySlices(SSLTransportContext ctx, URL cUrl) 
			throws XMLRPCTransportException, ContextTransportException {
		String[] result = null;
		ctx.establishIdentity(cUrl);

		Map<String, Object> rr = null;
		try {
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(cUrl);
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);

			// set this transport factory for host-specific SSLContexts to work
			XmlRpcCommonsTransportFactory f = new XmlRpcCommonsTransportFactory(client);
			client.setTransportFactory(f);

			// sliver status
			rr = (Map<String, Object>)client.execute(LIST_SLICES, new Object[]{ new Object[]{}});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException ("Unable to list active slices: " + rr.get(MSG_RET_FIELD));

		Object[] ll = (Object[])rr.get(RET_RET_FIELD);
		if (ll.length == 0)
			return new String[0];
		else {
			result = new String[ll.length];
			for (int i = 0; i < ll.length; i++)
				result[i] = (String)((Object[])rr.get(RET_RET_FIELD))[i];
		}

		return result;
	}

	public String modifySlice(TransportContext ctx, String sliceId, String modReq, URL cUrl) 
			throws TransportException, ContextTransportException  {
		return _modifySlice((SSLTransportContext)ctx, sliceId, modReq, cUrl);
	}
	
	@SuppressWarnings("unchecked")
	private String _modifySlice(SSLTransportContext ctx, String sliceId, String modReq, URL cUrl) 
			throws XMLRPCTransportException, ContextTransportException  {
		assert(sliceId != null);
		assert(modReq != null);

		String result = null;
		ctx.establishIdentity(cUrl);

		Map<String, Object> rr = null;
		try {
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(cUrl);
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);

			// set this transport factory for host-specific SSLContexts to work
			XmlRpcCommonsTransportFactory f = new XmlRpcCommonsTransportFactory(client);
			client.setTransportFactory(f);

			// modify slice
			rr = (Map<String, Object>)client.execute(MODIFY_SLICE, new Object[]{ sliceId, new Object[]{}, modReq});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to modify slice: " + (String)rr.get(MSG_RET_FIELD));

		result = (String)rr.get(RET_RET_FIELD);
		return result;
	}

	public String listResources(TransportContext ctx, URL cUrl) 
			throws TransportException, ContextTransportException {
		return _listResources((SSLTransportContext)ctx, cUrl);
	}

	@SuppressWarnings("unchecked")
	private String _listResources(SSLTransportContext ctx, URL cUrl) 
			throws XMLRPCTransportException, ContextTransportException {

		String result = null;
		ctx.establishIdentity(cUrl);

		Map<String, Object> rr = null;
		try {
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(cUrl);
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);

			// set this transport factory for host-specific SSLContexts to work
			XmlRpcCommonsTransportFactory f = new XmlRpcCommonsTransportFactory(client);
			client.setTransportFactory(f);

			// modify slice
			rr = (Map<String, Object>)client.execute(LIST_RESOURCES, new Object[]{ new Object[]{}, new HashMap<String, String>()});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException("Unable to contact SM " + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to list resources: " + (String)rr.get(MSG_RET_FIELD));

		result = (String)rr.get(RET_RET_FIELD);
		return result;
	}
}
