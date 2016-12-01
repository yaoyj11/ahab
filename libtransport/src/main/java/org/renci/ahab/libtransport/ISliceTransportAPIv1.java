package org.renci.ahab.libtransport;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.renci.ahab.libtransport.util.ContextTransportException;
import org.renci.ahab.libtransport.util.TransportException;
import org.renci.ahab.libtransport.xmlrpc.XMLRPCTransportException;

public interface ISliceTransportAPIv1 {

	/**
	 * getVersion of the controller
	 * @return
	 * @throws XMLRPCTransportException
	 * @throws ContextTransportException
	 */
	public abstract Map<String, Object> getVersion() throws TransportException,
			ContextTransportException;

	/**
	 * Create slice 
	 * @param sliceId - slice id
	 * @param resReq - NDL request
	 * @param sliceCtx - slice access context with SSH keys
	 * @return
	 * @throws CTransportException
	 * @throws ContextTransportException
	 */
	public abstract String createSlice(String sliceId,
			String resReq, SliceAccessContext<? extends AccessToken> sliceCtx)
			throws TransportException, ContextTransportException;

	/**
	 * Renew a slice
	 * @param sliceId
	 * @param newDate
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract Boolean renewSlice(String sliceId,
			Date newDate) throws TransportException,
			ContextTransportException;

	/**
	 * Delete a slice
	 * @param sliceId
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract boolean deleteSlice(String sliceId) throws TransportException,
			ContextTransportException;

	/**
	 * Get a status of the slice (manifest)
	 * @param sliceId
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract String sliceStatus(String sliceId) throws TransportException,
			ContextTransportException;

	/**
	 * Get states of reservations named in the list
	 * @param sliceId
	 * @param reservationIds
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract Map<String, Map<String, String>> getReservationStates(String sliceId,
			List<String> reservationIds)
			throws TransportException, ContextTransportException;

	/**
	 * Get sliver properties for a named reservation within a named slice
	 * @param sliceId
	 * @param reservationId
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract List<Map<String, String>> getSliverProperties(
			String sliceId, String reservationId) throws TransportException,
			ContextTransportException;

	/**
	 * List all my slices
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract String[] listMySlices()
			throws TransportException, ContextTransportException;

	/**
	 * Modify a slice
	 * @param sliceId
	 * @param modReq
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract String modifySlice(String sliceId,
			String modReq) throws TransportException,
			ContextTransportException;

	/**
	 * List resources - list known resources
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract String listResources()
			throws TransportException, ContextTransportException;
	
	
	/**
	 * Permit a bearer-token-based stitch to a given reservation in a slice
	 * @param sliceId
	 * @param resId
	 * @param secret
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract boolean permitSliceStitch(String sliceId, String resId, String secret) 
			throws TransportException, ContextTransportException;
	
	/**
	 * Revoke permission to stitch to a given reservation
	 * @param sliceId
	 * @param resId
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract boolean revokeSliceStitch(String sliceId, String resId)
			throws TransportException, ContextTransportException;

	/**
	 * Perform a stitching operation between reservations (from slice/from reservation)
	 * and someone else's reservation (toSlice/toReservation) using bearer token secret
	 * established by the owner of the other reservation via permitStitch call. Properties
	 * can contain configuration properties for the stitch.
	 * @param fromSlice
	 * @param fromReservation
	 * @param toSlice
	 * @param toReservation
	 * @param secret
	 * @param p
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract boolean performSliceStitch(String fromSlice, String fromReservation, 
			String toSlice, String toReservation, String secret, Properties p) 
			throws TransportException, ContextTransportException;
	
	/**
	 * If present, undo a stitch between the reservations (from owned by the caller)
	 * @param fromSlice
	 * @param fromReservation
	 * @param toSlice
	 * @param toReservation
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract boolean undoSliceStitch(String fromSlice, String fromReservation,
			String toSlice, String toReservation) 
			throws TransportException, ContextTransportException;
	
	/**
	 * Retrieve stitching information about a list of reservations in a slice. Returned as a 
	 * Map of Maps (of Maps) (JSON-ish equivalent:
	 * 
	 * [<reservation id>:
	 *      "allowed": "yes"|"no"
	 * 		[<stitch guid>:
	 * 			"performed": <RFC3399 date/time>
	 * 			"undone":    <RFC3399 date/time> - optional
	 * 			"toreservation": <guid>
	 * 			"toslice": <string>
	 * 			"stitch.dn": <string>]]
	 * )
	 * @param sliceId
	 * @param resId 
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
    public static final String SliceStitchAllowed = "allowed";
    public static final String SliceStitchToReservation = "toreservation";
    public static final String SliceStitchToSlice = "toslice";
    public static final String SliceStitchPerformed = "performed";
    public static final String SliceStitchUndone = "undone";
    public static final String SliceStitchDN = "stitch.dn";
	public abstract Map<String, Object> getSliceStitchInfo(String sliceId, List<String> resId) 
			throws TransportException, ContextTransportException;
}