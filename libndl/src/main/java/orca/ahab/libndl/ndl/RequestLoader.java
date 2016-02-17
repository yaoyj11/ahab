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
package orca.ahab.libndl.ndl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Set;

import orca.ahab.libndl.Request;
import orca.ahab.libndl.Slice;
import orca.ahab.libndl.resources.request.ComputeNode;
import orca.ahab.libndl.resources.request.InterfaceNode2Net;
import orca.ahab.libndl.resources.request.Network;
import orca.ahab.libndl.resources.request.Node;
import orca.ahab.libndl.resources.request.RequestReservationTerm;
import orca.ahab.libndl.resources.request.RequestResource;
import orca.ahab.libndl.resources.request.StitchPort;
import orca.ahab.libndl.resources.request.StorageNode;
import orca.ndl.INdlRequestModelListener;
import orca.ndl.NdlCommons;
import orca.ndl.NdlRequestParser;

import org.apache.commons.lang.StringUtils;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

public class RequestLoader implements INdlRequestModelListener {
	
	private Slice slice;
	private Request request;
	private RequestReservationTerm term = new RequestReservationTerm();

	public RequestLoader(Slice slice, Request request){
		this.request = request;
		this.slice =slice;
	}
    
	/**
	 * Load from file
	 * @param f
	 * @return
	 */
	public String loadGraph(File f) {
		BufferedReader bin = null; 
		String rawRDF = null;
		try {
			FileInputStream is = new FileInputStream(f);
			bin = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			
			StringBuilder sb = new StringBuilder();
		
			String line = null;
			while((line = bin.readLine()) != null) {
				sb.append(line);
				// re-add line separator
				sb.append(System.getProperty("line.separator"));
			}
			
			bin.close();
			
			rawRDF = sb.toString();
			
			NdlRequestParser nrp = new NdlRequestParser(sb.toString(), this);
			request.logger().debug("Parsing request");
			nrp.doLessStrictChecking(); //TODO: Should be removed...
			nrp.processRequest();
			
			nrp.freeModel();
			
		} catch (Exception e) {
			request.logger().debug("error loading graph");
			request.logger().error(e);
			e.printStackTrace();
			return null;
		} 
		
		return rawRDF;
	}
	
	/**
	 * Load from string
	 * @param f
	 * @return
	 */
	public boolean load(String rdf) {
		try {
			NdlRequestParser nrp = new NdlRequestParser(rdf, this);
			nrp.doLessStrictChecking(); //TODO: Should be removed...
			nrp.processRequest();
			nrp.freeModel();
			
		} catch (Exception e) {
			request.logger().error(e);
			request.logger().debug("error loading graph");
			return false;
		} 
		
		return true;
	}

	
	

	public void ndlReservation(Resource i, final OntModel m) {
		
		request.logger().debug("Reservation: " + i + ", sliceState(Request:ndlReservation) = " + NdlCommons.getGeniSliceStateName(i));
		// try to extract the guid out of the URL
		String u = i.getURI();
		String guid = StringUtils.removeEnd(StringUtils.removeStart(u, NdlCommons.ORCA_NS), "#");
		
		this.request.setNsGuid(guid);
		
		this.slice.setState(NdlCommons.getGeniSliceStateName(i));
		
		/*if (i != null) {
			reservationDomain = RequestSaver.reverseLookupDomain(NdlCommons.getDomain(i));
			this.request.setOFVersion(NdlCommons.getOpenFlowVersion(i));
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

	public void ndlNode(Resource ce, OntModel om, Resource ceClass, List<Resource> interfaces) {
		request.logger().debug("Node: " + ce + " of class " + ceClass);
	
		
		if (ce == null)
			return;
		
		Node newNode;
		ComputeNode newComputeNode = null;
		if (ceClass.equals(NdlCommons.computeElementClass)){
			if(!ce.hasProperty(NdlCommons.manifestHasParent)){
				request.logger().debug("Node: " + ce.getLocalName() + " : found computeElementClass, parent = " + ce.hasProperty(NdlCommons.manifestHasParent));
			
				newNode = this.request.addComputeNode(ce.getLocalName());
				newComputeNode = (ComputeNode)newNode;
				newNode.setModelResource(ce);
			} else {
				request.logger().debug("Node: " + ce.getLocalName() + " : found computeElementClass without parent, skipping!");
				return;
			}
		} else { 
			if (ceClass.equals(NdlCommons.serverCloudClass)) {
				request.logger().debug("Node: " + ce.getLocalName() + " : found serverCloudClass, parent = " + ce.hasProperty(NdlCommons.manifestHasParent));
				ComputeNode newNodeGroup = this.request.addComputeNode(ce.getLocalName());
				newComputeNode = newNodeGroup;
				//int ceCount = NdlCommons.getNumCE(ce);
				//if (ceCount > 0) newNodeGroup.setNodeCount(ceCount);
				newNodeGroup.initializeNodeCount(0);
				//newNodeGroup.setSplittable(NdlCommons.isSplittable(ce));
				newNode = newNodeGroup;
				newNode.setModelResource(ce);
				
				String groupUrl = NdlCommons.getRequestGroupURLProperty(ce);
				request.logger().debug("NdlCommons.getRequestGroupURLProperty: " + groupUrl);
				
				String nodeUrl = ce.getURI();
				request.logger().debug("URI: " + nodeUrl);
				
				
				
			} else if (NdlCommons.isStitchingNode(ce)) {
				// stitching node
				// For some reason the properties of the stitchport are stored on the interface (not here)
				StitchPort sp = this.request.addStitchPort(ce.getLocalName());
				newNode = sp;
			} else if (NdlCommons.isNetworkStorage(ce)) {
				// storage node
				StorageNode snode = this.request.addStorageNode(ce.getLocalName());
				snode.setCapacity(NdlCommons.getResourceStorageCapacity(ce));
				newNode = snode;
			} else // default just a node
				newNode = this.request.addComputeNode(ce.getLocalName());
		}

		request.logger().debug("about to load domain");
		Resource domain = NdlCommons.getDomain(ce);
		if (domain != null){
			request.logger().debug("load domain: " + RequestSaver.reverseLookupDomain(domain));
			newNode.setDomain(RequestSaver.reverseLookupDomain(domain));
		}
			
		
		if (ceClass.equals(NdlCommons.computeElementClass) || ceClass.equals(NdlCommons.serverCloudClass)){
			Resource ceType = NdlCommons.getSpecificCE(ce);
			if (ceType != null){
				newComputeNode.setNodeType(RequestSaver.reverseNodeTypeLookup(ceType));
			}
		}

		//process image
		request.logger().debug("about to load image");
		if (ceClass.equals(NdlCommons.computeElementClass) || ceClass.equals(NdlCommons.serverCloudClass)){
			request.logger().debug("about to load domain: it is a compute element");
			// disk image
			Resource di = NdlCommons.getDiskImage(ce);
			if (di != null) {
				request.logger().debug("about to load domain: it has a image");
				try {
					String imageURL = NdlCommons.getIndividualsImageURL(ce);
					String imageHash = NdlCommons.getIndividualsImageHash(ce);
					//String imName = this.request.addImage(new OrcaImage(di.getLocalName(), 
					//		new URL(imageURL), imageHash), null);
					//String imName = imageURL + imageHash;  //FIX ME: not right
					String imName = newComputeNode.getName() + "-image"; //FIX ME: not right:  why do we even have an image name???
					newComputeNode.setImage(imageURL,imageHash,imName);
				} catch (Exception e) {
					// FIXME: ?
					request.logger().debug("about to load domain: hit an exception");
					;
				}
			}

			// post boot script
			String script = NdlCommons.getPostBootScript(ce);
			if ((script != null) && (script.length() > 0)) {
				newComputeNode.setPostBootScript(script);
			}
		}
	}

	/**
	 * For now deals only with p-to-p connections
	 */
	public void ndlNetworkConnection(Resource l, OntModel om, 
			long bandwidth, long latency, List<Resource> interfaces) {
		
		request.logger().debug("NetworkConnection: " + l);
		
		// request.logger().debug("Found connection " + l + " connecting " + interfaces + " with bandwidth " + bandwidth);
		if (l == null)
			return;
		
		Network ol = this.request.addLink(l.getLocalName());
		ol.setBandwidth(bandwidth);
		ol.setLatency(latency);
		ol.setLabel(NdlCommons.getLayerLabelLiteral(l));
	}

	public void ndlInterface(Resource intf, OntModel om, Resource conn, Resource node, String ip, String mask) {
	
		request.logger().debug("Interface: " + intf + " link: " + conn + " node: " + node);
		if(intf == null){
			return;
		}
		
		
		RequestResource onode = null;
		if(node != null){
			onode = this.request.getResourceByName(node.getLocalName());
		} else {
			request.logger().warn("ndlInterface with null node: " + intf);
		}
		RequestResource olink = null;
		if(conn != null){
			olink = this.request.getResourceByName(conn.getLocalName());
		} else{
			request.logger().warn("ndlInterface with null connection: " + intf);
		}
		
		if(onode == null){
			request.logger().warn("ndlInterface with null missing node:  Interface: " + intf + ", Node: " + node);
			return;
		}
		
		//ComputeNode
		if(onode instanceof ComputeNode && olink instanceof Network){
			request.logger().debug("stitching compute node");
			InterfaceNode2Net stitch = (InterfaceNode2Net)onode.stitch(olink);
			stitch.setIpAddress(ip);  
			stitch.setNetmask(mask);
			return;
		} 
		
		//StorageNode
		if(onode instanceof StorageNode){
			request.logger().debug("stitching storage node");
			InterfaceNode2Net stitch = (InterfaceNode2Net)onode.stitch(olink);
			
			return;
		}
		
		//StitchPort
		if(onode instanceof StitchPort){
			request.logger().debug("stitching stitchport");
			//Why is this stuff stored in the interface? 
			//It Seems like they are properties of the stitchport itself. 
			StitchPort sp = (StitchPort)onode;
			sp.setPort(intf.toString());
			sp.setLabel(NdlCommons.getLayerLabelLiteral(intf));
			
			InterfaceNode2Net stitch = (InterfaceNode2Net)onode.stitch(olink);
			return;
		}	
		
		//shouldnt get here
		request.logger().debug("Unknown node type: " + node + ", " + node.getClass());
	}
	
	public void ndlSlice(Resource sl, OntModel m) {
		
		request.logger().debug("Slice: " + sl + ", sliceState(request) = " + NdlCommons.getGeniSliceStateName(sl));
		// check that this is an OpenFlow slice and get its details
		if (sl.hasProperty(NdlCommons.RDF_TYPE, NdlCommons.ofSliceClass)) {
			Resource ofCtrl = NdlCommons.getOfCtrl(sl);
			if (ofCtrl == null)
				return;
			this.request.setOfCtrlUrl(NdlCommons.getURL(ofCtrl));
			this.request.setOfUserEmail(NdlCommons.getEmail(sl));
			this.request.setOfSlicePass(NdlCommons.getSlicePassword(sl));
			if ((this.request.getOfUserEmail() == null) ||
					(this.request.getOfSlicePass() == null) ||
					(this.request.getOfCtrlUrl() == null)) {
					// disable OF if invalid parameters
					//this.request.setNoOF();
					this.request.setOfCtrlUrl(null);
					this.request.setOfSlicePass(null);
					this.request.setOfUserEmail(null);
			}
		}	
	}

	public void ndlReservationResources(List<Resource> res, OntModel m) {
		// nothing to do here in this case
	}
	
	public void ndlParseComplete() {
		request.logger().debug("Done parsing.");
		// set term etc
		this.request.setTerm(term);
		//this.request.setDomainInReservation(reservationDomain);
	}

	public void ndlNodeDependencies(Resource ni, OntModel m, Set<Resource> dependencies) {
		request.logger().debug("nlNodeDependencies -- SKIPPED");
		
		/*OrcaNode mainNode = nodes.get(ni.getURI());
		if ((mainNode == null) || (dependencies == null))
			return;
		for(Resource r: dependencies) {
			OrcaNode depNode = nodes.get(r.getURI());
			if (depNode != null)
				mainNode.addDependency(depNode);
		}*/
	}

	/**
	 * Process a broadcast link
	 */
	public void ndlBroadcastConnection(Resource bl, OntModel om,
			long bandwidth, List<Resource> interfaces) {
		
		request.logger().debug("BroadcastConnection: " + bl);
		
		Network ol = this.request.addLink(bl.getLocalName());
		ol.setBandwidth(bandwidth);
		//ol.setLatency(latency);
		ol.setLabel(NdlCommons.getLayerLabelLiteral(bl));	
	}
}
