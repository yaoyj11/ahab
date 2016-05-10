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
import java.util.Collections;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import orca.ahab.libndl.resources.request.BroadcastNetwork;
import orca.ahab.libndl.resources.request.ComputeNode;
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
	public static void main(String [] args){
    	System.out.println("ndllib TestDriver: START");
    	
    	LIBNDL.setLogger();
    	//testDeleteComputeNode(args[0],"Node0");
    	//testAddComputeNode(args[0],"Node3");
    	//testDeleteComputeNode(args[0],"Node1");
    	//testLibtransport(args[0]);
    	//testLoad(args[0],"pruth.1");
    	//TestDriver.testAddNetwork(args[0], "Node0", "Node1", "VLAN0");

    	TestDriver.testNewSlice1(args[0]);
    	
    	//testSave();
    	//testLoadAndSave();
    	//testLoadManifest();
    	//adamantTest1();
    	//adamantTest2();
    	//adamantTest3();
    	///autoIP1();
    	System.out.println("ndllib TestDriver: END");
    	
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
//
//		try{
//			IP4Assign ipa = new IP4Assign();
//			IP4Subnet subnet5 = ipa.getSubnet((Inet4Address)InetAddress.getByName("172.16.0.6"),24);
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
	
	public static void testDeleteComputeNode(String pem, String delNodeName){
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
	
	public static void testAddComputeNode(String pem, String newNodeName){
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
		//s.logger().debug(s.getDebugString());
	
		
		ComputeNode   newnode = s.addComputeNode(newNodeName);
		newnode.setImage("http://geni-images.renci.org/images/standard/centos/centos6.3-v1.0.11.xml","776f4874420266834c3e56c8092f5ca48a180eed","PRUTH-centos");
		newnode.setNodeType("XO Large");
		newnode.setDomain("RENCI (Chapel Hill, NC USA) XO Rack");
		newnode.setPostBootScript("master post boot script");

		s.logger().debug("******************** END PRINTING *********************");
		
		ComputeNode cn = (ComputeNode)s.getResourceByName(newNodeName);
		s.logger().debug("cn: " + cn);
		s.logger().debug("compute node: " + cn.getName());
		s.logger().debug("compute node: " + cn.getDomain());
		s.logger().debug("compute node: " + cn.getNodeType());
		s.logger().debug("compute node: " + cn.getImageShortName());
		s.logger().debug("compute node: " + cn.getImageHash());
		s.logger().debug("compute node: " + cn.getImageUrl());
		s.logger().debug("compute node: " + cn.getPostBootScript());
		
		s.logger().debug("Interfaces:  " + cn.getInterfaces());
		
		s.commit();
		
	}
	
	
	public static void testAddNetwork(String pem, String node1Name, String node2Name, String networkName){
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
		
			
		s.logger().debug("******************** START Slice Info " + s.getName() + " *********************");
		//s.logger().debug(s.getRequest());
		//s.logger().debug(s.getDebugString());
		s.logger().debug("******************** END PRINTING *********************");
		
		ComputeNode node1 = (ComputeNode)s.getResourceByName(node1Name);
		ComputeNode node2 = (ComputeNode)s.getResourceByName(node2Name);
		
		BroadcastNetwork net = s.addBroadcastLink(networkName);
		net.stitch(node1);
		net.stitch(node2);
	
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
   

	
}
