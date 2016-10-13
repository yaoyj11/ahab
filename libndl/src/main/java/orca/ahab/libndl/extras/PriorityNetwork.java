package orca.ahab.libndl.extras;



import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.ws.WebServiceRef;

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
	private HashMap<String,Long> siteIDs;
	private HashMap<String,ComputeNode> switches;
	private HashMap<String,BroadcastNetwork> localAttachmentPoints;
	private HashMap<String,BroadcastNetwork> interdomainSwitchLinks;
	
	private Slice s;
	private String priorityNetworkName;
	private String controllerSiteStr;
	private String controllerPublicIP;
	
	private long bandwidth;
	
	//data structures for handling RYU priorities
	private ArrayList<String> newSites = new ArrayList<String>();
	private HashMap<String,Integer> priorities = new HashMap<String,Integer>();
	private HashMap<String,ArrayList<ComputeNode>> siteNodes = new HashMap<String,ArrayList<ComputeNode>>();
	
	
	
	
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
			if(n.getName().equals(this.generateControllerName() /*priorityNetworkName+"_controller"*/)){
				this.controller = (ComputeNode) s.getResourceByName(this.generateControllerName() /*priorityNetworkName+"_controller"*/); 
			}
			//if switch
			if(n.getName().startsWith(this.generateSwitchNamePrefix())){
				//switches.put(n.getName().replace(this.generateSwitchNamePrefix(), ""), (ComputeNode) n);
				//siteList.add(n.getName().replace(this.generateSwitchNamePrefix(), ""));
				switches.put(this.getSiteNameFromSwitchName(n.getName()), (ComputeNode) n);
				siteList.add(this.getSiteNameFromSwitchName(n.getName()));
			}
		}
		
		for (Network n : s.getLinks()){
			//if local attachment point
			if(n.getName().startsWith(this.generateLocalAttachmentNetworkNamePrifix())){
				//localAttachmentPoints.put(n.getName().replace(this.generateLocalAttachmentNetworkNamePrifix(),""), (BroadcastNetwork) n); 
				localAttachmentPoints.put(this.getSiteNameFromLocalAttachmentPointName(n.getName()), (BroadcastNetwork) n);
			}
			//if interdomain switch link
			if(n.getName().startsWith(this.generateIntersiteNetworkNamePrefix())){
				//rivate HashMap<String,ComputeNode> switches;
				interdomainSwitchLinks.put(n.getName().replace(this.generateIntersiteNetworkNamePrefix(),""), (BroadcastNetwork) n);
			}
		}
		
		this.controllerPublicIP = this.blockUntilUp(this.generateControllerName());
		
	}
	
	public static PriorityNetwork create(Slice s, String name, String controllerSite, long bandwidth){
		PriorityNetwork sdn = new PriorityNetwork(s, name, controllerSite);
		sdn.setBandwidth(bandwidth);
		sdn.startController();
		return sdn;
	}
	
	

	private static PriorityNetwork create(Slice s, String name, String controllerSite){
		return PriorityNetwork.create(s, name, controllerSite, 1000000000l);
	}                                                          
	
	
	private PriorityNetwork(Slice s, String name, String controllerSiteStr){
		this.s = s;
		this.priorityNetworkName = name;
		this.controllerSiteStr = controllerSiteStr;
		
		siteList = new ArrayList<String>();
		siteIDs = new HashMap<String,Long>();
		switches = new HashMap<String,ComputeNode>();
		localAttachmentPoints = new HashMap<String,BroadcastNetwork>();
		interdomainSwitchLinks = new HashMap<String,BroadcastNetwork>();
		
		this.bandwidth = bandwidth;
	}
	
	private  String generateControllerName(){
		return priorityNetworkName + "_controller";
	}
	
	private String getSiteNameFromLocalAttachmentPointName(String switchName){
		String[] tokens = switchName.toString().split("[_]+");
		return tokens[tokens.length-2];
	}
	
	private String getSiteNameFromSwitchName(String switchName){
		String[] tokens = switchName.toString().split("[_]+");
		return tokens[tokens.length-2];
	}
	
	private Long getSiteIDFromSwitchName(String switchName){
		String[] tokens = switchName.toString().split("[_]+");
		return Long.parseLong(tokens[tokens.length-1]);
	}
	
	private  String generateSwitchName(String siteName){
		return  generateSwitchNamePrefix()+siteName+"_"+getSiteID(siteName);
	}
	
	private  String generateSwitchNamePrefix(){
		return  this.priorityNetworkName + "_sw_";
	}
	
	private  String generateLocalAttachmentNetworkName(String siteName){
		return generateLocalAttachmentNetworkNamePrifix()+siteName+"_"+getSiteID(siteName);
	}
	private  String generateLocalAttachmentNetworkNamePrifix(){
		return this.priorityNetworkName + "_vlan_sw_";
	}
	private   String generateIntersiteNetworkName(String siteName1, String siteName2){
		return   generateIntersiteNetworkNamePrefix() +  siteName1 + "_" + siteName2;
	}
	
	
	private String generateIntersiteNetworkNamePrefix(){
		return   this.priorityNetworkName + "_net_";
	}	
	
	private static Long generateSwitchDPID_Long(long id){
		return 0x001100000000l + id;
	}
	private static String generateSwitchDPID_dec(long id){
		return generateSwitchDPID_Long(id).toString();
	}
	private static String generateSwitchDPID_mac(long id){
		Long dpid = generateSwitchDPID_Long(id);
		
		
		
		String dpidStr = Long.toHexString(dpid);
		
		//System.out.println("generateSwitchDPID_mac(10): id = " + id + ", dpid = " + dpid + ", dpidStr = " + dpidStr);
		
		//pad with zeros
		while (dpidStr.length() < 12){
			dpidStr = "0" + dpidStr;
		}
		//System.out.println("generateSwitchDPID_mac(10): id = " + id + ", dpid = " + dpid + ", dpidStr = " + dpidStr);
		//add colons
		String rtnStr = "";
		rtnStr += dpidStr.substring(0, 2) + ":" +
				  dpidStr.substring(2, 4) + ":" +
				  dpidStr.substring(4, 6) + ":" +
				  dpidStr.substring(6, 8) + ":" +
				  dpidStr.substring(8, 10) + ":" +
				  dpidStr.substring(10, 12);
		//System.out.println("generateSwitchDPID_mac(10): id = " + id + ", dpid = " + dpid + ", dpidStr = " + dpidStr + ", rtnStr = " + rtnStr);
		
		return rtnStr;
	}

	
	private Long getSiteID(String name){
		return siteIDs.get(name);
	}
	
	private void setBandwidth(long bandwidth) {
		this.bandwidth = bandwidth;
	}
	
	
	public void bind(String name, String rdfID){
		this.addSDNSite(name, rdfID, this.controllerPublicIP);  
		
		//add site to new site to be processed after instantiation: 
		//HACK needs fixing. switch need to be up before finishing the processing
		this.newSites.add(name);
		this.priorities.put(name, 1);
		this.siteNodes.put(name, new ArrayList<ComputeNode>());
	}
	
	public void addNode(Node n, String site, String ip, String mask){
		
		
		BroadcastNetwork net = this.localAttachmentPoints.get(site); 
		
		s.logger().debug("BroadcastNetwork net = " + net);
	
		Interface int1  = net.stitch(n);
		
		s.logger().debug("Interface int1 = " + int1);
		
		((InterfaceNode2Net)int1).setIpAddress(ip);
		((InterfaceNode2Net)int1).setNetmask(mask);
		
		s.logger().debug("AddNode2Net request:  " + s.getRequest());
		
		
		
		//Update priorities if needed
		//Add new sites
		this.processNewSites();
		//(re)set priorities
		
	}
	
	private void processNewSites(){
		
		while(!newSites.isEmpty()){
			String site = newSites.remove(0);
			//set ovsdb
			//dddd
		}
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
		
		//String controllerImageShortName="Centos7-SDN-controller.v0.4";
		//String controllerImageURL ="http://geni-images.renci.org/images/pruth/SDN/Centos7-SDN-controller.v0.4/Centos7-SDN-controller.v0.4.xml";
		//String controllerImageHash ="b71cbdbd8de5b2d187ae9a3efb0a19a170b92183";
		String controllerImageShortName="RYU-Ubuntu-14.04-v0.1";
		String controllerImageURL ="http://geni-images.renci.org/images/pruth/SDN/RYU-Ubuntu-14.04-v0.1/RYU-Ubuntu-14.04-v0.1.xml";
		String controllerImageHash ="b623bd059f18c9c55ab57f50151e7cbaa9c2b942";
		String controllerDomain=controllerSiteStr;//"RENCI (Chapel Hill, NC USA) XO Rack";
		String contorllerNodeType="XO Medium";
		String controllerPostBootScript=getSDNControllerScript();
		String controllerName=generateControllerName(); //name + "_controller";

		try{
			ComputeNode   controllerNode = s.addComputeNode(controllerName);
			controllerNode.setImage(controllerImageURL,controllerImageHash,controllerImageShortName);
			controllerNode.setNodeType(contorllerNodeType);
			controllerNode.setDomain(controllerDomain);
			controllerNode.setPostBootScript(controllerPostBootScript);

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
		this.siteList.add(name);
		long switchNum = siteList.indexOf(name);
		this.siteIDs.put(name,switchNum);
		
		String switchImageShortName="Centos6.7-SDN.v0.1";

		String switchImageURL ="http://geni-images.renci.org/images/pruth/SDN/Centos6.7-SDN.v0.1/Centos6.7-SDN.v0.1.xml";
		String switchImageHash ="77ec2959ff3333f7f7e89be9ad4320c600aa6d77";
		String switchDomain=site;
		String switchNodeType="XO Medium";
		String switchPostBootScript=getSDNSwitchScript(SDNControllerIP,switchNum);
		
		String switchName = this.generateSwitchName(name);//  this.priorityNetworkName + "_sw_"+name;
		String networkName = this.generateLocalAttachmentNetworkName(name);  //this.priorityNetworkName + "_vlan_sw_"+name;
		
		//setup the node
		ArrayList<ComputeNode> switches = new ArrayList<ComputeNode>();
		ComputeNode  sw = s.addComputeNode(switchName);
		sw.setImage(switchImageURL,switchImageHash,switchImageShortName);
		sw.setNodeType(switchNodeType);
		sw.setDomain(switchDomain);
		sw.setPostBootScript(switchPostBootScript);

		BroadcastNetwork net = s.addBroadcastLink(networkName,bandwidth);
		//net.setBandwidth(bandwidth);
		
		Interface localIface = net.stitch(sw);
		
		this.switches.put(name, sw);
		this.localAttachmentPoints.put(name,net);	
		//this.siteList.add(name);
		
		//Add network to parent
		int position = siteList.indexOf(name);
		if (position > 0){
			String parentName = siteList.get((position-1)/2);
			ComputeNode node1 = this.switches.get(parentName);
			ComputeNode node2 = sw;
	
			String interdomainNetworkName = this.generateIntersiteNetworkName(parentName,  name); //  this.priorityNetworkName + "_net_" +  parentName + "_" + name;
			BroadcastNetwork interdomainNet = s.addBroadcastLink(interdomainNetworkName, bandwidth);
			//interdomainNet.setBandwidth(bandwidth);
			
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
				"sed '/OFPFlowMod(/,/)/s/)/, table_id=1)/' /usr/local/lib/python2.7/dist-packages/ryu/app/simple_switch_13.py > /usr/local/lib/python2.7/dist-packages/ryu/app/qos_simple_switch_13.py \n" +   
				"/usr/local/bin/ryu-manager --log-file /tmp/ryu.log  ryu.app.rest_qos ryu.app.qos_simple_switch_13 ryu.app.rest_conf_switch ryu.app.ofctl_rest  2>&1 > /tmp/ryu.stdout & \n "; 
				
				
				//"/usr/local/bin/ryu-manager --log-file /tmp/ryu.log /usr/local/lib/python2.7/dist-packages/ryu/app/simple_switch.py 2>&1 > /tmp/ryu.stdout &  \n";
		
	}
	
	

	private static String getSDNSwitchScript(String SDNControllerIP, Long switchNum){
	    
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
			
			" while true; do \n" +
			"     sleep 10 \n" +
			"     ping -c 1 8.8.8.8\n" +
			"     if [ \"$?\" == \"0\" ]; then \n" +                                                                                                          
			"        echo Network is up! \n" +
			"        break \n" +
			"     fi \n" +
			"done\n" +
			//"sleep 120 \n" +  //take this out
			
			" /etc/init.d/openvswitch restart \n" +
			" sleep 60 \n" +
			" ovs-vsctl add-br br0 \n" +
			"ovs-vsctl set bridge br0 other-config:hwaddr=" + PriorityNetwork.generateSwitchDPID_mac(switchNum)+ "\n" + 
			
			//"ovs-vsctl set bridge br0 other-config:hwaddr=00:11:00:00:00:0" + Integer.toHexString(switchNum) + "\n" + 
			" ifconfig br0 up \n" +
			" controller_ip='" + SDNControllerIP + "' \n" +
			" echo \"setting ovs to use controller \" ${controller_ip} \n" +
			
			" ovs-vsctl set-controller br0 tcp:${controller_ip}:6633 \n" +
			" ovs-vsctl set controller br0 connection-mode=out-of-band \n" +
			" ovs-appctl fdb/show br0 \n" +
			" ovs-vsctl set Bridge br0 protocols=OpenFlow13 \n" +
			" ovs-vsctl set-manager ptcp:6632 \n" +

			
			
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

	/****************************************************************************
	 * 
	 *   web services functions for communicating with ryu controller 
	 * 
	 * 
	 */
	

	private final String USER_AGENT = "Mozilla/5.0";
	// HTTP GET request
	public  ArrayList<String> getRYUSwitches() throws Exception {
		ArrayList<String> switches = new ArrayList<String>();
		
		System.out.println("this.controllerPublicIP: " + this.controllerPublicIP);
		
		String url = "http://"+this.controllerPublicIP+":8080/stats/switches";

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		System.out.println("Response: XXX" + response.toString() + "XXX");
		
		String delims = "[ ,\\[\\]]+";
		String[] tokens = response.toString().split(delims);
		for (int i = 0; i < tokens.length; i++){
			System.out.println("Tokens: " + tokens[i]);
			if(tokens[i].length() > 0){
				switches.add(tokens[i]);
			}
		}
		    
		//print result
		//System.out.println(response.toString());

		
		return switches;
	}
	
	public String convert2Hex(String switchID){
		Long l =Long.parseLong(switchID);
		return Long.toHexString(l);
			
	}
	
	public  void getRYUSwitchDesc(String switchID) throws Exception {

		String url = "http://" + this.controllerPublicIP + ":8080/stats/desc/" + switchID;

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		System.out.println(response.toString());

	}
	
	// HTTP POST request
	private void sendPost() throws Exception {

		String url = "https://selfsolve.apple.com/wcResults.do";
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		System.out.println(response.toString());

	}

	// HTTP POST request
		private void sendPost_SetOVSDB_addr() throws Exception {
			
			
//			//curl -X PUT -d '"tcp:127.0.0.1:8000”’ http://localhost:8080/v1.0/conf/switches/0000121d02890549/ovsdb_addr
//			
//			
//			String url = "https://"+ this.controllerPublicIP + ":8080/v1.0/conf/switches/"; // + XXX + "/ovsdb_addr";
//			URL obj = new URL(url);
//			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
//
//			//add reuqest header
//			con.setRequestMethod("POST");
//			con.setRequestProperty("User-Agent", USER_AGENT);
//			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
//
//			String urlParameters = "tcp:";         // + switchPublicIP + ":6632";
//
//			// Send post request
//			con.setDoOutput(true);
//			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//			wr.writeBytes(urlParameters);
//			wr.flush();
//			wr.close();
//
//			int responseCode = con.getResponseCode();
//			System.out.println("\nSending 'POST' request to URL : " + url);
//			System.out.println("Post parameters : " + urlParameters);
//			System.out.println("Response Code : " + responseCode);
//
//			BufferedReader in = new BufferedReader(
//			        new InputStreamReader(con.getInputStream()));
//			String inputLine;
//			StringBuffer response = new StringBuffer();
//
//			while ((inputLine = in.readLine()) != null) {
//				response.append(inputLine);
//			}
//			in.close();
//
//			//print result
//			System.out.println(response.toString());

		}
}
