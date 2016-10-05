package orca.ahab.libndl.extras;



import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import orca.ahab.libndl.Slice;
import orca.ahab.libndl.resources.request.BroadcastNetwork;
import orca.ahab.libndl.resources.request.ComputeNode;
import orca.ahab.libndl.resources.request.Interface;
import orca.ahab.libndl.resources.request.InterfaceNode2Net;
import orca.ahab.libndl.resources.request.Network;
import orca.ahab.libndl.resources.request.Node;
import orca.ahab.libtransport.ISliceTransportAPIv1;
import orca.ahab.libtransport.ITransportProxyFactory;
import orca.ahab.libtransport.PEMTransportContext;
import orca.ahab.libtransport.TransportContext;
import orca.ahab.libtransport.util.ContextTransportException;
import orca.ahab.libtransport.util.TransportException;
import orca.ahab.libtransport.xmlrpc.XMLRPCProxyFactory;

public class PriorityNetwork {

	private ComputeNode controller;
	private ArrayList<String> siteList;
	private HashMap<String,ComputeNode> switches;
	private HashMap<String,BroadcastNetwork> localAttachmentPoints;
	private HashMap<String,BroadcastNetwork> interdomainSwitchLinks;

	private Slice s;
	private String name;
	private String controllerSiteStr;
	private String controllerPublicIP;
	
	
	//Hack so we can update slice
	public static PriorityNetwork create(Slice s, String name){
		//SDN sdn = SDN.create(s, name, "RENCI (Chapel Hill, NC USA) XO Rack");
		PriorityNetwork sdn = PriorityNetwork.create(s, name, "PSC (Pittsburgh, TX, USA) XO Rack");
		
		
		sdn.startController();

		return sdn;
	}
	
	public static PriorityNetwork get(Slice s, String name){
		PriorityNetwork net = new PriorityNetwork(s, name, "PSC (Pittsburgh, TX, USA) XO Rack");
		
		net.init();
		
		s.logger().debug("PriorityNetwork.init controller = " + net.controller);
		s.logger().debug("PriorityNetwork.init siteList = " + net.siteList);
		s.logger().debug("PriorityNetwork.init switches = " + net.switches);
		s.logger().debug("PriorityNetwork.init localAttachmentPoints = " + net.localAttachmentPoints);
		s.logger().debug("PriorityNetwork.init interdomainSwitchLinks = " + net.interdomainSwitchLinks);
		
		return net;
	}
	
	//initialized a priority network from an existing slice
	private void init(){
		for (Node n : s.getNodes()){
			//if controller
			if(n.getName().equals(name+"_controller")){
				this.controller = (ComputeNode) s.getResourceByName(name+"_controller"); 
			}
			//if switch
			if(n.getName().startsWith(name+"_sw")){
				switches.put(n.getName().replace(name+"_sw_", ""), (ComputeNode) n);
				siteList.add(n.getName().replace(name+"_sw_", ""));
			}
		}
		
		for (Network n : s.getLinks()){
			//if local attachment point
			if(n.getName().startsWith(name+"_vlan_sw_")){
				localAttachmentPoints.put(n.getName().replace(name+"_vlan_sw_",""), (BroadcastNetwork) n); 
			}
			//if interdomain switch link
			if(n.getName().startsWith(name+"_net_")){
				//rivate HashMap<String,ComputeNode> switches;
				interdomainSwitchLinks.put(n.getName().replace(name+"_net_",""), (BroadcastNetwork) n);
			}
		}
		
		
	}
	
	private static PriorityNetwork create(Slice s, String name, String controllerSite){
		PriorityNetwork sdn = new PriorityNetwork(s, name, controllerSite);
		
		
		return sdn;
	}
	
	
	
	private PriorityNetwork(Slice s, String name, String controllerSiteStr){
		this.s = s;
		this.name = name;
		this.controllerSiteStr = controllerSiteStr;
		
		siteList = new ArrayList<String>();
		
		switches = new HashMap<String,ComputeNode>();
		localAttachmentPoints = new HashMap<String,BroadcastNetwork>();
		interdomainSwitchLinks = new HashMap<String,BroadcastNetwork>();
	}
	
	
	
	
	public void bind(String name, String rdfID){
		this.addSDNSite(name, rdfID, this.controllerPublicIP);   
	}
	
	public void addNode(Node n, String site, String ip){
		
		
		BroadcastNetwork net = this.localAttachmentPoints.get(site); 
		
		s.logger().debug("BroadcastNetwork net = " + net);
	
		Interface int1  = net.stitch(n);
		
		s.logger().debug("Interface int1 = " + int1);
		
		((InterfaceNode2Net)int1).setIpAddress(ip);
		((InterfaceNode2Net)int1).setNetmask("255.255.255.0");
		
		s.logger().debug("AddNode2Net request:  " + s.getRequest());
		
	}
	
	
	
	public void QoS_setPriority(String site1, String site2, int priority){
		
	}

	public void QoS_commit(){
		
	}
	
	public String getState(String site){
		ComputeNode sw = switches.get(site);		
		Network net = localAttachmentPoints.get(site);
		
		
		s.logger().debug("PriorityNetwork.getState(): net = " + net);
		s.logger().debug("PriorityNetwork.getState(): sw = " + sw);
		s.logger().debug("PriorityNetwork.getState(): net.getState() = " + net.getState());
		s.logger().debug("PriorityNetwork.getState(): sw.getState() = " + sw.getState());
		
		
		if(net.getState().equals("Active") && sw.getState().equals("Active")){
			return "Active";
		}
		
		if(net.getState().equals("Failed") || sw.getState().equals("Failed")){
			return "Failed";
		}
		
		return "Building";
	}
	
	public String getState(){
		
		String state = "Active";
		for( String site : siteList){
			if(this.getState(site).equals("Failed")){
				return "Failed";
			}
			
			if(this.getState(site).equals("Building")){
				state = "Building";
			}
		}		
		return state;
	}
	
	private void startController(){

		//Create the controller
		String controllerImageShortName="Centos7-SDN-controller.v0.4";
		String controllerImageURL ="http://geni-images.renci.org/images/pruth/SDN/Centos7-SDN-controller.v0.4/Centos7-SDN-controller.v0.4.xml";
		String controllerImageHash ="b71cbdbd8de5b2d187ae9a3efb0a19a170b92183";
		String controllerDomain=controllerSiteStr;//"RENCI (Chapel Hill, NC USA) XO Rack";
		String contorllerNodeType="XO Medium";
		String controllerPostBootScript="#!/bin/bash\n echo hello, world > /tmp/bootscript.log";
		String controllerName=name + "_controller";

		try{
			ComputeNode   controllerNode = s.addComputeNode(controllerName);
			controllerNode.setImage(controllerImageURL,controllerImageHash,controllerImageShortName);
			controllerNode.setNodeType(contorllerNodeType);
			controllerNode.setDomain(controllerDomain);
			controllerNode.setPostBootScript(controllerPostBootScript);

			String switchImageShortName="Centos6.7-SDN.v0.1";
			String switchImageURL ="http://geni-images.renci.org/images/pruth/SDN/Centos6.7-SDN.v0.1/Centos6.7-SDN.v0.1.xml";
			String switchImageHash ="77ec2959ff3333f7f7e89be9ad4320c600aa6d77";
			String switchDomain=controllerDomain;
			String switchNodeType="XO Medium";
			String switchPostBootScript=getSDNControllerScript();

			s.commit(10, 20);
			
			this.controllerPublicIP = this.blockUntilUp(controllerName);

			s.refresh();
			
			System.out.println("controllerPublicIP: " + controllerPublicIP);
			
			this.controller = (ComputeNode) s.getResourceByName(controllerName);
		} catch (Exception e){
			e.printStackTrace();
			System.err.println("Proxy factory test failed");
			assert(false);
		}
	}
	
	
	public void addSDNSite(String name, String site, String SDNControllerIP){
		//String sliceName="pruth.sdn.2";	
		
		String switchImageShortName="Centos6.7-SDN.v0.1";
		String switchImageURL ="http://geni-images.renci.org/images/pruth/SDN/Centos6.7-SDN.v0.1/Centos6.7-SDN.v0.1.xml";
		String switchImageHash ="77ec2959ff3333f7f7e89be9ad4320c600aa6d77";
		String switchDomain=site;
		String switchNodeType="XO Medium";
		String switchPostBootScript=getSDNSwitchScript(SDNControllerIP);
		
		String switchName = this.name + "_sw_"+name;
		String networkName = this.name + "_vlan_sw_"+name;
		
		//setup the node
		ArrayList<ComputeNode> switches = new ArrayList<ComputeNode>();
		ComputeNode  sw = s.addComputeNode(switchName);
		sw.setImage(switchImageURL,switchImageHash,switchImageShortName);
		sw.setNodeType(switchNodeType);
		sw.setDomain(switchDomain);
		sw.setPostBootScript(switchPostBootScript);

		BroadcastNetwork net = s.addBroadcastLink(networkName);
		Interface localIface = net.stitch(sw);
		
		this.switches.put(name, sw);
		this.localAttachmentPoints.put(name,net);	
		this.siteList.add(name);
		
		//Add network to parent
		int position = siteList.indexOf(name);
		if (position > 0){
			String parentName = siteList.get((position-1)/2);
			ComputeNode node1 = this.switches.get(parentName);
			ComputeNode node2 = sw;
	
			String interdomainNetworkName = this.name + "_net_" +  parentName + "_" + name;
			BroadcastNetwork interdomainNet = s.addBroadcastLink(interdomainNetworkName);
			Interface int1 = interdomainNet.stitch(node1);
			Interface int2 = interdomainNet.stitch(node2);
		
			
		}
	}

	
	
	/************************
	 * 
	 * Helper methods
	 *
	 */
	
	
	
	private static void sleep(int sec){
		try {
		    Thread.sleep(sec*1000); //1000 milliseconds is one second.
		} catch(InterruptedException ex) {  
		    Thread.currentThread().interrupt();
		}
	}
	
	public String getPublicIP(String nodeName){
		
		try{
			ComputeNode  node = (ComputeNode) s.getResourceByName(nodeName);
		
			return node.getManagementIP();
		} catch  (Exception e){
			e.printStackTrace();
			System.err.println("Proxy factory test failed");
			assert(false);
		}
		return "";
	}
	

	
	private String blockUntilUp(String nodeName){
		int count = 0;
		String SDNControllerIP = null; 
    	while (SDNControllerIP == null){
    		s.refresh();
    	    SDNControllerIP=this.getPublicIP(nodeName);
    	    System.out.println("Waiting for node: " +nodeName + " (try " + count++ + ")");
    	    
    	    if(SDNControllerIP != null) break;
    	    
    	    sleep(30);
    	};
    	return SDNControllerIP;
	}
		
	private static String  getSDNControllerScript(){
		return "#!/bin/bash \n" +
				"#script not build yet";
	}
	
	private static String getSDNSwitchScript(String SDNControllerIP){
	    
		return "#!/bin/bash \n" +
			"{ \n " +
			"reset_bridge () { \n" +
			"   #$1 bridge name \n" +                                                                                                                                                                                              
			"  br_name=$1\n" +
			"  controller_ip=$2\n" +
			"\n" +
			" echo reset_bridge $br_name\n" +
			" ovs-vsctl del-br $br_name\n" +
			" ovs-vsctl add-br $br_name\n" +
			" ifconfig $br_name up\n" +
			"\n" +
			" echo \"setting ovs to use controller \" ${controller_ip} \n" +
			" ovs-vsctl set-controller br0 tcp:${controller_ip}:6633 \n" +
			"     ovs-vsctl set controller br0 connection-mode=out-of-band \n" +
			"     		 \n" +
			" } \n" +
			" \n" +
			" \n" +
			" is_bridge_consistant () { \n" +
			"     #checks to see if bridge has non-existant interfaces  \n" +                                                                                                                                                         
			"     br_name=$1 \n" +
			"     echo checking $br_name \n" +
			"     #1 bridge name                                           \n" +                                                                                                                                                      
			"     for iface in `ovs-vsctl list-ports ${br_name}`; do \n" +
			"        echo checkong $iface \n" +
			"         ip link show $iface 2>&1 > /dev/null \n" +
			"        if [ \"$?\" != \"0\" ]; then \n" +
			"            #error iface dne                             \n" +                                                                                                              
			"            echo Found wedged iface $iface \n" +
			"            return 1 \n" +
			"        fi \n" +
			"    done \n" +
			"     \n" +
			"     return 0 \n" +
			" } \n" +
			" \n" +
			" /etc/init.d/openvswitch restart \n" +
			" sleep 60 \n" +
			" ovs-vsctl add-br br0 \n" +
			" ifconfig br0 up \n" +
			" controller_ip='" + SDNControllerIP + "' \n" +
			" echo \"setting ovs to use controller \" ${controller_ip} \n" +
			" ovs-vsctl set-controller br0 tcp:${controller_ip}:6633 \n" +
			" ovs-vsctl set controller br0 connection-mode=out-of-band \n" +
			" ovs-appctl fdb/show br0 \n" +
			
			" while true; do \n" +
			"     sleep 10 \n" +
			"     ping -c 1 8.8.8.8\n" +
			"     if [ \"$?\" == \"0\" ]; then \n" +
			"        #error iface dne                             \n" +                                                                                                              
			"        echo Network is up! \n" +
			"        break \n" +
			"     fi \n" +
			"done\n" +
			"\n" +
			" while true; do \n" +
			"  #check to see if bridge is consistant \n" +
			"   is_bridge_consistant \"br0\" \n" +
			"           if [ \"$?\" != \"0\" ]; then \n" +
			"           echo resetting bridge br0 \n" +
			"           reset_bridge \"br0\" ${controller_ip} \n" +
			"   fi \n" +
			"     br0_ifaces=`ovs-vsctl list-ifaces br0` \n" +
			"     echo 'Ifaces on br0: '$br0_ifaces \n" +
			"     echo 'All ifaces' \n" +
			"     for i in `ip link show | grep '^[1-9]' | awk -F\": \"  '{print $2}'`; do  \n" +
			"        ip=`ip -f inet -o addr show $i` \n" +
			"      if [[ \"$ip\" != \"\" ]]; then \n" +
			"          echo skipping $i \n" +
			"      else \n" +
			"         echo checking ${i};   \n" +
			"          br_4_iface=`ovs-vsctl iface-to-br $i` \n" +
			"         if [ \"$?\" != \"0\" ]; then \n" +
			"             echo \"adding \"$i \n" +
			"             ifconfig $i promisc up \n" +
			"             ovs-vsctl add-port br0 $i \n" +
			"         else \n" +
			"            echo \"skipping already added iface: \"$i \n" +
			"          fi \n" +
			"       fi \n" +
			"   done \n" +
			"     echo sleeping 10 \n" +
			"     sleep 10  \n" +
			" done \n" +
			" echo Bootscript done. \n" +
			" } 2>&1 > /tmp/bootscript.log \n" +
			" " ;
	}

}
