/*
* Copyright (c) 2011 RENCI/UNC Chapel Hill 
*
* @author Ilia Baldine
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
* and/or hardware specification (the "Work") to deal in the Work without restriction, including 
* without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
* sell copies of the Work, and to permit persons to whom the Work is furnished to do so, subject to 
* the following conditions:  
* The above copyright notice and this permission notice shall be included in all copies or 
* substantial portions of the Work.  
*
* THE WORK IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
* OUT OF OR IN CONNECTION WITH THE WORK OR THE USE OR OTHER DEALINGS 
* IN THE WORK.
*/
package org.renci.ahab.libndl.ndl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import orca.ndl.INdlManifestModelListener;
import orca.ndl.INdlRequestModelListener;
import orca.ndl.NdlCommons;
import orca.ndl.NdlManifestParser;
import orca.ndl.NdlRequestParser;

import org.apache.commons.lang.StringUtils;
import org.renci.ahab.libndl.LIBNDL;
import org.renci.ahab.libndl.Slice;
import org.renci.ahab.libndl.SliceGraph;
import org.renci.ahab.libndl.resources.common.ModelResource;
import org.renci.ahab.libndl.resources.request.ComputeNode;
import org.renci.ahab.libndl.resources.request.Interface;
import org.renci.ahab.libndl.resources.request.InterfaceNode2Net;
import org.renci.ahab.libndl.resources.request.Network;
import org.renci.ahab.libndl.resources.request.Node;
import org.renci.ahab.libndl.resources.request.RequestReservationTerm;
import org.renci.ahab.libndl.resources.request.RequestResource;
import org.renci.ahab.libndl.resources.request.StitchPort;
import org.renci.ahab.libndl.resources.request.StorageNode;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;


import edu.uci.ics.jung.graph.SparseMultigraph;

public class UserAbstractionLoader extends NDLLoader  implements INdlManifestModelListener {
	
	private NDLModel ndlModel;
	private RequestReservationTerm term = new RequestReservationTerm();

	public UserAbstractionLoader(SliceGraph sliceGraph, NDLModel ndlModel){
		this.sliceGraph = sliceGraph;
		this.ndlModel = ndlModel;
	}
    


	/**
	 * Load from string
	 * @param f
	 * @return
	 */
	public NdlManifestParser load(String rdf) {
		NdlManifestParser nrp;
		try {
			nrp = new NdlManifestParser(rdf, this);
			//nrp.doLessStrictChecking(); //TODO: Should be removed...
			//nrp.processRequest();
			//nrp.freeModel();
			nrp.processManifest();
			LIBNDL.logger().debug("processManifest ends");

		} catch (Exception e) {
			LIBNDL.logger().error(e);
			LIBNDL.logger().debug("error loading graph");
			e.printStackTrace();
			return null;
		} 
		
		return nrp;
	}

	
	

	public void ndlReservation(Resource i, final OntModel m) {
		
		LIBNDL.logger().debug("Reservation: " + i + ", sliceState(Request:ndlReservation) = " + NdlCommons.getGeniSliceStateName(i));
		// try to extract the guid out of the URL
		String u = i.getURI();
		String guid = StringUtils.removeEnd(StringUtils.removeStart(u, NdlCommons.ORCA_NS), "#");
		
		this.sliceGraph.setNsGuid(guid);
		
		//this.slice.setState(NdlCommons.getGeniSliceStateName(i));
		
		/*if (i != null) {
			reservationDomain = RequestSaver.reverseLookupDomain(NdlCommons.getDomain(i));
			this.sliceGraph.setOFVersion(NdlCommons.getOpenFlowVersion(i));
		}*/
	}

	public void ndlReservationEnd(Literal e, OntModel m, Date end) {
		// Nothing to do
	}

	public void ndlReservationStart(Literal s, OntModel m, Date start) {
		term.setStart(start);
	}

	public void ndlReservationTermDuration(Resource t, OntModel m, int years, int months, int days,
			int hours, int minutes, int seconds) {
		term.setDuration(days, hours, minutes);
	}

	private boolean isType(Resource r, Resource resourceClass){
		
		//Test for type of subject (if any)
		Resource candidateResourceClass = r.getProperty(new PropertyImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")).getResource();
	
		if(candidateResourceClass != null && candidateResourceClass.equals(resourceClass)){
			return true;
		}
		return false;
		
	}
	
	public void ndlNode(Resource ce, OntModel om, Resource ceClass, List<Resource> interfaces) {
		try {
			LIBNDL.logger().debug("UserAbstractionLoader::ndlNode");
			LIBNDL.logger().debug("UserAbstractionLoader::ndlNode, Node: " + ce + " of class " + ceClass + ", ce objext class: " + ce.getClass());
			
			if (ce == null)
				return;
			
			Node newNode;
			ComputeNode newComputeNode = null;
			if (ceClass.equals(NdlCommons.computeElementClass)){
				//if(!ce.hasProperty(NdlCommons.manifestHasParent)){
				LIBNDL.logger().debug("BUILDING: Compute Node: " + this.getPrettyName(ce) + " : found computeElementClass, parent = " + ce.hasProperty(NdlCommons.manifestHasParent));
				newNode = this.sliceGraph.buildComputeNode(this.getPrettyName(ce));
				newComputeNode = (ComputeNode)newNode;
				
				
		
				ndlModel.mapRequestResource2ModelResource(newNode, ce);
				LIBNDL.logger().debug("newComputeNode.getName(): " + newNode.getName());
				Resource returnedResource = ndlModel.getModelResource(newNode);
				LIBNDL.logger().debug("returnedResource.getLocalName(): " + returnedResource.getLocalName());
				ndlModel.printRequest2NDLMap();
				
				
			
			} else if (ceClass.equals(NdlCommons.serverCloudClass)) {
				LIBNDL.logger().debug("BUILDING: Group Node: " + ce.getLocalName() + " : found serverCloudClass, parent = " + ce.hasProperty(NdlCommons.manifestHasParent));
				ComputeNode newNodeGroup = this.sliceGraph.buildComputeNode(ce.getLocalName());
				ndlModel.mapRequestResource2ModelResource(newNodeGroup, ce);
				//newNodeGroup.setNDLModel(ndlModel);
				//ndlModel.mapSliceResource2ModelResource(newNodeGroup, ce);
				newComputeNode = newNodeGroup;
				//int ceCount = NdlCommons.getNumCE(ce);
				//if (ceCount > 0) newNodeGroup.setNodeCount(ceCount);
				newNodeGroup.initializeNodeCount(0);
				//newNodeGroup.setSplittable(NdlCommons.isSplittable(ce));
				newNode = newNodeGroup;


				String groupUrl = NdlCommons.getRequestGroupURLProperty(ce);
				LIBNDL.logger().debug("NdlCommons.getRequestGroupURLProperty: " + groupUrl);

				String nodeUrl = ce.getURI();
				LIBNDL.logger().debug("URI: " + nodeUrl);



			} else if (NdlCommons.isNetworkStorage(ce)) {
				LIBNDL.logger().debug("BUILDING: Storage Node: " + ce.getLocalName() );
				// storage node
				StorageNode snode = this.sliceGraph.buildStorageNode(ce.getLocalName());
				ndlModel.mapRequestResource2ModelResource(snode, ce);
				newNode = snode;
				snode.setCapacity(NdlCommons.getResourceStorageCapacity(ce));
				
			} else if(NdlCommons.isStitchingNode(ce)){
				LIBNDL.logger().debug("\n\n\n ************************************** FOUND STITCHPORT NODE *************************************** \n\n\n");
				LIBNDL.logger().debug("Found a stitchport");
				
				//StitchPort
				Resource sp = ce;
				
				//LIBNDL.logger().debug("&&&&&&&&&&&&&&&&&&&&&&&& StitchPort uri" + ce.getURI());
				//LIBNDL.logger().debug("&&&&&&&&&&&&&&&&&&&&&&&& StitchPort id" + ce.getId());
				Iterator i;
				//for (i = om.listStatements(null, new PropertyImpl("http://geni-orca.renci.org/owl/topology.owl#hasInterface"), (RDFNode) sp); i.hasNext();){
				//	Statement st = (Statement) i.next();
				//	LIBNDL.logger().debug("FOUND Statement subject: " + st.getSubject() + ", predicate: " + st.getPredicate() + ", resource  " + st.getResource()); 
				//}
				
				
				//get Interface
				Resource spIface = null;
				//get the interface
				if(interfaces.size() == 1){
					spIface = interfaces.get(0);
				} else {
					LIBNDL.logger().error("StitchPort: " + ce.getLocalName() + ", has wrong number in interfaces (" + interfaces.size() + ")");
				}
				//LIBNDL.logger().debug("StitchPort: " + ce.getLocalName() + ", " + ce.getURI());
				//LIBNDL.logger().debug("Interface = " + spIface.getLocalName() + ", " + spIface.getURI()); 
			
				LIBNDL.logger().debug("Looking for linkconnection");
				//get LinkConnection (thing that sit between an interface and a link... why?)
				Resource spLinkConnection = null;
				
				for (i = om.listStatements(null, new PropertyImpl("http://geni-orca.renci.org/owl/topology.owl#hasInterface"), (RDFNode) spIface); i.hasNext();){
					Statement st = (Statement) i.next();
					LIBNDL.logger().debug("FOUND Statement subject: " + st.getSubject() + ", predicate: " + st.getPredicate() + ", resource  " + st.getResource()); 
					
					//if (!st.getSubject().equals(sp)) {
					if(isType(st.getSubject(),NdlCommons.topologyNetworkConnectionClass)){
						LIBNDL.logger().debug("XXXXXXXXXXXXXXXX SETTING  LinkConnection resource: " + st.getSubject());
						spLinkConnection = st.getSubject(); 
						break;
					}
				}
				
				
				//LIBNDL.logger().debug("StitchPort: " + ce.getLocalName() + ", " + ce.getURI());
				//LIBNDL.logger().debug("Interface = " + spIface.getLocalName() + ", " + spIface.getURI()); 
				//LIBNDL.logger().debug("LinkConnection: " + spLinkConnection.getLocalName() + ", " + spLinkConnection.getURI());
				//LIBNDL.logger().debug("LinkConnection: " + spLinkConnection);
				//LIBNDL.logger().debug("LinkConnection: TYPE: " + spLinkConnection.getProperty(new PropertyImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")));

				//i = spLinkConnection.listProperties();
				//while (i.hasNext()){
				//	LIBNDL.logger().debug("Property: " + i.next());
				//}
				
	
				
				LIBNDL.logger().debug("Looking for linkconnectioniface");
				//Get Link connection iface (the interface between the linkconnection and the link
				Resource spLinkConnectionIface = null;
				for (i = om.listStatements(spLinkConnection, new PropertyImpl("http://geni-orca.renci.org/owl/topology.owl#hasInterface"), (RDFNode) null); i.hasNext();){
					Statement st = (Statement) i.next();
					LIBNDL.logger().debug("FOUND Statement subject: " + st.getSubject() + ", predicate: " + st.getPredicate() + ", resource  " + st.getResource());  
					if (!st.getResource().equals(spIface)) {
						LIBNDL.logger().debug("XXXXXXXXXXXXX SETTING Link connection iface resource: " + st.getResource());
						spLinkConnectionIface = st.getResource(); 
						break;
					}
				}
				
				
				//LIBNDL.logger().debug("StitchPort: " + ce.getLocalName() + ", " + ce.getURI());
				//LIBNDL.logger().debug("Interface = " + spIface.getLocalName() + ", " + spIface.getURI()); 
				//LIBNDL.logger().debug("LinkConnectionIface: " + spLinkConnectionIface.getLocalName() + ", " + spLinkConnectionIface.getURI());
				//LIBNDL.logger().debug("LinkConnectionIface: TYPE: " + spLinkConnectionIface.getProperty(new PropertyImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")));
				
				
				
				//i = spLinkConnectionIface.listProperties();
				//while (i.hasNext()){
				//	LIBNDL.logger().debug("Property: " + i.next());
				//}
				

				//inRequestNetworkConnection
				
//				//Get stichport in request 
//				LIBNDL.logger().debug("GET spRequest: BEGIN");
//				Resource spRequest = null;
//				for (i = om.listStatements(null, new PropertyImpl("http://geni-orca.renci.org/owl/topology.owl#hasInterface"), (RDFNode) spLinkConnectionIface); i.hasNext();){
//					Statement st = (Statement) i.next();
//					LIBNDL.logger().debug("FOUND Statement subject: " + st.getSubject() + ", predicate: " + st.getPredicate() + ", resource  " + st.getResource());  
//					
//					if(isType(st.getSubject(),NdlCommons.deviceOntClass)){
//						spRequest = st.getSubject();
//						break;
//					}
//				}
//				LIBNDL.logger().debug("GET spRequest: END");
//				
//				
//				
//				//Get Link
//				Resource spLink = null;
//				for (i = om.listStatements(null, new PropertyImpl("http://geni-orca.renci.org/owl/topology.owl#hasInterface"), (RDFNode) spLinkConnectionIface); i.hasNext();){
//					Statement st = (Statement) i.next();
//					LIBNDL.logger().debug("FOUND Statement subject: " + st.getSubject() + ", predicate: " + st.getPredicate() + ", resource  " + st.getResource()); 
//
//					if (st.getSubject().equals(spLinkConnectionIface)) {
//						//LIBNDL.logger().debug("XXXXXXXXXXXXX SETTING Link resource: " + st.getResource());
//						spLink = st.getSubject();
//						break;
//					}
//				}
				

				//inRequestNetworkConnection
				
				//Get stichport in request 
				LIBNDL.logger().debug("GET spRequest: BEGIN");
				Resource spRequest = null;
				for (i = om.listStatements(null, NdlCommons.inRequestNetworkConnection, (RDFNode) spLinkConnection); i.hasNext();){
					Statement st = (Statement) i.next();
					LIBNDL.logger().debug("FOUND Statement subject: " + st.getSubject() + ", predicate: " + st.getPredicate() + ", resource  " + st.getResource());  
					
					if(isType(st.getSubject(),NdlCommons.deviceOntClass)){
						spRequest = st.getSubject();
						break;
					}
				}
				LIBNDL.logger().debug("GET spRequest: END");
				
				
				
				
				//LIBNDL.logger().debug("StitchPort: " + ce.getLocalName() + ", " + ce.getURI());
				//LIBNDL.logger().debug("Interface = " + spIface.getLocalName() + ", " + spIface.getURI()); 
				//LIBNDL.logger().debug("Link: " + spLink.getLocalName() + ", " + spLink.getURI());
				//LIBNDL.logger().debug("Link: TYPE: " + spLink.getProperty(new PropertyImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")));
				
				
				LIBNDL.logger().debug("StitchPort (Request) = " + spRequest.getLocalName() + ", " + spRequest.getURI()); 
				
				String spName = spRequest.getLocalName();
				//String spName = spLink.getLocalName().substring(0, spLink.getLocalName().indexOf("-net") - 1);
				//String spName = ce.getLocalName();
				
				LIBNDL.logger().debug("StitchPort Name: " + spName);
				
				//Statement st = ce.getProperty(new PropertyImpl("http://geni-orca.renci.org/owl/topology.owl#hasURL"));
				//st.getObject();
				//st.getString();
				
				//LIBNDL.logger().debug(printStr);
						
				String label = null;
				String port = null;
				if (interfaces.size() == 1 && NdlCommons.getLinkTo(interfaces.get(0)) != null){
					label = NdlCommons.getLayerLabelLiteral(interfaces.get(0));
					port = NdlCommons.getLinkTo(interfaces.get(0)).toString();
				}
				long bandwidth = 10000000;
				StitchPort newStitchport = this.sliceGraph.buildStitchPort(spName,label,port,bandwidth);
								
				ndlModel.mapRequestResource2ModelResource(newStitchport, spRequest);
				LIBNDL.logger().debug("\n\n\n ************************************** Done WITH STITCHPORT NODE *************************************** \n\n\n");
				return;
			} else {
				// default just a node
				LIBNDL.logger().debug("BUILDING: Just a Node: " + ce.getLocalName() );
				newNode = this.sliceGraph.buildComputeNode(ce.getLocalName());
				ndlModel.mapRequestResource2ModelResource(newNode, ce);
			}
			
			sliceGraph.printGraph();
			
			LIBNDL.logger().debug("about to load domain");
			Resource domain = NdlCommons.getDomain(ce);
			if (domain != null){
				LIBNDL.logger().debug("load domain: " + RequestGenerator.reverseLookupDomain(domain));
				//newNode.setDomain(RequestGenerator.reverseLookupDomain(domain));
			}
				
			
			if (ceClass.equals(NdlCommons.computeElementClass) || ceClass.equals(NdlCommons.serverCloudClass)){
				Resource ceType = NdlCommons.getSpecificCE(ce);
				if (ceType != null){
					newComputeNode.setNodeType(RequestGenerator.reverseNodeTypeLookup(ceType));
				}
			}

			//process image
			LIBNDL.logger().debug("about to load image");
			if (ceClass.equals(NdlCommons.computeElementClass) || ceClass.equals(NdlCommons.serverCloudClass)){
				LIBNDL.logger().debug("about to load domain: it is a compute element");
				// disk image
				Resource di = NdlCommons.getDiskImage(ce);
				if (di != null) {
					LIBNDL.logger().debug("about to load domain: it has a image");
					try {
						String imageURL = NdlCommons.getIndividualsImageURL(ce);
						String imageHash = NdlCommons.getIndividualsImageHash(ce);
						//String imName = this.sliceGraph.buildImage(new OrcaImage(di.getLocalName(), 
						//		new URL(imageURL), imageHash), null);
						//String imName = imageURL + imageHash;  //FIX ME: not right
						String imName = newComputeNode.getName() + "-image"; //FIX ME: not right:  why do we even have an image name???
						newComputeNode.setImage(imageURL,imageHash,imName);
					} catch (Exception e) {
						// FIXME:SliceGraph ?
						LIBNDL.logger().debug("about to load domain: hit an exception");
						;
					}
				}

				// post boot script
				String script = NdlCommons.getPostBootScript(ce);
				if ((script != null) && (script.length() > 0)) {
					newComputeNode.setPostBootScript(script);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Broadcast networks only
	 */
	public void ndlNetworkConnection(Resource l, OntModel om, 
			long bandwidth, long latency, List<Resource> interfaces) {
		
		LIBNDL.logger().debug("NetworkConnection: " + l);
		
		// LIBNDL.logger().debug("Found connection " + l + " connecting " + interfaces + " with bandwidth " + bandwidth);
		if (l == null)
			return;


		//om.listStatements(null, null, l);
 		LIBNDL.logger().debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%     ndlNetworkConnection     %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% ");
 		//NdlCommons.getResourceType(r)
 		if (NdlCommons.isLinkConnection(l)){
 			LIBNDL.logger().debug("NdlCommons.isLinkConnection(l)");
 		} else {
 			LIBNDL.logger().debug("NOT NdlCommons.isLinkConnection(l)");
 		}
 		
 		
 		//super hack to pre-calculate state because parts of model close after parsing
 		LIBNDL.logger().debug("looking for statements: begin");
 		String state = "Active";
 		Iterator i = null;
 		for (i = om.listStatements(null, NdlCommons.inRequestNetworkConnection, (RDFNode) l); i.hasNext();){
    		Statement st = (Statement) i.next();
    		LIBNDL.logger().debug("FOUND Statement subject: " + st.getSubject() + ", predicate: " + st.getPredicate() + ", resource  " + st.getResource()); 
    		//LIBNDL.logger().debug("resource type: " + getType(st.getSubject()));

    		if (isType(st.getSubject(),NdlCommons.topologyCrossConnectClass)) {

					LIBNDL.logger().debug("adding vlan: " + st.getSubject());
					try {
						if (NdlCommons.getResourceStateAsString(st.getSubject()).equals("Failed")) {
							LIBNDL.logger().debug("State = " + NdlCommons.getResourceStateAsString(st.getSubject()));
							state = "Failed";
						}

						if (!NdlCommons.getResourceStateAsString(st.getSubject()).equals("Active")) {
							LIBNDL.logger().debug("State = " + NdlCommons.getResourceStateAsString(st.getSubject()));
							state = "Building";
							break;
						}
					}catch (NullPointerException e){
						LIBNDL.logger().debug("State = " + NdlCommons.getResourceStateAsString(st.getSubject()));
						state = "NULL";
					}
				}
    	}
 		LIBNDL.logger().debug("looking for statements: end");
 		LIBNDL.logger().debug("State = " + state);
 		
		Network ol = this.sliceGraph.buildLink(l.getLocalName());
		ndlModel.mapRequestResource2ModelResource(ol, l);
		ol.setBandwidth(bandwidth);
		ol.setLatency(latency);
		ol.setLabel(NdlCommons.getLayerLabelLiteral(l));
	
		//hack
		LIBNDL.logger().debug("Setting state = " + state);
		//ol.setState(state);
	}

	public void ndlInterface(Resource intf, OntModel om, Resource conn, Resource node, String ip, String mask) {
		LIBNDL.logger().debug("PRUTH-Interface: " + intf + " link: " + conn + " node: " + node);
		
		try{
		
		if(intf == null){
			
			return;
		}
				
		RequestResource onode = null;
		if(node != null){
			ndlModel.printRequest2NDLMap();
			onode = this.sliceGraph.getResourceByName(this.getPrettyName(node));
			LIBNDL.logger().debug("ndlInterface with node: " + onode + ", localName: " + this.getPrettyName(node));
		} else {
			LIBNDL.logger().debug("ndlInterface with null node: " + intf);
		}
		RequestResource olink = null;
		if(conn != null){
			olink = this.sliceGraph.getResourceByName(this.getPrettyName(conn));
		} else{
			LIBNDL.logger().warn("ndlInterface with null connection: " + intf);
			LIBNDL.logger().debug("intf.getLocalName: " + intf.getLocalName());
			//LIBNDL.logger().debug("intf.getURI: " + intf.getURI());
			//LIBNDL.logger().debug("intf.getId: " + intf.getId());
			
			//Super hack that needs to be fixed in NDL
			//if the connection is null assume its interdomain and find the link name by parsing the interface name
			LIBNDL.logger().debug("link name: " + intf.getLocalName().split("-")[0]);
			olink = this.sliceGraph.getResourceByName(intf.getLocalName().split("-")[0]);
			//end hack
		}
		
		
		if(onode == null){
			LIBNDL.logger().warn("ndlInterface with null missing node:  Interface: " + intf + ", Node: " + node);
			return;
		}
		
		//ComputeNode
		if(onode instanceof ComputeNode && olink instanceof Network){
			LIBNDL.logger().debug("stitching compute node");
			//InterfaceNode2Net stitch = (InterfaceNode2Net)onode.stitch(olink);
			InterfaceNode2Net stitch = sliceGraph.buildInterfaceNode2Net((Node)onode, (Network)olink);
			ndlModel.mapRequestResource2ModelResource((ModelResource)stitch, intf);
			return;
		} 
		
		//StorageNode
		if(onode instanceof StorageNode){
			LIBNDL.logger().debug("stitching storage node");
			//InterfaceNode2Net stitch = (InterfaceNode2Net)onode.stitch(olink);
			InterfaceNode2Net stitch = sliceGraph.buildInterfaceNode2Net((Node)onode, (Network)olink);
			
			return;
		}
		
		//StitchPort
//		if(onode instanceof StitchPort){
//			LIBNDL.logger().debug("stitching stitchport");
//			//Why is this stuff stored in the interface? 
//			//It Seems like they are properties of the stitchport itself. 
//			StitchPort sp = (StitchPort)onode;
//			sp.setPort(intf.toString());
//			sp.setLabel(NdlCommons.getLayerLabelLiteral(intf));
//			
//			//InterfaceNode2Net stitch = (InterfaceNode2Net)onode.stitch(olink);
//			InterfaceNode2Net stitch = sliceGraph.buildInterfaceNode2Net((Node)onode, (Network)olink);
//			return;
//		}	
		
		}catch (Exception e){
			LIBNDL.logger().debug("PRUTH-Interface: Exception: " + e.getMessage() );
			e.printStackTrace();
		}
		
		
		//shouldnt get here
		LIBNDL.logger().debug("Stitching to unknown node type: " + node + ", " + node.getClass());
	}
	
	public void ndlSlice(Resource sl, OntModel m) {
		LIBNDL.logger().debug("UserAbstractionLoader::ndlSlice, OntModel m = " + m); 
		ndlModel.setJenaModel(m);
		
		LIBNDL.logger().debug("UserAbstractionLoader::ndlSlice, Slice: " + sl + ", sliceState(sliceGraph) = " + NdlCommons.getGeniSliceStateName(sl));
		// check that this is an OpenFlow slice and get its details
		if (sl.hasProperty(NdlCommons.RDF_TYPE, NdlCommons.ofSliceClass)) {
			Resource ofCtrl = NdlCommons.getOfCtrl(sl);
			if (ofCtrl == null)
				return;
			this.sliceGraph.setOfCtrlUrl(NdlCommons.getURL(ofCtrl));
			this.sliceGraph.setOfUserEmail(NdlCommons.getEmail(sl));
			this.sliceGraph.setOfSlicePass(NdlCommons.getSlicePassword(sl));
			if ((this.sliceGraph.getOfUserEmail() == null) ||
					(this.sliceGraph.getOfSlicePass() == null) ||
					(this.sliceGraph.getOfCtrlUrl() == null)) {
					// disable OF if invalid parameters
					//this.sliceGraph.setNoOF();
					this.sliceGraph.setOfCtrlUrl(null);
					this.sliceGraph.setOfSlicePass(null);
					this.sliceGraph.setOfUserEmail(null);
			}
		}	
	}

	public void ndlReservationResources(List<Resource> res, OntModel m) {
		// nothing to do here in this case
	}
	
	public void ndlParseComplete() {
		LIBNDL.logger().debug("Done parsing.");
		// set term etc
		this.sliceGraph.setTerm(term);
		//this.sliceGraph.setDomainInReservation(reservationDomain);
	}

	public void ndlNodeDependencies(Resource ni, OntModel m, Set<Resource> dependencies) {
		LIBNDL.logger().debug("nlNodeDependencies -- SKIPPED");
		
		/*OrcaNode mainNode = nodes.get(ni.getURI());
		if ((mainNode == null) || (dependencies == null))
			return;
		for(Resource r: dependencies) {
			OrcaNode depNode = nodes.get(r.getURI());
			if (depNode != null)
				mainNode.buildDependency(depNode);
		}*/
	}

	/**
	 * Process a broadcast link
	 */
	public void ndlBroadcastConnection(Resource bl, OntModel om,
			long bandwidth, List<Resource> interfaces) {
		
		LIBNDL.logger().debug("BroadcastConnection: " + bl);
		
		Network ol = this.sliceGraph.buildLink(bl.getLocalName());
		ndlModel.mapRequestResource2ModelResource(ol, bl);
		ol.setBandwidth(bandwidth);
		//ol.setLatency(latency);
		ol.setLabel(NdlCommons.getLayerLabelLiteral(bl));	
	}


	/**
	 * Hacks that should be in ndlcommons
	 */
	
	// sometimes getLocalName is not good enough
		// so we strip off orca name space and call it a day
		private String getTrueName(Resource r) {
			if (r == null)
				return null;
			
			return StringUtils.removeStart(r.getURI(), NdlCommons.ORCA_NS);
		}
		protected String getPrettyName(Resource r) {
			String rname = getTrueName(r);
			int start_index = rname.indexOf('#');
			int end_index = rname.indexOf('/');
			if(start_index > 0 && end_index == -1){
				rname = rname.substring(start_index + 1);
			} else if (start_index > 0 && end_index > 0 && end_index > start_index) {
				rname = rname.substring(start_index + 1,end_index);
			}
			return rname;
		}
	
		/****************************************************/

	@Override
	public void ndlManifest(Resource i, OntModel m) {
		LIBNDL.logger().debug("UserAbstractionLoader::ndlManifest, OntModel m = " + m); 
		ndlModel.setJenaModel(m);
		
		
	}



	@Override
	public void ndlLinkConnection(Resource l, OntModel m, List<Resource> interfaces, Resource parent) {
		// TODO Auto-generated method stub
		
	}


	private ArrayList<Resource> crossConnects = null;
	@Override
	public void ndlCrossConnect(Resource c, OntModel m, long bw, String label, List<Resource> interfaces,
			Resource parent) {
		if(crossConnects == null){
			crossConnects = new ArrayList<Resource>();
		}
		
		LIBNDL.logger().debug("Adding CrossConnect: " + c);
		crossConnects.add(c);
	}



	@Override
	public void ndlNetworkConnectionPath(Resource c, OntModel m, List<List<Resource>> path, List<Resource> roots) {
		// TODO Auto-generated method stub
		
	}



	
}
