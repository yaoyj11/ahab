package orca.ahab.libtransport.util;

import orca.ahab.libtransport.SSHAccessToken;

public interface ISSHAccessTokenPopulator<T extends SSHAccessToken> {
	
	public T getPopulatedToken() throws UtilTransportException;
}
