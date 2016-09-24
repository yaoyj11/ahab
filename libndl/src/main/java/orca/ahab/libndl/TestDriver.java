/**
 * 
 */
package orca.ahab.libndl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import orca.ahab.libndl.resources.request.BroadcastNetwork;
import orca.ahab.libndl.resources.request.ComputeNode;
import orca.ahab.libndl.resources.request.Interface;
import orca.ahab.libndl.resources.request.InterfaceNode2Net;
import orca.ahab.libndl.resources.request.Network;
import orca.ahab.libndl.resources.request.Node;
import orca.ahab.libndl.resources.request.StitchPort;
import orca.ahab.libndl.resources.request.StorageNode;
import orca.ahab.libtransport.ISliceTransportAPIv1;
import orca.ahab.libtransport.ITransportProxyFactory;
import orca.ahab.libtransport.JKSTransportContext;
import orca.ahab.libtransport.PEMTransportContext;
import orca.ahab.libtransport.SSHAccessToken;
import orca.ahab.libtransport.SliceAccessContext;
import orca.ahab.libtransport.TransportContext;
import orca.ahab.libtransport.util.SSHAccessTokenFileFactory;
import orca.ahab.libtransport.xmlrpc.XMLRPCProxyFactory;
import orca.ahab.ndllib.transport.OrcaSMXMLRPCProxy;


/**computeElement to group: Workers, newNode: http://geni-orca.renci.org/owl/2ce709f3-bee2-46f0-97da-b6b944aed836#Workers/3

 * @author geni-orca
 *
 */
public class TestDriver {
	public static void sleep(int sec){
		try {
		    Thread.sleep(sec*1000);                 //1000 milliseconds is one second.
		} catch(InterruptedException ex) {  
		    Thread.currentThread().interrupt();
		}
	}
	
	
	public static void blockUntilUp(String pem, String sliceName, String nodeName){
		String SDNControllerIP = null; 
    	while (SDNControllerIP == null){
    		
    	    SDNControllerIP=TestDriver.getPublicIP(pem, sliceName, nodeName);
    	    System.out.println(nodeName + " SDNControllerIP: " + SDNControllerIP);
    	    
    	    if(SDNControllerIP != null) break;
    	    
    	    TestDriver.sleep(30);
    	};
	}
	
	public static void buildSDX(String [] args, int count){
    	String rdf;
    	
    	LIBNDL.setLogger();
    	//int count = 3;
    	String sliceName = "pruth.sdx.1";
    	TestDriver.startSDNSlice_Controller(args[0],sliceName,count);
    	
    	String SDNControllerIP = null; 
    	do{
    		TestDriver.sleep(30);
    	    SDNControllerIP=TestDriver.getPublicIP(args[0], sliceName, "SDNcontroller");
    	    System.out.println("SDNControllerIP: " + SDNControllerIP);
    	} while (SDNControllerIP == null);
    	
    	TestDriver.addSDNSlice_VSDX_v2(args[0],sliceName,count,SDNControllerIP);
    	System.out.println("SDNControllerIP: " + TestDriver.getPublicIP(args[0], sliceName, "SDNcontroller"));
    	
    	//TestDriver.sleep(200);
    	for (int i = 0; i < count; i++){
    		System.out.println("Waiting for sw"+i);
    		TestDriver.blockUntilUp(args[0], sliceName, "sw"+i);
    	}	
    	
    	

    
    	for (int i = 0; i < count; i++){
    		TestDriver.testAddLocalBroadcastNetwork(args[0],sliceName,"sw"+i,"vlan-sw"+i);
    	}
    	TestDriver.sleep(20);
      	for (int i = 0; i < count; i++){
    		for (int j = 0; j < 2; j++){
        		TestDriver.testAddComputeNode2Network(args[0], sliceName, "node"+i+"-"+j, "vlan-sw"+i, "172.16."+i+"."+(100+j), 0);
        	}
    	}
		
	}
	
	public static void deleteAllSDXNetworks(String pem, String sliceName){
		try{
			//ExoGENI controller context
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);
			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL("https://geni.renci.org:11443/orca/xmlrpc"));
				
			Slice s = Slice.loadManifestFile(sliceProxy, sliceName);
	
			for (BroadcastNetwork net : s.getBroadcastLinks()){
				if(net.getName().startsWith("SDX")){
					System.out.println("deleting net: "  + net.getName());
					net.delete();
				}
			}
		
			s.commit();


		} catch  (Exception e){
			e.printStackTrace();
			System.err.println("Proxy factory test failed");
			assert(false);
		}
		
		
	}
	
	public static void setupSDXLinearNetwork(String pem, String sliceName){
		try{
			//ExoGENI controller context
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);
			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL("https://geni.renci.org:11443/orca/xmlrpc"));
		
				
			Slice s = Slice.loadManifestFile(sliceProxy, sliceName);
	
			List<ComputeNode> switches = new ArrayList<ComputeNode>();
			
			for(ComputeNode node : s.getComputeNodes()){
				System.out.println("node: "  + node);
				if(node.getName().startsWith("sw")){
					switches.add(node);
				}
			}
			
	    	for (int i = 1; i < switches.size(); i++){
	    		int parentIndex = i-1;
	    		ComputeNode child = switches.get(i);
	    		ComputeNode parent = switches.get(parentIndex);
	    		System.out.println("Child: " + child.getName() + ", Parent: " + parent.getName());
	    		
	    		TestDriver.testAddNetwork(pem,sliceName,child.getName(),parent.getName(),"SDX-vlan-"+child.getName()+"-"+parent.getName());
	    	}
		
			


		} catch  (Exception e){
			e.printStackTrace();
			System.err.println("Proxy factory test failed");
			assert(false);
		}
		
		
	}
	
	
	public static void setupSDXTreeNetwork(String pem, String sliceName){
		try{
			//ExoGENI controller context
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);
			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL("https://geni.renci.org:11443/orca/xmlrpc"));
		
				
			Slice s = Slice.loadManifestFile(sliceProxy, sliceName);
	
			List<ComputeNode> switches = new ArrayList<ComputeNode>();
			
			for(ComputeNode node : s.getComputeNodes()){
				System.out.println("node: "  + node);
				if(node.getName().startsWith("sw")){
					switches.add(node);
				}
			}
			
	    	for (int i = 1; i < switches.size(); i++){
	    		int parentIndex = (i-1)/2;
	    		ComputeNode child = switches.get(i);
	    		ComputeNode parent = switches.get(parentIndex);
	    		System.out.println("Child: " + child.getName() + ", Parent: " + parent.getName());
	    		
	    		TestDriver.testAddNetwork(pem,sliceName,child.getName(),parent.getName(),"SDX-vlan-"+child.getName()+"-"+parent.getName()); 
	    	}
		
			


		} catch  (Exception e){
			e.printStackTrace();
			System.err.println("Proxy factory test failed");
			assert(false);
		}
		
		
	}
	
	
	public static String geIP(String pem, String sliceName, String nodeName, String netName){
	
		
	
		try{

			//SSH context
			//SliceAccessContext<SSHAccessToken> sctx = new SliceAccessContext<>();
			//SSHAccessTokenFileFactory fac = new SSHAccessTokenFileFactory("/home/geni-orca/.ssh/id_rsa.pub", false);
			//SSHAccessToken t = fac.getPopulatedToken();			
			//sctx.addToken("pruth", "pruth", t);
			//sctx.addToken("pruth", t);

			//ExoGENI controller context
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);
			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL("https://geni.renci.org:11443/orca/xmlrpc"));
		
				
			Slice s = Slice.loadManifestFile(sliceProxy, sliceName);
	
		
			//System.out.println("Slice: " + s.getDebugString());
			System.out.println("Slice: " + s.getSliceGraphString());
			ComputeNode  node = (ComputeNode) s.getResourceByName(nodeName);
			BroadcastNetwork net = (BroadcastNetwork)s.getResourceByName(netName);
				
			System.out.println("node: " + node);
			System.out.println("net: " + net);
			
			//InterfaceNode2Net iface =  s.getInterfaces();
			InterfaceNode2Net iface = (InterfaceNode2Net) node.getInterface(net);
			
			String ip = iface.getIpAddress();
			System.out.println("Get IP:  " + ip);
		
			System.out.println("Interface: " + iface);


		} catch  (Exception e){
			e.printStackTrace();
			System.err.println("Proxy factory test failed");
			assert(false);
		}
		return "";
	}
	
	public static String getPublicIP(String pem, String sliceName, String nodeName){
		
		
		
		try{

			//SSH context
			//SliceAccessContext<SSHAccessToken> sctx = new SliceAccessContext<>();
			//SSHAccessTokenFileFactory fac = new SSHAccessTokenFileFactory("/home/geni-orca/.ssh/id_rsa.pub", false);
			//SSHAccessToken t = fac.getPopulatedToken();			
			//sctx.addToken("pruth", "pruth", getManagt);
			//sctx.addToken("pruth", t);

			//ExoGENI controller context
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);
			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL("https://geni.renci.org:11443/orca/xmlrpc"));
		
				
			Slice s = Slice.loadManifestFile(sliceProxy, sliceName);
	
		
			//System.out.println("Slice: " + s.getDebugString());
			//System.out.println("Slice: " + s.getSliceGraphString());
			ComputeNode  node = (ComputeNode) s.getResourceByName(nodeName);
				
			//System.out.println("node: " + node);
			
			//InterfaceNode2Net iface =  s.getInterfaces();
			
			//List<String> services = node.getManagmentServices();
			//System.out.println("Get services:  " + services);
		
			//System.out.println("Get ip:  " + node.getManagementIP());
			
			return node.getManagementIP();
		} catch  (Exception e){
			e.printStackTrace();
			System.err.println("Proxy factory test failed");
			assert(false);
		}
		return "";
	}
	
	public static void main(String [] args){
		
		LIBNDL.setLogger();
		
    	System.out.println("ndllib TestDriver: START");
    	
    	//String sliceName = "pruth.sdx.1";
    	//TestDriver.buildSDX(args,10);
    	
    	
    	TestDriver.deleteAllSDXNetworks(args[0], "pruth.sdx.1");
    	//TestDriver.setupSDXTreeNetwork(args[0],"pruth.sdx.1" );
    	TestDriver.setupSDXLinearNetwork(args[0],"pruth.sdx.1" );
    	//TestDriver.getPublicIP(args[0], "pruth.sdn.103", "sw3");
    	

    	//TestDriver.testAddComputeNode(args[0], sliceName, "node1", 0);
    	//TestDriver.testAddComputeNode(args[0], sliceName, "node2", 0);
    	//TestDriver.testAddComputeNode(args[0], sliceName, "node3", 0);
    	
    	//rdf = TestDriver.testAddNetwork(args[0],sliceName,"sw0","node0","vlan-sw0");
    	//rdf = TestDriver.testAddNetwork(args[0],sliceName,"sw1","node1","vlan-sw1");
    	//rdf = TestDriver.testAddNetwork(args[0],sliceName,"sw2","node2","vlan-sw2");
    	//rdf = TestDriver.testAddNetwork(args[0],sliceName,"sw3","node3","vlan-sw3");
    	//System.out.println(rdf);
    	
    	//rdf = TestDriver.testAddNetwork(args[0],"pruth.2","sw2","sw3","vlan23");
    	//System.out.println(rdf);
    	
    	
    	//testDeleteComputeNode(args[0],"Node0");
    	//testAddComputeNode(args[0],"Node3");
    	//testDeleteComputeNode(args[0],"Node1");
    	//testLibtransport(args[0]);
    	//testLoad(args[0],"pruth.1");
    	//TestDriver.testAddNetwork(args[0], "Node0", "Node1", "VLAN0");

    	//TestDriver.testNewSlice2(args[0], "pruth.10");
    	//TestDriver.testDeleteLink1(args[0]);
    	//TestDriver.testDeleteNetwork(args[0],"VLAN0");
    	//TestDriver.testDelete(args[0], "pruth.slice1");
    	
    	
    	//testSave();
    	//testLoadAndSave();
    	//testLoadManifest();
    	//adamantTest1();
    	//adamantTest2();
    	//adamantTest3();
    	///autoIP1();
    	System.out.println("ndllib TestDriver: END");
    	
	}
	
	public static void startSDNSlice_Controller(String pem, String sliceName, int count){
		//String sliceName="pruth.sdn.2";
		
		String controllerImageShortName="Centos7-SDN-controller.v0.4";
		String controllerImageURL ="http://geni-images.renci.org/images/pruth/SDN/Centos7-SDN-controller.v0.4/Centos7-SDN-controller.v0.4.xml";
		String controllerImageHash ="b71cbdbd8de5b2d187ae9a3efb0a19a170b92183";
		String controllerDomain="RENCI (Chapel Hill, NC USA) XO Rack";
		String contorllerNodeType="XO Medium";
		String controllerPostBootScript="#!/bin/bash\n echo hello, world > /tmp/bootscript.log";
		
		try{
			//SSH context
			SliceAccessContext<SSHAccessToken> sctx = new SliceAccessContext<>();
			SSHAccessTokenFileFactory fac = new SSHAccessTokenFileFactory("/home/geni-orca/.ssh/id_rsa.pub", false);
			SSHAccessToken t = fac.getPopulatedToken();			
			sctx.addToken("root", "root", t);
			sctx.addToken("root", t);
			
			//ExoGENI controller context
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);
			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL("https://geni.renci.org:11443/orca/xmlrpc"));
			Slice s = Slice.create(sliceProxy, sctx, sliceName);
		
			ComputeNode   controllerNode = s.addComputeNode("SDNcontroller");
			controllerNode.setImage(controllerImageURL,controllerImageHash,controllerImageShortName);
			controllerNode.setNodeType(contorllerNodeType);
			controllerNode.setDomain(controllerDomain);
			controllerNode.setPostBootScript(controllerPostBootScript);
		
			
			String switchImageShortName="Centos6.7-SDN.v0.1";
			String switchImageURL ="http://geni-images.renci.org/images/pruth/SDN/Centos6.7-SDN.v0.1/Centos6.7-SDN.v0.1.xml";
			String switchImageHash ="77ec2959ff3333f7f7e89be9ad4320c600aa6d77";
			String switchDomain="RENCI (Chapel Hill, NC USA) XO Rack";
			String switchNodeType="XO Medium";
			String switchPostBootScript=getSDNControllerScript();
			//String switchPostBootScript="switch boot script";
			
//			ArrayList<ComputeNode> switches = new ArrayList<ComputeNode>();
//			for (int i = 0; i < count; i++){
//				ComputeNode  sw = s.addComputeNode("sw"+i);
//				sw.setImage(switchImageURL,switchImageHash,switchImageShortName);
//				sw.setNodeType(switchNodeType);
//				sw.setDomain(domains.get(i));
//				sw.setPostBootScript(switchPostBootScript);
//				switches.add(i,sw);
//			}
			
			s.commit();
		} catch (Exception e){
			e.printStackTrace();
			System.err.println("Proxy factory test failed");
			assert(false);
		}

	}
	
	public static void addSDNSlice_VSDX(String pem, String sliceName, int count, String SDNControllerIP){
		//String sliceName="pruth.sdn.2";	
		
		String switchImageShortName="Centos6.7-SDN.v0.1";
		String switchImageURL ="http://geni-images.renci.org/images/pruth/SDN/Centos6.7-SDN.v0.1/Centos6.7-SDN.v0.1.xml";
		String switchImageHash ="77ec2959ff3333f7f7e89be9ad4320c600aa6d77";
		String switchDomain="RENCI (Chapel Hill, NC USA) XO Rack";
		String switchNodeType="XO Medium";
		//String switchPostBootScript=SDN_SWITCH_SCRIPT;
		String switchPostBootScript=getSDNSwitchScript(SDNControllerIP);
		
		
		try{
			//SSH context
			SliceAccessContext<SSHAccessToken> sctx = new SliceAccessContext<>();
			SSHAccessTokenFileFactory fac = new SSHAccessTokenFileFactory("/home/geni-orca/.ssh/id_rsa.pub", false);
			SSHAccessToken t = fac.getPopulatedToken();			
			sctx.addToken("pruth", "pruth", t);
			sctx.addToken("pruth", t);
			
			//ExoGENI controller context
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);
			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL("https://geni.renci.org:11443/orca/xmlrpc"));
			
			Slice s = Slice.loadManifestFile(sliceProxy, sliceName);
			
			ArrayList<ComputeNode> switches = new ArrayList<ComputeNode>();
			for (int i = 0; i < count; i++){
				ComputeNode  sw = s.addComputeNode("sw2-"+i);
				sw.setImage(switchImageURL,switchImageHash,switchImageShortName);
				sw.setNodeType(switchNodeType);
				sw.setDomain(domains.get(0));
				sw.setPostBootScript(switchPostBootScript);
				switches.add(i,sw);
		

//				if(i != 0){
//				//if ( i%2 != 0){
//					int parent = (i-1)/2;
//					//int parent = i-1;
//					BroadcastNetwork net = s.addBroadcastLink("VLAN-"+parent+"-"+i);
//					net.stitch(switches.get(parent));
//					net.stitch(switches.get(i));
//					
//				}
			}
			
			System.out.println("REQUEST: \n" + s.getRequest());
			
			s.commit();
		} catch (Exception e){
			e.printStackTrace();
			System.err.println("Proxy factory test failed");
			assert(false);
		}

	}
	
	public static void addSDNSlice_VSDX_v2(String pem, String sliceName, int count, String SDNControllerIP){
		//String sliceName="pruth.sdn.2";	
		
		String switchImageShortName="Centos6.7-SDN.v0.1";
		String switchImageURL ="http://geni-images.renci.org/images/pruth/SDN/Centos6.7-SDN.v0.1/Centos6.7-SDN.v0.1.xml";
		String switchImageHash ="77ec2959ff3333f7f7e89be9ad4320c600aa6d77";
		String switchDomain="RENCI (Chapel Hill, NC USA) XO Rack";
		String switchNodeType="XO Medium";
		String switchPostBootScript=getSDNSwitchScript(SDNControllerIP);
		//String switchPostBootScript="switch boot script";
	
		try{

			//SSH context
			SliceAccessContext<SSHAccessToken> sctx = new SliceAccessContext<>();
			SSHAccessTokenFileFactory fac = new SSHAccessTokenFileFactory("/home/geni-orca/.ssh/id_rsa.pub", false);
			SSHAccessToken t = fac.getPopulatedToken();			
			sctx.addToken("pruth", "pruth", t);
			sctx.addToken("pruth", t);

			//ExoGENI controller context
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);
			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL("https://geni.renci.org:11443/orca/xmlrpc"));
			




			//setup the nodes
			try{
				
				Slice s = Slice.loadManifestFile(sliceProxy, sliceName);

				ArrayList<ComputeNode> switches = new ArrayList<ComputeNode>();
				for (int i = 0; i < count; i++){
					ComputeNode  sw = s.addComputeNode("sw"+i);
					sw.setImage(switchImageURL,switchImageHash,switchImageShortName);
					sw.setNodeType(switchNodeType);
					sw.setDomain(domains.get(0));
					sw.setPostBootScript(switchPostBootScript);
					//switches.add(i,sw);
				}
				System.out.println("Commiting: " + s.getDebugString());
				
				//System.out.println("REQUEST: \n" + s.getRequest());
				s.commit();

				TestDriver.sleep(10);

			} catch (Exception e){
				e.printStackTrace();
				System.err.println("Proxy factory test failed");
				assert(false);
			}

//			//Setup the networks
//			try{
//				
//				
//				for (int i = 0; i < count; i++){
//					System.out.println("Adding network " + i);
//					Slice s = Slice.loadManifestFile(sliceProxy, sliceName);
//
//					
//					
//
//					s.logger().debug("******************** START Slice Info " + s.getName() + " *********************");
//					//s.logger().debug(s.getRequest());
//					s.logger().debug(s.getDebugString());
//					s.logger().debug("******************** END PRINTING *********************");
//					
//					
//					//if(i != 0){
//						if ( i%2 != 0){
//						//int parent = (i-1)/2;
//						int parent = i-1;
//						
//						s.logger().debug("i: " + i + ", parent: " + parent);
//				
//						BroadcastNetwork net = s.addBroadcastLink("VLAN"+i);
//
//						ComputeNode node1 = (ComputeNode)s.getResourceByName("sw"+i);
//						ComputeNode node2 = (ComputeNode)s.getResourceByName("sw"+parent);
//
//						s.logger().debug("net: " + net);
//						s.logger().debug("node1: " + node1);
//						s.logger().debug("node2: " + node2);
//						
//						net.stitch(node1);
//						net.stitch(node2);
//
//						
//						s.logger().debug("******************** START Slice Info " + s.getName() + " *********************");
//						s.logger().debug(s.getRequest());
//						//s.logger().debug(s.getDebugString());
//						s.logger().debug("******************** END PRINTING *********************");
//						
//
//						//System.out.println("REQUEST: \n" + s.getRequest());
//						
//						TestDriver.sleep(10);
//						s.commit();
//						}
//					
//					
//
//				}
//				
//			} catch (Exception e){
//				e.printStackTrace();
//				System.err.println("Proxy factory test failed");
//				assert(false);
//			}


		} catch  (Exception e){
			e.printStackTrace();
			System.err.println("Proxy factory test failed");
			assert(false);
		}



	}
	
	public static void testNewSlice1(String pem){
		try{
	
			
			SliceAccessContext<SSHAccessToken> sctx = new SliceAccessContext<>();

			SSHAccessTokenFileFactory fac = new SSHAccessTokenFileFactory("/home/geni-orca/.ssh/id_rsa.pub", false);
			SSHAccessToken t = fac.getPopulatedToken();

			//fac = new SSHAccessTokenFileFactory("/home/geni-orca/.ssh/id_rsa.pub", false);
			//SSHAccessToken t1 = fac.getPopulatedToken();
			
			sctx.addToken("pruth", "pruth", t);
			//sctx.addToken("testuser", "some1", t1);
			
			sctx.addToken("pruth", t);
			
			System.out.println(sctx);
		
		ITransportProxyFactory ifac = new XMLRPCProxyFactory();
		System.out.println("Opening certificate " + pem + " and key " + pem);
		TransportContext ctx = new PEMTransportContext("", pem, pem);

		ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL("https://geni.renci.org:11443/orca/xmlrpc"));
		Slice s = Slice.create(sliceProxy, sctx, "pruth.slice1");
		
		for (int i = 0; i < 16; i++){
			ComputeNode   newnode = s.addComputeNode("ComputeNode"+i);
			newnode.setImage("http://geni-images.renci.org/images/standard/centos/centos6.3-v1.0.11.xml","776f4874420266834c3e56c8092f5ca48a180eed","PRUTH-centos");
			newnode.setNodeType("XO Large");
			newnode.setDomain("RENCI (Chapel Hill, NC USA) XO Rack");
			newnode.setPostBootScript("master post boot script");
			
		}
			
		
		
		System.out.println("testNewSlice1: " + s.getDebugString());
		
		System.out.println("testNewSlice1: " + s.getRequest());
		
		s.commit();
		} catch (Exception e){
			e.printStackTrace();
			System.err.println("Proxy factory test failed");
			assert(false);
		}
		
	}
	
	public static void testNewSlice2(String pem, String sliceName){
		String controllerImageShortName="Centos7-SDN-controller.v0.4";
		String controllerImageURL ="http://geni-images.renci.org/images/pruth/SDN/Centos7-SDN-controller.v0.4/Centos7-SDN-controller.v0.4.xml";
		String controllerImageHash ="b71cbdbd8de5b2d187ae9a3efb0a19a170b92183";
		//String controllerDomain="RENCI (Chapel Hill, NC USA) XO Rack";
		String contorllerNodeType="XO Medium";
		String controllerPostBootScript="#!/bin/bash\n echo hello, world > /tmp/bootscript.log";
		
		try{
			//SSH context
			SliceAccessContext<SSHAccessToken> sctx = new SliceAccessContext<>();
			SSHAccessTokenFileFactory fac = new SSHAccessTokenFileFactory("/home/geni-orca/.ssh/id_rsa.pub", false);
			SSHAccessToken t = fac.getPopulatedToken();			
			sctx.addToken("root", "root", t);
			sctx.addToken("root", t);
			
			//ExoGENI controller context
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);
			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL("https://geni.renci.org:11443/orca/xmlrpc"));
			Slice s = Slice.create(sliceProxy, sctx, sliceName);
		
			ComputeNode   Node1 = s.addComputeNode("Node1");
			Node1.setImage(controllerImageURL,controllerImageHash,controllerImageShortName);
			Node1.setNodeType(contorllerNodeType);
			Node1.setDomain(domains.get(1));
			Node1.setPostBootScript(controllerPostBootScript);
		
			
			ComputeNode   Node2 = s.addComputeNode("Node2");
			Node2.setImage(controllerImageURL,controllerImageHash,controllerImageShortName);
			Node2.setNodeType(contorllerNodeType);
			Node2.setDomain(domains.get(2));
			Node2.setPostBootScript(controllerPostBootScript);

			
			
			
			BroadcastNetwork net = s.addBroadcastLink("VLAN0");
			InterfaceNode2Net i1 = (InterfaceNode2Net) net.stitch(Node1);
			InterfaceNode2Net i2 = (InterfaceNode2Net) net.stitch(Node2);
			
			i1.setIpAddress("172.16.1.1");
			i2.setIpAddress("172.16.1.2");
			
			
			s.commit();
		} catch (Exception e){
			e.printStackTrace();
			System.err.println("Proxy factory test failed");
			assert(false);
		}

	
		

		
	
		
	}
	
	public static void testLibtransport(String pem){
		try {
//			Logger logger = Logger.getRootLogger();//Logger.getLogger("org.apache.commons.httpclient.HttpClient");
//			logger.setLevel(Level.DEBUG);
//			ConsoleAppender capp = new ConsoleAppender();
//			capp.setImmediateFlush(true);
//			capp.setName("Test Console Appender");
//			org.apache.log4j.SimpleLayout sl = new SimpleLayout();
//			capp.setLayout(sl);
//			capp.setWriter(new PrintWriter(System.out));
//			logger.addAppender(capp);
			
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);

			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL("https://geni.renci.org:11443/orca/xmlrpc"));
			
			//System.out.println(sliceProxy.getVersion());
			for (String str : sliceProxy.listMySlices()){
				System.out.println(str);
			}
			
			String manifest = sliceProxy.sliceStatus("pruth.101");
			//System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			//System.out.println(manifest);
			//System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			
			Slice s = Slice.loadManifest(manifest);
			System.out.println("Slice pruth.101 = " + s.getDebugString());
			
			ComputeNode   newnode = s.addComputeNode("ComputeNode0");
			newnode.setImage("http://geni-images.renci.org/images/standard/centos/centos6.3-v1.0.11.xml","776f4874420266834c3e56c8092f5ca48a180eed","PRUTH-centos");
			newnode.setNodeType("XO Large");
			newnode.setDomain("RENCI (Chapel Hill, NC USA) XO Rack");
			newnode.setPostBootScript("master post bootnew Slice(); script");
			
			System.out.println("Slice pruth.101 = " + s.getDebugString());
			
			//System.out.println("Request: " + s.getRequest());
			
			sliceProxy.modifySlice("pruth.101", s.getRequest());
			
			// now you can do things like 
			//sliceProxy.getVersion();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Proxy factory test failed");
			assert(false);
		}
		System.out.println("Proxy factory test succeeded");
	}
	
	
//	public static  void autoIP1(){
//		Slice s = new Slice();
//		s.logger().debug("START: autoIP1");
//((InterfaceNode2Net)int1).setNetmask("255.255.255.0");
//		try{
//			IP4Assign ipa = new IP4Assign();
//			IP4Subnet subnet5 = ipa.getSubnet(		s.logger().debug("******************** START Slice Info " + s.getName() + " *********************");
	//s.logger().debug(s.getRequest());
//			IP4Subnet subnet6 = ipa.getSubnet((Inet4Address)InetAddress.getByName("192.168.0.0"),24);
//			IP4Subnet subnet1 = ipa.getAvailableSubnet(300);
//			IP4Subnet subnet2 = ipa.getAvailableSubnet(100);
//			IP4Subnet subnet3 = ipa.getAvailableSubnet(600);
//			IP4Subnet subnet4 = ipa.getAvailableSubnet(100);
//			
//
//			s.logger().debug("subnet1: \n" + subnet1.toString());
//			s.logger().debug("subnet2: \n" + subnet2.toString());
//			s.logger().debug("subnet3: \n" + subnet3.toString());
//			s.logger().debug("subnet4: \n" + subnet4.toString());
//			s.logger().debug("subnet5: \n" + subnet5.toString());
//			s.logger().debug("subnet6: \n" + subnet6.toString());
//			
//			s.logger().debug("Subnet1 IP: " + subnet1.getFreeIP());
//			s.logger().debug("Subnet1 IP: " + subnet1.getFreeIPs(10));
//			s.logger().debug("Subnet1 IP: " + subnet1.getFreeIP());
//			
//		} catch (Exception e){
//			;
//		}
//	}
//	
//	public static  void test1(){
//		
//		System.out.println("ndllib TestDriver: test1");
//		Slice r = new Slice();
//		
//		
//		ComputeNode   cn = r.addComputeNode("ComputeNode0");
//		StorageNode   sn = r.addStorageNode("StorageNode0");
//		StitchPort    sp = r.addStitchPort("StitchPort0");
//		Network           l = r.addLink("Link0");
//		BroadcastNetwork bl = r.addBroadcastLink("BcastLink0");
//		
//		Interface stitch = bl.stitch(cn);
//		sn.stitch(l);
//		sp.stitch(bl);
//		
//		System.out.println("Output:");
//		System.out.println("Request: \n" + r.getRequestString());
//		
//		System.out.println("\nNodes:");
//		System.out.println("ComputeNode:  " + cn);
//		System.out.println("StorageNode:  " + sn);
//		System.out.println("StitchPort:   " + sp);
//		System.out.println("Link:         " + l);
//		System.out.println("BcastLink:    " + bl);
//		
//		System.out.println("\nStitches:");
//		System.out.println("Stitch:  " + stitch);
//	}
//	s
	public static void testLoad(String pem, String sliceName){
		Slice s = null;
		try{
		
			//r.logger("ndllib TestDriver: testLoad");
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);

			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL	("https://geni.renci.org:11443/orca/xmlrpc"));

			s = Slice.loadManifestFile(sliceProxy, sliceName);
		} catch (Exception e){
			s.logger().debug("Failed to fetch manifest");
			return;
		}
		

	
		System.out.println("******************** START Slice Info " + s.getName() + " *********************");
		//System.out.println(s.getDebugString());
		System.out.println(s.getSliceGraphString());
		System.out.println("******************** END PRINTING *********************");
		
		}
	
	public static void ComputeNode(String pem, String delNodeName){
		Slice s = null;
		try{
		
			//r.logger("ndllib TestDriver: testLoad");
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);

			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL	("https://geni.renci.org:11443/orca/xmlrpc"));

			s = Slice.loadManifestFile(sliceProxy, "pruth.1");
		} catch (Exception e){
			s.logger().debug("Failed to fetch manifest");
			return;
		}
		
		
		
		//Slice s = Slice.loadRequestFile("/home/geni-orca/test-rdfs/request-test1.rdf");
		//Slice s = Slice.loadManifestFile("/home/geni-orca/test-rdfs/manifest-test1.rdf");
		//Slice s = Slice.loadManifestFile("/home/geni-orca/test-rdfs/newest-test-manifest-up-1failed.rdf");
		//Slice s = Slice.loadManifestFile("/home/geni-orca/test-rdfs/newest-test-manifest-halfup.rdf");
		
		//s.load("/home/geni-orca/test-rdfs/test1.rdf");
		//s.loadFile("/home/geni-orca/test-rdfs/test1.rdf");
	
		s.logger().debug("******************** START Slice Info " + s.getName() + " *********************");
		//s.logger().debug(s.getRequest());
		s.logger().debug(s.getDebugString());
		s.logger().debug("******************** END PRINTING *********************");
		
		
		
		
		ComputeNode cn = (ComputeNode)s.getResourceByName(delNodeName);
		s.logger().debug("cn: " + cn);
		s.logger().debug("compute node: " + cn.getName());
		s.logger().debug("compute node: " + cn.getDomain());
		s.logger().debug("compute node: " + cn.getNodeType());
		s.logger().debug("compute node: " + cn.getImageShortName());
		s.logger().debug("compute node: " + cn.getImageHash());
		s.logger().debug("compute node: " + cn.getImageUrl());
		s.logger().debug("compute node: " + cn.getPostBootScript());
		
		//s.logger().debug("Interfaces:  " + cn.getInterfaces());
		
		cn.delete();
		
		s.commit();
	}
	
	public static void testDelete(String pem, String sliceName){
		Slice s = null;
		try{
		
			//r.logger("ndllib TestDriver: testLoad");
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);

			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL	("https://geni.renci.org:11443/orca/xmlrpc"));

			s = Slice.loadManifestFile(sliceProxy, sliceName);
		} catch (Exception e){
			s.logger().debug("Failed to fetch manifest");
			return;
		}
		
		s.delete();
		
	}
	
	
	public static void testAddComputeNode2Network(String pem, String sliceName, String newNodeName, String netName, String ip, int domain){

	
		
		
		String switchImageShortName="Centos6.7-SDN.v0.1";
		String switchImageURL ="http://geni-images.renci.org/images/pruth/SDN/Centos6.7-SDN.v0.1/Centos6.7-SDN.v0.1.xml";
		String switchImageHash ="77ec2959ff3333f7f7e89be9ad4320c600aa6d77";
		String switchDomain=domains.get(domain);
		String switchNodeType="XO Medium";
		//String switchPostBootScript=SDN_SWITCH_SCRIPT;
		String switchPostBootScript="switch boot script";
	
		try{

			//SSH context
			SliceAccessContext<SSHAccessToken> sctx = new SliceAccessContext<>();
			SSHAccessTokenFileFactory fac = new SSHAccessTokenFileFactory("/home/geni-orca/.ssh/id_rsa.pub", false);
			SSHAccessToken t = fac.getPopulatedToken();			
			sctx.addToken("pruth", "pruth", t);
			sctx.addToken("pruth", t);

			//ExoGENI controller context
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);
			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL("https://geni.renci.org:11443/orca/xmlrpc"));
		

			//setup the nodes
			try{
				
				Slice s = Slice.loadManifestFile(sliceProxy, sliceName);

				ComputeNode  node = s.addComputeNode(newNodeName);
				node.setImage(switchImageURL,switchImageHash,switchImageShortName);
				node.setNodeType(switchNodeType);
				node.setDomain(switchDomain);
				node.setPostBootScript(switchPostBootScript);
	
				BroadcastNetwork net = (BroadcastNetwork)s.getResourceByName(netName); 
				
				s.logger().debug("BroadcastNetwork net = " + net);
			
				Interface int1  = net.stitch(node);
				
				s.logger().debug("Interface int1 = " + int1);
				
				((InterfaceNode2Net)int1).setIpAddress(ip);
				//((InterfaceNode2Net)int1).setNetmask("255.255.255.0");
				
				s.logger().debug("AddNode2Net request:  " + s.getRequest());
				
				s.commit();
			} catch (Exception e){
				e.printStackTrace();
				System.err.println("Proxy factory test failed");
				assert(false);
			}



		} catch  (Exception e){
			e.printStackTrace();
			System.err.println("Proxy factory test failed");
			assert(false);
		}


		
	}
	
	
	public static String testAddLocalBroadcastNetwork(String pem, String sliceName, String nodeName, String networkName){
		Slice s = null;
		try{
		
			//r.logger("ndllib TestDriver: testLoad");
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);

			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL	("https://geni.renci.org:11443/orca/xmlrpc"));

			s = Slice.loadManifestFile(sliceProxy, sliceName);
		} catch (Exception e){
			s.logger().debug("Failed to fetch manifest");
			return "error";
		}
		
			
		s.logger().debug("******************** START Before Slice Info " + s.getName() + " *********************");
		//s.logger().debug(s.getRequest());
		//s.logger().debug(s.getDebugString());
		s.logger().debug("******************** END Before  *********************");
		
		ComputeNode node1 = (ComputeNode)s.getResourceByName(nodeName);
		
		
		//ComputeNode   node2 = s.addComputeNode("ComputeNode2");
		//node2.setImage("http://geni-images.renci.org/images/standard/centos/centos6.3-v1.0.11.xml","776f4874420266834c3e56c8092f5ca48a180eed","PRUTH-centos");
		//node2.setNodeType("XO Large");
		//node2.setDomain("RENCI (Chapel Hill, NC USA) XO Rack");
		//node2.setPostBootScript("master post boot script");
		
		
		BroadcastNetwork net = s.addBroadcastLink(networkName);
		Interface int1 = net.stitch(node1);
		//Interface int2 = net.stitch(node2);
	
		//((InterfaceNode2Net)int1).setIpAddress("172.16.1.1");
		//((InterfaceNode2Net)int1).setNetmask("255.255.255.0");
		
		//((InterfaceNode2Net)int2).setIpAddress("172.16.1.2");
		//((InterfaceNode2Net)int2).setNetmask("255.255.255.0");
			
		s.logger().debug("******************** START After Slice Info " + s.getName() + " *********************");
		//s.logger().debug(s.getRequest());
		//s.logger().debug(s.getDebugString());
		s.logger().debug("******************** END After  *********************");
		
		String rdfString = s.getRequest();
		
		s.commit();
		
		return rdfString;
	}
	
	
	public static void testAddComputeNode(String pem, String sliceName, String newNodeName, int domain){

		
		
		
		String switchImageShortName="Centos6.7-SDN.v0.1";
		String switchImageURL ="http://geni-images.renci.org/images/pruth/SDN/Centos6.7-SDN.v0.1/Centos6.7-SDN.v0.1.xml";
		String switchImageHash ="77ec2959ff3333f7f7e89be9ad4320c600aa6d77";
		String switchDomain=domains.get(domain);
		String switchNodeType="XO Medium";
		//String switchPostBootScript=SDN_SWITCH_SCRIPT;
		String switchPostBootScript="switch boot script";
	
		try{

			//SSH context
			SliceAccessContext<SSHAccessToken> sctx = new SliceAccessContext<>();
			SSHAccessTokenFileFactory fac = new SSHAccessTokenFileFactory("/home/geni-orca/.ssh/id_rsa.pub", false);
			SSHAccessToken t = fac.getPopulatedToken();			
			sctx.addToken("pruth", "pruth", t);
			sctx.addToken("pruth", t);

			//ExoGENI controller context
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);
			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL("https://geni.renci.org:11443/orca/xmlrpc"));
		

			//setup the nodes
			try{
				
				Slice s = Slice.loadManifestFile(sliceProxy, sliceName);

				ArrayList<ComputeNode> switches = new ArrayList<ComputeNode>();
		
				ComputeNode  sw = s.addComputeNode(newNodeName);
				sw.setImage(switchImageURL,switchImageHash,switchImageShortName);
				sw.setNodeType(switchNodeType);
				sw.setDomain(switchDomain);
				sw.setPostBootScript(switchPostBootScript);
	
				s.commit();
			} catch (Exception e){
				e.printStackTrace();
				System.err.println("Proxy factory test failed");
				assert(false);
			}



		} catch  (Exception e){
			e.printStackTrace();
			System.err.println("Proxy factory test failed");
			assert(false);
		}


		
	}
	
	
	public static String testAddNetwork(String pem, String sliceName, String node1Name, String node2Name, String networkName){
		Slice s = null;
		try{
		
			//r.logger("ndllib TestDriver: testLoad");
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);

			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL	("https://geni.renci.org:11443/orca/xmlrpc"));

			s = Slice.loadManifestFile(sliceProxy, sliceName);
		} catch (Exception e){
			s.logger().debug("Failed to fetch manifest");
			return "error";
		}
		
		
		ComputeNode node1 = (ComputeNode)s.getResourceByName(node1Name);
		ComputeNode node2 = (ComputeNode)s.getResourceByName(node2Name);
	
		BroadcastNetwork net = s.addBroadcastLink(networkName);
		Interface int1 = net.stitch(node1);
		Interface int2 = net.stitch(node2);
		
		String rdfString = s.getRequest();
		
		s.commit();
		
		return rdfString;
	}
	
	public static void testDeleteNetwork(String pem, String networkName){
		Slice s = null;
		try{
		
			//r.logger("ndllib TestDriver: testLoad");
			ITransportProxyFactory ifac = new XMLRPCProxyFactory();
			System.out.println("Opening certificate " + pem + " and key " + pem);
			TransportContext ctx = new PEMTransportContext("", pem, pem);

			ISliceTransportAPIv1 sliceProxy = ifac.getSliceProxy(ctx, new URL	("https://geni.renci.org:11443/orca/xmlrpc"));

			s = Slice.loadManifestFile(sliceProxy, "pruth.slice1");
		} catch (Exception e){
			s.logger().debug("Failed to fetch manifest");
			return;
		}
		
		
		BroadcastNetwork net = (BroadcastNetwork)s.getResourceByName(networkName);
		net.delete();
	
		s.commit();
		
	}	
/*	
	public static void testSave(){
		Slice r = new Slice();
		
		ComputeNode cn = r.addComputeNode("Node42");
		cn.setImage("http://geni-images.renci.org/images/standard/centos/centos6.3-v1.0.11.xml","776f4874420266834c3e56c8092f5ca48a180eed","PRUTH-centos");
		cn.setNodeType("XO Large");
		cn.setDomain("RENCI (Chapel Hill, NC USA) XO Rack");
		cn.setPostBootScript("post boot script");
		
		r.logger().debug(r.getRequestString());
		for (Node node : r.getNodes()){
			r.logger().debug("PRUTH:" + node);
		}
		
		for (Network link : r.getLinks()){
			r.logger().debug("PRUTH:" + link);
		}
		
		
		r.save("/home/geni-orca/test-requests/test-save.rdf");
		
	}
	
	public static void testLoadAndSave(){
		Slice s = new Slice();
		
		
		s.loadFile("/home/geni-orca/test-requests/test-load-request.rdf");
		
		printRequest2Log(s);
		
		s.save("/home/geni-orca/test-requests/test-save-request.rdf");
		
		
	}
	
	public static void testLoadManifest(){s
		Slice s = new Slice();
		s.logger().debug("testLoadManifest");
		s.loadFile("/home/geni-orca/test-requests/test-load-manifest.rdf");
		
		
		
		s.logger().debug("******************** START REQUEST *********************");
		s.logger().debug(s.getRequestString());
		
		s.logger().debug("******************** START MANIFEST *********************");
		//s.logger().debug(s.getManifestString());
		
		//s.logger().debug("******************** END PRINTING *********************");
	}

	public static String readRDF(String fileName){
		BufferedReader bin = null; 
		StringBuilder sb = null;
		try {
			FileInputStream is = new FileInputStream(fileName);
			bin = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			
			sb = new StringBuilder();
			String line = null;// parse as request
			
			while((line = bin.readLine()) != null) {
				sb.append(line);
				// re-add line separator
				sb.append(System.getProperty("line.separator"));
			}
			
			bin.close();

		} catch (Exception e) {
			return "Error Reading " + fileName;
		} 
		
		return sb.toString();
		
	}
	*/
	
	/** 
	 *  Test Case 1
	 *  
	 *  1.  Create a new slices
	 *  2. 	Add nodes/links to create a pegasus/condor slice
	 *  3.  Get the rdf
	 */
	
	
//	public static void adamantTest1(){
//		Slice s = new Slice();
//		s.logger().debug("adamantTest1: ");
//		
//		s.logger().debug("Domains: " + s.getDomains());
//		
//		ComputeNode master     = s.addComputeNode("Master");
//		ComputeNode workers    = s.addComputeNode("Workers");
//		StitchPort  data       = s.addStitchPort("Data");
//		StorageNode storage    = s.addStorageNode("Storage");
//		BroadcastNetwork net   = s.addBroadcastLink("DataNetwork");
//		BroadcastNetwork storageNet   = s.addBroadcastLink("StorageNetwork");
//		
//		InterfaceNode2Net masterIface  = (InterfaceNode2Net) net.stitch(master);
//		InterfaceNode2Net workersIface = (InterfaceNode2Net) net.stitch(workers);
//		InterfaceNode2Net dataIface    = (InterfaceNode2Net) net.stitch(data);
//
//		InterfaceNode2Net masterStorageIface  = (InterfaceNode2Net) storageNet.stitch(master);
//		InterfaceNode2Net storageStorageIface  = (InterfaceNode2Net) storageNet.stitch(storage);
//		
//		
//		master.setImage("http://geni-images.renci.org/images/standard/centos/centos6.3-v1.0.11.xml","776f4874420266834c3e56c8092f5ca48a180eed","PRUTH-centos");
//		master.setNodeType("XO Large");
//		master.setDomain("RENCI (Chapel Hill, NC USA) XO Rack");
//		master.setPostBootScript("master post boot script");
//		
//		workers.setImage("http://geni-images.renci.org/images/standard/centos/centos6.3-v1.0.11.xml","776f4874420266834c3e56c8092f5ca48a180eed","PRUTH-centos");
//		workers.setNodeType("XO Large");
//		workers.setDomain("UH (Houston, TX USA) XO Rack");
//		workers.setPostBootScript("worker post boot script");
//		workers.setNodeCount(2);
//		workers.setMaxNodeCount(13);
//			
//		data.setLabel("1499");
//		data.setPort("http://geni-orca.renci.org/owl/ben-6509.rdf#Renci/Cisco/6509/TenGigabitEthernet/3/4/ethernet");
//		
//		storage.setDomain("RENCI (Chapel Hill, NC USA) XO Rack");
//		storage.setCapacity(10);
//		
//		//masterIface.setIpAddress("172.16.1.1");
//    	//masterIface.setNetmask("255.255.255.0");
//		//workersIface.setIpAddress("172.16.1.100");
//		//workersIface.setNetmask("255.255.255.0");
//		
//		//net.allocateIPSubnet(300);
//		//net.setIPSubnet("196.168.0.0", 20);
//		//net.autoIP();
//		net.setIPSubnet("192.168.0.0",24);
//		net.clearAvailableIPs();
//		for (int i = 20; i < 50; i++){
//			net.addAvailableIP("192.168.0." + i);
//		}
//		s.autoIP();
//		
//		//s.logger().debug("******************** START REQUEST *********************");
//		//s.logger().debug(s.getRequest());
//		//s.logger().debug("******************** END REQUEST ******************getManifestFromORCA("adamantTest1", "https://localhost:11443/orca/xmlrpc");***");
//		
//		s.save("/home/geni-orca/test-requests/adamant-test1-output-request.rdf");
//
//		/*************** Submit ********************/ 
//		processPreferences();
//                //@anirban commented out (05/05/15)
//                //sendCreateRequestToORCA("adamantTest1", "https://localhost:11443/orca/xmlrpc", s.getRequest());
//        
//       
//	}
//	
//	
//	/** 
//	 *  Test Case 2
//	 *  
//	 *  1.  Read in a request rdf of a pegags/condor slice template
//	 *  2. 	Add/remove nodes to the group of workers
//	 *  3.  Get the rdf
//	 */
//	
//	public static void adamantTest2(){
//		Slice s = new Slice();
//		s.logger().debug("adamantTest2: ");
//		s.loadFile("/home/geni-orca/test-requests/adamant-test2-input-request-template.rdf");
//		//s.loadRDF(readRDF("/home/geni-orca/test-requests/adamant-test2-input-request-template.rdf"));
//		
//		
//		
//		ComputeNode n = (ComputeNode)s.getResourceByName("Workers");
//		n.setNodeCount(16);
//		
//		s.logger().debug("******************** START REQUEST *********************");
//		s.logger().debug(s.getRequest());
//		s.logger().debug("******************** END REQUEST *********************");
//		
//		s.save("/home/geni-orca/test-requests/adamant-test2-output-request.rdf");
//		
//	}
//	
//	/** 
//	 *  Test Case 3
//	 *  
//	 *  1.  Read in a manifest rdf of a running pegasus/condor slice 
//	 *  2. 	Add/remove nodes to the group of workers
//	 *  3.  Get the modify request rdf
//	 * @thro 
//	 */
//        
//	public static void adamantTest3() {
//		
//		
//		Slice s = new Slice();
//		s.logger().debugs("adamantTest3: ");
//		//s.loadFile("/home/geni-orca/test-requests/adamant-test3-input-manifest.rdf");
//		
//		
//		
//		//printRequest2Log(s);
//		
//		//s.logger().debug("******************** START MANIFEST *********************");
//		//s.logger().debug(s.getRequest());
//		//s.logger().debug("******************** START MANIFEST *********************");
//		
//		//s.save("/home/geni-orca/test-requests/adamant-test3-output-request.rdf");
//		
//		//String ndlReq = s.getRequest();
//		
//		// ***************** Submit *******************  
//        processPreferences();
//        //sendCreateRequestToORCA("adamantTest1", "https://localhost:11443/orca/xmlrpc", ndlReq);
//		
//        //System.out.println("Press enter to continue...");
//        //Scanner keyboard = new Scanner(System.in);
//        //keyboard.nextLine();
//        //System.out.println("continuing");
//        String manifest = getManifestFromORCA("pruth-ndllib-1", "https://geni.renci.org:11443/orca/xmlrpc");
//        s.logger().debug("******************** START MANIFEST *********************");
//		s.logger().debug(manifest);
//		s.logger().debug("******************** END MANIFEST *********************");
//		
//		
//		s.logger().debug("************************************ Loading new manifest into slice *********************************");
//
//		s.loadRDF(manifest);
//		
//		printRequest2Log(s);
//		
//		s.logger().debug("NodeGroup0: ");
//		ComputeNode cn = (ComputeNode) s.getResourceByName("NodeGroup0");
//		s.logger().debug("NodeGroup0 = " + cn);
//		s.logger().debug("NodeGroup0 nodeCount = " + cn.getNodeCount());
//		//cn.setNodeCount(9);
//		//s.logger().debug("Workers nodeCount = " + cn.getNodeCount());
//		
//		
//		
//		//int i = 0;
//		//for (orca.ndllib.resources.manifest.Node mn : ((ComputeNode)cn).getManifestNodes()){
//		//	s.logger().debug("public ip: " + mn.getPublicIP());
//			 //s.logger().debug("manifestNode: " + mn.getURI() + ", state = " + mn.getState());
//			 //if (i++ == 1) {
//			//	 s.logger().debug("manifestNode: deleting " + mn.getURI());
//			//	 mn.delete();
//			 //}
//		//}
//		
//		s.logger().debug("SliceState = " + s.getState());
//		s
//		//s.logger().debug(manifest);
//		
//		//s.logger().debug("******************** START MANIFEST *********************");
//		//s.logger().debug(s.getRequest());
//		//s.logger().debug("******************** END MANIFEST *********************");
//		
//		//ComputeNode n = (ComputeNode)s.getResourceByName("Workers");
//		//if(n != null){
//		//	n.setNodeCount(10);
//		//} else {
//		//	s.logger().debug("ERROR: n = null");
//		//}
//		
//		//printRequest2Log(s);
//		
//		//sendModifyRequestToORCA("adamantTest1", "https://localhost:11443/orca/xmlrpc", s.getRequest());
//		
//        
//	}

	
	
	public static void printManifest2Log(Slice s){
		s.logger().debug("******************** START printManifest2Log *********************");
		//r.logger().debug(r.getRequestDebugString());
		/*for (Node node : m.getNodes()){
			m.logger.debug("PRUTH:" + node);
		}
		
		for (Network link : m.getLinks()){
			m.logger.debug("PRUTH:" + link);
		}*/
		s.logger().debug("******************** END printManifest2Log *********************");
	}
	
	public static void printRequest2Log(Slice s){
		s.logger().debug("******************** START printReqest2Log *********************");
		//r.logger().debug(r.getRequestDebugString());
		for (Node node : s.getNodes()){
			String printStr = "PRUTH:" + node;
			if (node instanceof ComputeNode){
				printStr += ", size: " + ((ComputeNode)node).getNodeCount();
				for (orca.ahab.libndl.resources.manifest.Node mn : ((ComputeNode)node).getManifestNodes()){
					printStr += ", manifestNode: " + mn.getURI();
				}
			}
			s.logger().debug(printStr);
		}
		
		for (Network link : s.getLinks()){
			s.logger().debug("PRUTH:" + link);
		}
		s.logger().debug("******************** END printReqest2Log *********************");
	}
	

	
	/****************************** XMLRPC methods ************************************/
    private static final String GLOBAL_PREF_FILE = "/etc/rm/rm.properties";
    private static final String PREF_FILE = ".rm.properties";

    private static final String PUBSUB_PROP_PREFIX = "RM.pubsub";
    private static final String PUBSUB_SERVER_PROP = PUBSUB_PROP_PREFIX + ".server";
    private static final String PUBSUB_LOGIN_PROP = PUBSUB_PROP_PREFIX + ".login";
    private static final String PUBSUB_PASSWORD_PROP = PUBSUB_PROP_PREFIX + ".password";


    private static Properties rmProperties = null;
	
	
	
    /**
     * Read and process preferences file
     */
    protected static void processPreferences() {

            Properties p = System.getProperties();

            // properties can be under /etc/mm/mm.properties or under $HOME/.mm.properties
            // in that order of preference
            String prefFilePath = GLOBAL_PREF_FILE;

            try {
            		System.err.println("loding properites from " + prefFilePath);
                    rmProperties = loadPropertiesFromAnyFile(prefFilePath);
                    System.err.println("rmProperties = " + rmProperties.toString());
                    return;
            } catch (IOException ioe) {
                    System.err.println("Unable to load global config file " + prefFilePath + ", trying local file");
            }

            prefFilePath = "" + p.getProperty("user.home") + p.getProperty("file.separator") + PREF_FILE;
            try {
            		System.err.println("loding properites from " + prefFilePath);
            		
                    rmProperties = loadPropertiesFromAnyFile(prefFilePath);
                    System.err.println("rmProperties = " + rmProperties.toString());
            } catch (IOException e) {
                    System.err.println("Unable to load local config file " + prefFilePath + ", exiting.");
                    System.exit(1);
            }
    }


    /**
     * loads properties from a file in the classpath
     * @param fileName
     * @return
     * @throws IOException
     */
    private static Properties loadProperties(String fileName) throws IOException {

        //File prefs = new File(fileName);
        //FileInputStream is = new FileInputStream(prefs);

        InputStream is = TestDriver.class.getClassLoader().getResourceAsStream(fileName);

        BufferedReader bin = new BufferedReader(new InputStreamReader(is, "UTF-8"));

        Properties p = new Properties();
        p.load(bin);
        bin.close();

        return p;
    }

    /**
     * loads properties from any file , given it's absolute path
     * @param fileName
     * @return
     * @throws IOException
     */
    private static Properties loadPropertiesFromAnyFile(String fileName) throws IOException {

        File prefs = new File(fileName);
        FileInputStream is = new FileInputStream(prefs);

        BufferedReader bin = new BufferedReader(new InputStreamReader(is, "UTF-8"));

        Properties p = new Properties();
        p.load(bin);
        bin.close();

        return p;

    }
    
    // Send modify request to a specific ORCA controller
    private static void sendModifyRequestToORCA(String sliceId, String controllerUrl, String modifyReq){

        //Logger logger = Logger.getLogger(this.getClass());

        String modifyRes = null;
        try {
            OrcaSMXMLRPCProxy orcaProxy = new OrcaSMXMLRPCProxy(rmProperties);
            orcaProxy.setControllerUrl(controllerUrl);
            modifyRes = orcaProxy.modifySlice(sliceId, modifyReq);
            //logger.info("Result for modify slice for " + sliceId + " = " + modifyRes);
            //System.out.println("Result for modify slice for " + sliceId + " = " + modifyRes);
        } catch (Exception ex) {
            //logger.error("Exception while calling ORCA modifySlice" + ex);
            System.out.println("Exception while calling ORCA modifySlice" + ex);
            return;
        }
        return;

    }
    
    // Send create request to a specific ORCA controller
    private static void sendCreateRequestToORCA(String sliceId, String controllerUrl, String createReq){

        //Logger logger = Logger.getLogger(this.getClass());

        String createRes = null;
        try {
            OrcaSMXMLRPCProxy orcaProxy = new OrcaSMXMLRPCProxy(rmProperties);
            orcaProxy.setControllerUrl(controllerUrl);
            createRes = orcaProxy.createSlice(sliceId, createReq);
            //logger.info("Result for create slice for " + sliceId + " = " + createRes);
            //System.out.println("Result for modify slice for " + sliceId + " = " + createRes);
        } catch (Exception ex) {
            //logger.error("Exception while calling ORCA createSlice" + ex);
            System.out.println("Exception while calling ORCA createSlice" + ex);
            return;
        }
        return;

    }    
    
    private static String getManifestFromORCA(String sliceId, String controllerUrl){

        //Logger logger = Logger.getLogger(this.getClass());

        String manifest = null;
        String sanitizedManifest = null;
        try {
            OrcaSMXMLRPCProxy orcaProxy = new OrcaSMXMLRPCProxy(rmProperties);
            orcaProxy.setControllerUrl(controllerUrl);
            manifest = orcaProxy.sliceStatus(sliceId);
            //logger.info("manifest for slice " + sliceId + " = " + manifest);
            //System.out.println("manifest for slice " + sliceId + " = " + manifest);
            sanitizedManifest = sanitizeManifest(manifest);
        } catch (Exception ex) {
            //logger.error("Exception while calling ORCA sliceStatus" + ex);
            System.out.println("Exception while calling ORCA sliceStatus" + ex);
            return null;
        }
        return sanitizedManifest;

    }

    private static String sanitizeManifest(String manifest) {

    	if (manifest == null)
    		return null;

    	int ind = manifest.indexOf("<rdf:RDF");
    	if (ind > 0)
    		return manifest.substring(ind);
    	else
    		return null;


    }
   
	public static final ArrayList<String> domains;
	static {
		ArrayList<String> l = new ArrayList<String>();
		
		l.add("PSC (Pittsburgh, TX, USA) XO Rack");
		l.add("TAMU (College Station, TX, USA) XO Rack");
		
		l.add("UH (Houston, TX USA) XO Rack");
		l.add("WSU (Detroit, MI, USA) XO Rack");
		l.add("UFL (Gainesville, FL USA) XO Rack");
		l.add("OSF (Oakland, CA USA) XO Rack");
		l.add("SL (Chicago, IL USA) XO Rack");
		l.add("UMass (UMass Amherst, MA, USA) XO Rack");
		l.add("WVN (UCS-B series rack in Morgantown, WV, USA)");
		l.add("UAF (Fairbanks, AK, USA) XO Rack");
		l.add("BBN/GPO (Boston, MA USA) XO Rack");
		l.add("RENCI (Chapel Hill, NC USA) XO Rack");
		l.add("UvA (Amsterdam, The Netherlands) XO Rack");
	
		domains = l;
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
			" sleep 10 \n" +
			" ovs-vsctl add-br br0 \n" +
			" ifconfig br0 up \n" +
			" controller_ip='" + SDNControllerIP + "' \n" +
			" echo \"setting ovs to use controller \" ${controller_ip} \n" +
			" ovs-vsctl set-controller br0 tcp:${controller_ip}:6633 \n" +
			" ovs-vsctl set controller br0 connection-mode=out-of-band \n" +
			" ovs-appctl fdb/show br0 \n" +
			" sleep 60 \n" +
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