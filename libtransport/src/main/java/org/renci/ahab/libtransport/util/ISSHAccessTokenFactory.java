package org.renci.ahab.libtransport.util;

import org.renci.ahab.libtransport.SSHAccessToken;

public interface ISSHAccessTokenFactory<T extends SSHAccessToken> {
	
	public T getPopulatedToken() throws UtilTransportException;
}
