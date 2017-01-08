/**
 * 
 */
package org.renci.ahab.libndl.resources.request;

import org.renci.ahab.libndl.SliceGraph;

import com.hp.hpl.jena.sparql.function.library.namespace;

/**
 * @author geni-orca
 *
 */
public class InterfaceNode2Net extends Interface{
	//private OrcaNode a;  -- from OrcaStitch
	//private OrcaLink b;  -- from OrcaStitch

	//private String ipAddress; 
	//private String netmask;
	private String macAddress;
	
	public InterfaceNode2Net(Node n, Network l, SliceGraph sliceGraph){
		super(n,l, sliceGraph);
	}
	
	public Node getNode() {
		return (Node)a;
	}
	public void setNode(Node node) {
		this.a = node;
	}
	public Network getLink() {
		return (Network)b;
	}
	public void setLink(Network link) {
		this.b = link;
	}
	public String getIpAddress() {
		return this.getNDLModel().getIP(this);
		//return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.getNDLModel().setIP(this, ipAddress);
		//this.ipAddress = ipAddress;
	}
	public String getNetmask() {
		return this.getNDLModel().getNetMask(this);
		//return netmask;
	}
	public void setNetmask(String netmask) {
		this.getNDLModel().setNetMask(this, netmask);
		//this.netmask = netmask;
	}
	public String getMacAddress() {
		return this.getNDLModel().getMacAddress(this);
		//return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	
	
	public String toString(){
		String rtnStr = "";
		
		rtnStr += "Stitch "; 
		if(a != null)
			rtnStr += a.getName() + " to ";
		else
			rtnStr += "null to ";
		
		if(b != null)
			rtnStr += b.getName();
		else
			rtnStr += "null to ";	
	
		//rtnStr += ", ipAddress: " + this.ipAddress;
		//rtnStr += ", netmask: " + this.netmask;
		rtnStr += ", mac: " + this.macAddress;
		
		return rtnStr;
	}

	@Override
	public String getPrintText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub
		sliceGraph.deleteResource(this);
		
	}

}
