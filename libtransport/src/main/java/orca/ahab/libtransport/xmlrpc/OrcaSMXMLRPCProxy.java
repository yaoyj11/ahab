package orca.ahab.libtransport.xmlrpc;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.DatatypeConverter;

import orca.ahab.libtransport.AccessToken;
import orca.ahab.libtransport.ISliceTransportAPIv1;
import orca.ahab.libtransport.SSHAccessToken;
import orca.ahab.libtransport.SSLTransportContext;
import orca.ahab.libtransport.SliceAccessContext;
import orca.ahab.libtransport.util.ContextTransportException;
import orca.ahab.libtransport.util.TransportException;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

public class OrcaSMXMLRPCProxy implements ISliceTransportAPIv1 {
	private static final String UNABLE_TO_CONTACT_SM = "Unable to contact SM ";
	private static final String RECEIVED_NULL_RESPONSE = "Received null response from SM ";
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
	private static final String GET_RESERVATION_SLICE_STITCH_INFO = "orca.getReservationSliceStitchInfo";
	private static final String PERMIT_SLICE_STITCH = "orca.permitSliceStitch";
	private static final String REVOKE_SLICE_STITCH = "orca.revokeSliceStitch";
	private static final String PERFORM_SLICE_STITCH = "orca.performSliceStitch";
	private static final String UNDO_SLICE_STITCH = "orca.undoSliceStitch";

	private final SSLTransportContext ctx;
	private final URL cUrl;
	
	public OrcaSMXMLRPCProxy(SSLTransportContext c, URL u) throws ContextTransportException {
		ctx = c;
		cUrl = u;
		
		ctx.establishIdentity(cUrl);
	}

	private XmlRpcClient getConfiguredClient() {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(cUrl);
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(config);

		// set this transport factory for host-specific SSLContexts to work
		XmlRpcCommonsTransportFactory f = new XmlRpcCommonsTransportFactory(client);
		client.setTransportFactory(f);
		
		return client;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getVersion() throws TransportException, ContextTransportException {
		Map<String, Object> versionMap = null;

		try {
			// get verbose list of the AMs
			versionMap = (Map<String, Object>)getConfiguredClient().execute(GET_VERSION, new Object[]{});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl);
		}
		return versionMap;
	}

	@SuppressWarnings("unchecked")
	public String createSlice(String sliceId, String resReq, SliceAccessContext<? extends AccessToken> sliceCtx) 
			throws TransportException, ContextTransportException {
		assert(sliceId != null);
		assert(resReq != null);

		String result = null;

		Map<String, Object> rr = null;
		try {
			SliceAccessContext<SSHAccessToken> sshSliceCtx = (SliceAccessContext<SSHAccessToken>)sliceCtx;
			List<Map<String, ?>> users = (List<Map<String, ?>>)sshSliceCtx.getTokens(new XMLRPCAPIAdapter());
			// create sliver
			rr = (Map<String, Object>)getConfiguredClient().execute(CREATE_SLICE, 
					new Object[]{ sliceId, new Object[]{}, resReq, users});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl + " due to " + e);
		} catch (Exception e) {
			return "Unable to submit slice to SM:  " + cUrl + " due to " + e;
		}

		if (rr == null)
			throw new XMLRPCTransportException(RECEIVED_NULL_RESPONSE + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to create slice: " + (String)rr.get(MSG_RET_FIELD));

		result = (String)rr.get(RET_RET_FIELD);
		return result;
	}

	@SuppressWarnings("unchecked")
	public Boolean renewSlice(String sliceId, Date newDate) 
			throws TransportException, ContextTransportException {
		assert(sliceId != null);

		Boolean result = false;

		Map<String, Object> rr = null;
		try {
			// create sliver
			Calendar ecal = Calendar.getInstance();
			ecal.setTime(newDate);
			String endDateString = DatatypeConverter.printDateTime(ecal); // RFC3339/ISO8601
			rr = (Map<String, Object>)getConfiguredClient().execute(RENEW_SLICE, 
					new Object[]{ sliceId, new Object[]{}, endDateString});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException(RECEIVED_NULL_RESPONSE + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to renew slice: " + (String)rr.get(MSG_RET_FIELD));

		result = (Boolean)rr.get(RET_RET_FIELD);
		return result;
	}

	@SuppressWarnings("unchecked")
	public boolean deleteSlice(String sliceId)  
			throws TransportException, ContextTransportException {
		boolean res = false;

		Map<String, Object> rr = null;
		try {
			// delete sliver
			rr = (Map<String, Object>)getConfiguredClient().execute(DELETE_SLICE, 
					new Object[]{ sliceId, new Object[]{}});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException(RECEIVED_NULL_RESPONSE + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to delete slice: " + (String)rr.get(MSG_RET_FIELD));
		else
			res = (Boolean)rr.get(RET_RET_FIELD);

		return res;
	}


	private String stripManifest(String m) {
		if (m == null)
			return null;
		int ind = m.indexOf("<rdf:RDF");
		if (ind > 0)
			return m.substring(ind);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public String sliceStatus(String sliceId)  throws TransportException, ContextTransportException {
		assert(sliceId != null);

		String result = null;

		Map<String, Object> rr = null;
		try {

			// sliver status
			rr = (Map<String, Object>)getConfiguredClient().execute(SLICE_STATUS, 
					new Object[]{ sliceId, new Object[]{}});

		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException(RECEIVED_NULL_RESPONSE + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to get slice status: " + rr.get(MSG_RET_FIELD));

		result = (String)rr.get(RET_RET_FIELD);

		return stripManifest(result);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Map<String, String>> getReservationStates(String sliceId, List<String> reservationIds)  
			throws TransportException, ContextTransportException {
		assert(sliceId != null);
		
		if (reservationIds == null)
			reservationIds = new ArrayList<>();

		Map<String, Object> rr = null;
		try {
			// sliver status
			rr = (Map<String, Object>)getConfiguredClient().execute(GET_RESERVATION_STATES, 
					new Object[]{ sliceId, reservationIds, new Object[]{}});

		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException(RECEIVED_NULL_RESPONSE + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to get reservation states: " + rr.get(MSG_RET_FIELD));

		return (Map<String, Map<String, String>>) rr.get(RET_RET_FIELD);
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, String>> getSliverProperties(String sliceId, String reservationId) 
			throws TransportException, ContextTransportException {
		assert((sliceId != null) && (reservationId != null));

		Map<String, Object> rr = null;
		try {
			
			// sliver status
			rr = (Map<String, Object>)getConfiguredClient().execute(GET_SLIVER_PROPERTIES, 
					new Object[]{ sliceId, reservationId, new Object[]{}});

		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException(RECEIVED_NULL_RESPONSE + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to get sliver properties: " + rr.get(MSG_RET_FIELD));

		Object[] tmpL = (Object[]) rr.get(RET_RET_FIELD);
		List<Map<String, String>> t1 = new ArrayList<Map<String, String>>();
		for(Object o: tmpL) {
			t1.add((Map<String, String>)o);
		}

		return t1;
	}

	@SuppressWarnings("unchecked")
	public String[] listMySlices() 
			throws TransportException, ContextTransportException {
		String[] result = null;

		Map<String, Object> rr = null;
		try {
			// sliver status
			rr = (Map<String, Object>)getConfiguredClient().execute(LIST_SLICES, 
					new Object[]{ new Object[]{}});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException(RECEIVED_NULL_RESPONSE + cUrl);

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

	@SuppressWarnings("unchecked")
	public String modifySlice(String sliceId, String modReq) 
			throws TransportException, ContextTransportException  {
		assert(sliceId != null);
		assert(modReq != null);

		String result = null;

		Map<String, Object> rr = null;
		try {
			// modify slice
			rr = (Map<String, Object>)getConfiguredClient().execute(MODIFY_SLICE, 
					new Object[]{ sliceId, new Object[]{}, modReq});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException(RECEIVED_NULL_RESPONSE + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to modify slice: " + (String)rr.get(MSG_RET_FIELD));

		result = (String)rr.get(RET_RET_FIELD);
		return result;
	}

	@SuppressWarnings("unchecked")
	public String listResources() 
			throws TransportException, ContextTransportException {

		String result = null;

		Map<String, Object> rr = null;
		try {
			rr = (Map<String, Object>)getConfiguredClient().execute(LIST_RESOURCES, 
					new Object[]{ new Object[]{}, new HashMap<String, String>()});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException(RECEIVED_NULL_RESPONSE + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to list resources: " + (String)rr.get(MSG_RET_FIELD));

		result = (String)rr.get(RET_RET_FIELD);
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean permitSliceStitch(String sliceId, String resId, String secret)
			throws TransportException, ContextTransportException {
		boolean res = false;
		
		Map<String, Object> rr = null;
		try {
			// permit
			rr = (Map<String, Object>)getConfiguredClient().execute(PERMIT_SLICE_STITCH, 
					new Object[]{ sliceId, resId, secret, new Object[]{}});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException(RECEIVED_NULL_RESPONSE + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to permit slice stitch on slice " + sliceId + " reservation " + 
					resId + " due to: " + (String)rr.get(MSG_RET_FIELD));

		res = (Boolean)rr.get(RET_RET_FIELD);
		return res;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean revokeSliceStitch(String sliceId, String resId)
			throws TransportException, ContextTransportException {
		boolean res = false;
		
		Map<String, Object> rr = null;
		try {
			// revoke
			rr = (Map<String, Object>)getConfiguredClient().execute(REVOKE_SLICE_STITCH, 
					new Object[]{ sliceId, resId, new Object[]{}});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException(RECEIVED_NULL_RESPONSE + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to revoke slice stitch on slice " + sliceId + " reservation " + 
					resId + " due to: " + (String)rr.get(MSG_RET_FIELD));

		res = (Boolean)rr.get(RET_RET_FIELD);
		return res;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean performSliceStitch(String fromSlice, String fromReservation,
			String toSlice, String toReservation, String secret, Properties p)
			throws TransportException, ContextTransportException {
		boolean res = false;
		
		Map<String, Object> rr = null;
		try {
			rr = (Map<String, Object>)getConfiguredClient().execute(PERFORM_SLICE_STITCH, 
					new Object[]{ fromSlice, fromReservation, 
					toSlice, toReservation, secret, p, new Object[]{}});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException(RECEIVED_NULL_RESPONSE + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to perform slice stitch on slice " + fromSlice + " reservation " + 
					fromReservation + " due to: " + (String)rr.get(MSG_RET_FIELD));

		res = (Boolean)rr.get(RET_RET_FIELD);
		return res;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean undoSliceStitch(String fromSlice, String fromReservation,
			String toSlice, String toReservation) throws TransportException,
			ContextTransportException {
		boolean res = false;
		
		Map<String, Object> rr = null;
		try {
			rr = (Map<String, Object>)getConfiguredClient().execute(UNDO_SLICE_STITCH, 
					new Object[]{ fromSlice, fromReservation, 
					toSlice, toReservation, new Object[]{}});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException(RECEIVED_NULL_RESPONSE + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to perform slice stitch on slice " + fromSlice + " reservation " + 
					fromReservation + " due to: " + (String)rr.get(MSG_RET_FIELD));

		res = (Boolean)rr.get(RET_RET_FIELD);
		return res;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> getSliceStitchInfo(String sliceId, List<String> reservationIds) 
			throws TransportException, ContextTransportException {
		assert((sliceId != null) && (reservationIds != null));

		Map<String, Object> rr = null;
		try {
			rr = (Map<String, Object>)getConfiguredClient().execute(GET_RESERVATION_SLICE_STITCH_INFO, 
					new Object[]{ sliceId, reservationIds, new Object[]{}});
		} catch (XmlRpcException e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl + " due to " + e);
		} catch (Exception e) {
			throw new XMLRPCTransportException(UNABLE_TO_CONTACT_SM + cUrl);
		}

		if (rr == null)
			throw new XMLRPCTransportException(RECEIVED_NULL_RESPONSE + cUrl);

		if ((Boolean)rr.get(ERR_RET_FIELD))
			throw new XMLRPCTransportException("Unable to get reservation stitching info: " + rr.get(MSG_RET_FIELD));

		return (Map<String, Object>) rr.get(RET_RET_FIELD);
	}
}
