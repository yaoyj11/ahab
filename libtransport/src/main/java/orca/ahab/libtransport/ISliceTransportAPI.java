package orca.ahab.libtransport;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import orca.ahab.libtransport.util.ContextTransportException;
import orca.ahab.libtransport.util.TransportException;
import orca.ahab.libtransport.xmlrpc.XMLRPCTransportException;

public interface ISliceTransportAPI {

	/**
	 * getVersion of the controller
	 * @param ctx
	 * @param cUrl
	 * @return
	 * @throws XMLRPCTransportException
	 * @throws ContextTransportException
	 */
	public abstract Map<String, Object> getVersion(TransportContext ctx,
			URL cUrl) throws TransportException,
			ContextTransportException;

	/**
	 * Create slice 
	 * @param ctx -  context
	 * @param sliceId - slice id
	 * @param resReq - NDL request
	 * @param sliceCtx - slice access context with SSH keys
	 * @param cUrl - controller URL
	 * @return
	 * @throws CTransportException
	 * @throws ContextTransportException
	 */
	public abstract String createSlice(TransportContext ctx, String sliceId,
			String resReq, SliceAccessContext<? extends AccessToken> sliceCtx, URL cUrl)
			throws TransportException, ContextTransportException;

	/**
	 * Renew a slice
	 * @param ctx
	 * @param sliceId
	 * @param newDate
	 * @param cUrl
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract Boolean renewSlice(TransportContext ctx, String sliceId,
			Date newDate, URL cUrl) throws TransportException,
			ContextTransportException;

	/**
	 * Delete a slice
	 * @param ctx
	 * @param sliceId
	 * @param cUrl
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract boolean deleteSlice(TransportContext ctx,
			String sliceId, URL cUrl) throws TransportException,
			ContextTransportException;

	/**
	 * Get a status of the slice (manifest)
	 * @param ctx
	 * @param sliceId
	 * @param cUrl
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract String sliceStatus(TransportContext ctx, String sliceId,
			URL cUrl) throws TransportException,
			ContextTransportException;

	/**
	 * Get states of reservations named in the list
	 * @param ctx
	 * @param sliceId
	 * @param reservationIds
	 * @param cUrl
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract Map<String, Map<String, String>> getReservationStates(
			TransportContext ctx, String sliceId,
			List<String> reservationIds, URL cUrl)
			throws TransportException, ContextTransportException;

	/**
	 * Get sliver properties for a named reservation within a named slice
	 * @param ctx
	 * @param sliceId
	 * @param reservationId
	 * @param cUrl
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract List<Map<String, String>> getSliverProperties(
			TransportContext ctx, String sliceId, String reservationId,
			URL cUrl) throws TransportException,
			ContextTransportException;

	/**
	 * List all my slices
	 * @param ctx
	 * @param cUrl
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract String[] listMySlices(TransportContext ctx, URL cUrl)
			throws TransportException, ContextTransportException;

	/**
	 * Modify a slice
	 * @param ctx
	 * @param sliceId
	 * @param modReq
	 * @param cUrl
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract String modifySlice(TransportContext ctx, String sliceId,
			String modReq, URL cUrl) throws TransportException,
			ContextTransportException;

	/**
	 * List resources - list known resources
	 * @param ctx
	 * @param cUrl
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public abstract String listResources(TransportContext ctx, URL cUrl)
			throws TransportException, ContextTransportException;

}