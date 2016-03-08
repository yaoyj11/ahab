package orca.ahab.libtransport;

import java.util.Date;
import java.util.List;
import java.util.Map;

import orca.ahab.libtransport.util.ContextTransportException;
import orca.ahab.libtransport.util.TransportException;
import orca.ahab.libtransport.xmlrpc.XMLRPCTransportException;

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

}