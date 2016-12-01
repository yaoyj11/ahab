package org.renci.ahab.libtransport;

import java.net.URL;

import org.renci.ahab.libtransport.util.ContextTransportException;

public abstract class TransportContext {
	private boolean identitySet = false;
	
	protected void setIdentity() {
		identitySet = true;
	}
	
	public boolean identityIsSet() {
		return identitySet;
	}
	
	protected void resetIdentity() {
		identitySet = false;
	}
	
	/**
	 * Establish identity with particular controller
	 * @param ctrlrUrl
	 * @throws ContextTransportException
	 */
	protected abstract void _establishIdentity(URL ctrlrUrl) throws ContextTransportException;
	
	public void establishIdentity(URL ctrlUrl) throws ContextTransportException {
		if (identityIsSet()) 
			return;
		
		_establishIdentity(ctrlUrl);
		setIdentity();
	}
}
