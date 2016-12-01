package org.renci.ahab.libtransport;

import java.util.List;
import java.util.Map;

import org.renci.ahab.libtransport.util.ContextTransportException;
import org.renci.ahab.libtransport.util.TransportException;
import org.renci.ahab.libtransport.xmlrpc.XMLRPCTransportException;

public interface IGENICHAPIv1 {
	public enum FedAgent {
		SA, MA
	}

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

	public enum FedCall {
		get_version, create, 
		lookup, update;
	}

	public enum FedObjectType {
		SLICE, PROJECT, SLIVER, KEY;
	}
	
	/**
	 * Get version of the MA or SA
	 * @param MA or SA
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public Map<String, Object> fedGetVersion(FedAgent a) throws TransportException,
			ContextTransportException;

	/**
	 * Check we are talking to SA of correct version
	 * @param MA or SA
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public void fedCheckVersion(FedAgent a) throws TransportException, ContextTransportException;

	/**
	 * Create slice on SA using URN and project id
	 * @param name
	 * @param projectUrn
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public String saCreateSlice(String name, String projectUrn) throws TransportException,
			ContextTransportException;

	/**
	 * Update slice field to specific value
	 * @param ctx
	 * @param sliceUrn
	 * @param field
	 * @param val
	 * @param url
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public void saUpdateSlice(String sliceUrn, FedField field, Object val) throws TransportException, ContextTransportException;

	/**
	 * Lookup a slice on SA
	 * @param sliceUrn
	 * @param fields - filter
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public Map<String, Object> saLookupSlice(String sliceUrn, FedField[] fields)
			throws TransportException, ContextTransportException;

	/**
	 * Lookup either a list of fields or all fields in a slice
	 * @param sliceUrn
	 * @param fields
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public Map<String, Object> saLookupSlice(String sliceUrn, List<FedField> fields)
			throws TransportException, ContextTransportException;

	/**
	 * MA Call to get all public and private SSH keys of a user
	 * @param userUrn
	 * @return
	 * @throws TransportException
	 * @throws ContextTransportException
	 */
	public Map<String, Object> maLookupAllSSHKeys(String userUrn) throws TransportException,
			ContextTransportException;

	/**
	 * Get the latest pair of SSH keys from MA
	 * @param userUrn
	 * @return
	 * @throws XMLRPCTransportException
	 * @throws ContextTransportException
	 */
	public Map<String, Object> maLookupLatestSSHKeys(String userUrn) throws TransportException,
			ContextTransportException;

}