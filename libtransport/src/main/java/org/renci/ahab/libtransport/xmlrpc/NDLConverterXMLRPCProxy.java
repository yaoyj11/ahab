package org.renci.ahab.libtransport.xmlrpc;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.renci.ahab.libtransport.IConverterAPIv1;

/**
 * Class to call one of conveters on the list
 * @author ibaldin
 *
 */
public class NDLConverterXMLRPCProxy implements IConverterAPIv1 {
	List<URL> urlList;
	
	public NDLConverterXMLRPCProxy(List<URL> ul) {
		urlList = ul;
	}
	
    /**
	 * Make RR calls to converters until success or list exhausted
	 * @param call
	 * @param params
	 * @param urlList - list of converters
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String callConverter(ConverterCommand cmd, Object[] params) throws Exception {
		
		Map<String, Object> ret = null;
		Collections.shuffle(urlList);
		for(URL cUrl: urlList) {
			try {
				XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
				config.setServerURL(cUrl);
				XmlRpcClient client = new XmlRpcClient();
				client.setConfig(config);

				ret = (Map<String, Object>)client.execute("ndlConverter." + cmd.getCmd(), params);
				break;
			} catch (XmlRpcException e) {
				continue;
			} catch (ClassCastException ce) {
				// old converter, skip it
				continue;
			}
		}
		
		if (ret == null) {
			throw new Exception("Unable to call converter");
		}
		
		if ((Boolean)ret.get("err")) {
			throw new Exception ("Unable to call converter due to: " + (String)ret.get("msg"));
		}
		
		return (String)ret.get("ret");
	}
}
