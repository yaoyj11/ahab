package orca.ahab.libtransport.xmlrpc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

/**
 * Class to call one of conveters on the list
 * @author ibaldin
 *
 */
public class NDLConverter {
    public static final String RSPEC2_TO_NDL = "ndlConverter.requestFromRSpec2";
    public static final String RSPEC3_TO_NDL = "ndlConverter.requestFromRSpec3";
    public static final String MANIFEST_TO_RSPEC = "ndlConverter.manifestToRSpec3";
    public static final String AD_TO_RSPEC = "ndlConverter.adToRSpec3";
    public static final String ADS_TO_RSPEC = "ndlConverter.adsToRSpec3";

	/**
	 * Make RR calls to converters until success or list exhausted
	 * @param call
	 * @param params
	 * @param urlList - list of converters
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String callConverter(String call, Object[] params, List<String> urlList) throws Exception {
		
		Map<String, Object> ret = null;
		Collections.shuffle(urlList);
		for(String cUrl: urlList) {
			try {
				XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
				config.setServerURL(new URL(cUrl));
				XmlRpcClient client = new XmlRpcClient();
				client.setConfig(config);

				ret = (Map<String, Object>)client.execute(call, params);
				break;
			} catch (XmlRpcException e) {
				continue;
			} catch (MalformedURLException ue) {
				throw new Exception("Unable to call converter at " + cUrl + " due to " + ue);
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
