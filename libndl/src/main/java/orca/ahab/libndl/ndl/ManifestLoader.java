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

import org.apache.commons.lang.StringUtils;

import orca.ahab.libndl.LIBNDL;
import orca.ahab.libndl.Slice;
import orca.ahab.libndl.SliceGraph;
import orca.ahab.libndl.resources.manifest.LinkConnection;
import orca.ahab.libndl.resources.manifest.ManifestResource;
import orca.ahab.libndl.resources.request.ComputeNode;
import orca.ahab.libndl.resources.request.Interface;
import orca.ahab.libndl.resources.request.Node;
import orca.ahab.libndl.resources.request.RequestResource;
import orca.ahab.libndl.resources.request.StitchPort;
import orca.ahab.libndl.resources.request.StorageNode;
import orca.ndl.INdlManifestModelListener;
import orca.ndl.NdlCommons;
import orca.ndl.NdlManifestParser;
import orca.ndl.NdlRequestParser;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.uci.ics.jung.graph.SparseMultigraph;

/**
 * Class for loading manifests
 * @author ibaldin
 *
 */
public class ManifestLoader extends NDLLoader implements INdlManifestModelListener{
	private SliceGraph sliceGraph;
	private ExistingSliceModel ndlModel;
	
	public ManifestLoader(SliceGraph sliceGraph, ExistingSliceModel ndlModel){
		LIBNDL.logger().debug("new ManifestLoader");
		this.sliceGraph = sliceGraph;
		this.ndlModel = ndlModel;
	}
	

	
	public NdlManifestParser load(String rdf) {
		NdlManifestParser nmp = null;
		try {
			LIBNDL.logger().debug("About to parse manifest");
			
			// parse as manifest
			nmp = new NdlManifestParser(rdf, this);
			
			nmp.processManifest();	
			//nmp.freeModel();			
			//manifest.setManifestTerm(creationTime, expirationTime);
			
		} catch (Exception e) {
			LIBNDL.logger().debug("Excpetion: parsing request part of manifest" + e);
		} 
		return nmp;
	}


	
	
	/* Callbacks for Manifest mode */
	public void ndlInterface(Resource l, OntModel om, Resource conn,
			Resource node, String ip, String mask) {
		// TODO Auto-generated method stub
		String printStr =  "ndlManifest_Interface: \n\tName: " + l;
		printStr += "\n\tconn: " + conn;
		printStr += "\n\tnode: " + node;
		LIBNDL.logger().debug(printStr);
	}
	public void ndlNetworkConnection(Resource l, OntModel om,
			long bandwidth, long latency, List<Resource> interfaces) {
		
		//should be sliceGraph.add... and/or manifestGraph.add...
		//manifest.addNetworkConnection(l.toString());

		String printStr = "ndlManifest_NetworkConnection: \n\tName: " + l.toString() + " (" + l.getLocalName() + ")";
		printStr += "\n\tInterfaces:";
		for (Resource r : interfaces){
			printStr += "\n\t\t " + r;
		}			

		LIBNDL.logger().debug(printStr);
	}
	public void ndlParseComplete() {		
		String printStr = "ndlManifest_ParseComplete";
		LIBNDL.logger().debug(printStr);
	}
	public void ndlReservation(Resource i, OntModel m) {
		// TODO Auto-generated method stub
		String printStr = "ndlManifest_Reservation: \n\tName: " + i;
		printStr += ", sliceState(Manifest:ndlReservation) = " + NdlCommons.getGeniSliceStateName(i);
		LIBNDL.logger().debug(printStr);
		
		
	}
	public void ndlReservationTermDuration(Resource d, OntModel m,
			int years, int months, int days, int hours, int minutes, int seconds) {
		// TODO Auto-generated method stub
		String printStr = "ndlManifest_ReservationTermDuration: \n\tName: " + d;
		LIBNDL.logger().debug(printStr);
	}
	public void ndlReservationResources(List<Resource> r, OntModel m) {
		// TODO Auto-generated method stub
		String printStr = "ndlManifest_ReservationResources: \n\tName: " + r;
		LIBNDL.logger().debug(printStr);
	}
	public void ndlReservationStart(Literal s, OntModel m, Date start) {
		// TODO Auto generated method stub
		String printStr = "ndlManifest_ReservationStart: \n\tName: " + s ;
		LIBNDL.logger().debug(printStr);
	}
	public void ndlReservationEnd(Literal e, OntModel m, Date end) {
		// TODO Auto-generated method stub
		String printStr = "ndlManifest_ReservationEnd: \n\tNameg.: " + e;
		LIBNDL.logger().debug(printStr);
	}// parse as request
	
	public void ndlNodeDependencies(Resource ni, OntModel m,
			Set<Resource> dependencies) {
		// TODO Auto-generated method stub
		String printStr = "ndlManifest_NodeDependencies: \n\tName: " + ni;
		LIBNDL.logger().debug(printStr);
	}
	public void ndlSlice(Resource sl, OntModel m) {
		// TODO Auto-generated method stub
		String printStr = "ndlManifest_Slice: \n\tName: " + sl;
		printStr += ", sliceState(manifest) = " + NdlCommons.getGeniSliceStateName(sl);
		LIBNDL.logger().debug(printStr);
	}
	public void ndlBroadcastConnection(Resource bl, OntModel om,
			long bandwidth, List<Resource> interfaces) {
		// TODO Auto-generated method stub
		String printStr = "ndlManifest_BroadcastConnection: ************* SHOULD NEVER HAPPEN ******************* \n\tName: " + bl; 
		printStr += "\n\tInterfaces:";
		for (Resource r : interfaces){
			printStr += "\n\t\t " + r;
		}
		LIBNDL.logger().debug(printStr);
	}
	public void ndlManifest(Resource i, OntModel m) {		
		String printStr = "ndlManifest_Manifest: \n\tName: " + i;
		printStr += ", sliceState = " + NdlCommons.getGeniSliceStateName(i);
		LIBNDL.logger().debug(printStr);
		
	}
	public void ndlLinkConnection(Resource l, OntModel m,
			List<Resource> interfaces, Resource parent) {// parse as request
		
		String printStr = "ndlManifest_LinkConnection: \n\tNameg.: " + l +  "\n\tparent: " + parent; 
		printStr += "\n\tInterfaces:";
		for (Resource r : interfaces){
			printStr += "\n\t\t " + r;
		}
		LIBNDL.logger().debug(printStr);
		
		//should be sliceGraph and/or manifestGraph
		//LinkConnection lc = manifest.addLinkConnection(l.toString());
		//lc.setModelResource(l);		
	}	
	public void ndlCrossConnect(Resource c, OntModel m, long bw,
			String label, List<Resource> interfaces, Resource parent) {
		//should be sliceGraph and/or manifestGraph
		//manifest.addCrossConnect(c.toString());
		
		String printStr = "ndlManifest_CrossConnect: \n\tName: " + c +  "\n\tparent: " + parent; 
		printStr += "\n\tInterfaces:";
		for (Resource r : interfaces){
			printStr += "\n\t\t " + r;
		}
		LIBNDL.logger().debug(printStr);
	}	
	public void ndlNetworkConnectionPath(Resource c, OntModel m,
			List<List<Resource>> path, List<Resource> roots) {
		// TODO Auto-generated method stub
		String printStr = "ndlManifest_NetworkConnectionPath: \n\tName: " + c;
		LIBNDL.logger().debug(printStr);
	
	}
	public void ndlNode(Resource ce, OntModel om, Resource ceClass,
			List<Resource> interfaces) {
		LIBNDL.logger().debug("\n\n\n #################################### Processing Node ############################################## \n\n\n");
		if (ce == null)
			return;
		String printStr = "ndlManifest_Node: ("+ ceClass  + ")\n\tName: " + ce + " (" + ce.getLocalName() + ")"; 
		printStr += ", state = " + NdlCommons.getResourceStateAsString(ce);
		printStr += "\n\tInterfaces:";
		for (Resource r : interfaces){
			printStr += "\n\t\t " + r;
		}
		LIBNDL.logger().debug(printStr);
	
		if (NdlCommons.isStitchingNodeInManifest(ce)) {
		//if(NdlCommons.isStitchingNode(ce)){
			LIBNDL.logger().debug("\n\n\n ************************************** FOUND STITCHPORT NODE *************************************** \n\n\n");
			LIBNDL.logger().debug("Found a stitchport");
			String label = NdlCommons.getLayerLabelLiteral(interfaces.get(0));
			String port = NdlCommons.getLinkTo(interfaces.get(0)).toString();
			StitchPort newStitchport = this.sliceGraph.buildStitchPort(getPrettyName(ce),label,port);
			
			ndlModel.mapSliceResource2ModelResource(newStitchport, ce);
			
			return;
		}
		if(NdlCommons.isNetworkStorage(ce)){
			LIBNDL.logger().debug("\n\n\n ************************************** FOUND STORAGE NODE *************************************** \n\n\n");
			LIBNDL.logger().debug("Found a storage node, returning");
			StorageNode newStorageNode = this.sliceGraph.buildStorageNode(getPrettyName(ce));
			ndlModel.mapSliceResource2ModelResource(newStorageNode, ce);
			
			return;
		}
	
		
		//Only handle compute nodes for now.
		if ((ceClass.equals(NdlCommons.computeElementClass) || ceClass.equals(NdlCommons.serverCloudClass))){
			LIBNDL.logger().debug("\n\n\n ************************************** FOUND COMPUTE NODE *************************************** \n\n\n");
			ComputeNode newNode = this.sliceGraph.buildComputeNode(getPrettyName(ce));
			ndlModel.mapSliceResource2ModelResource(newNode, ce);
			
			newNode.setPostBootScript(NdlCommons.getPostBootScript(ce));
			

			
			
			
			//LIBNDL.logger().debug("modelNode: " + ndlModel.getModelResource(newNode).getLocalName());
			//String groupUrl = NdlCommons.getRequestGroupURLProperty(ce);
			//LIBNDL.logger().debug("NdlCommons.getRequestGroupURLProperty: " + groupUrl);
			//String nodeUrl = ce.getURI();
			//LIBNDL.logger().debug("ce.getURI(): " + nodeUrl);
			return;
		}
		
	}


	// sometimes getLocalName is not good enough
	// so we strip off orca name space and call it a day
	private String getTrueName(Resource r) {
		if (r == null)
			return null;
		
		return StringUtils.removeStart(r.getURI(), NdlCommons.ORCA_NS);
	}
	
	private String getPrettyName(Resource r) {
		String rname = getTrueName(r);
		int ind = rname.indexOf('#');
		if (ind > 0) {
			rname = rname.substring(ind + 1);
		}
		return rname;
	}

	/*
	// get domain name from inter-domain resource name
	private String getInterDomainName(Resource r) {
		String trueName = getTrueName(r);
		
		if (r == null)
			return null;
		
		String[] split = trueName.split("#");
		if (split.length >= 2) {
			String rem = split[1];
			String[] split1 = rem.split("/");
			return split1[0];
		}	
		return null;
	}
	
	public void ndlLinkConnection(Resource l, OntModel m,
			List<Resource> interfaces, Resource parent) {
		//System.out.println("Found link connection " + l + " connecting " + interfaces);
		assert(l != null);
		
		// ignore request items
		if (requestPhase)
			return;
		
		NDLLIB.logger().debug("Link Connection: " + l);
		
		// find what nodes it connects (should be two)
		Iterator<Resource> it = interfaces.iterator(); 
		
		String label = NdlCommons.getResourceLabel(l);
		
		if (interfaces.size() == 2) {
			NDLLIB.logger().debug("  Adding p-to-p link");
			OrcaLink ol = Manifest.getInstance().getLinkCreator().create(getPrettyName(l), NdlCommons.getResourceBandwidth(l));
			ol.setLabel(label);

			// maybe point-to-point link
			// the ends
			Resource if1 = it.next(), if2 = it.next();
			
			if ((if1 != null) && (if2 != null)) {
				OrcaNode if1Node = interfaceToNode.get(getTrueName(if1));
				OrcaNode if2Node = interfaceToNode.get(getTrueName(if2));
				
				if ((if1Node != null) && if1Node.equals(if2Node)) {
					// degenerate case of a node on a shared vlan
					OrcaCrossconnect oc = new OrcaCrossconnect(getPrettyName(l));
					oc.setLabel(label);
					oc.setDomain(RequestSaver.reverseLookupDomain(NdlCommons.getDomain(l)));
					nodes.put(getTrueName(l), oc);
					// save one interface
					interfaceToNode.put(getTrueName(if1), oc);
					Manifest.getInstance().getGraph().addVertex(oc);
					return;
				}
				
				// get the bandwidth of crossconnects if possible
				long bw1 = 0, bw2 = 0;
				if (if1Node instanceof OrcaCrossconnect) {
					OrcaCrossconnect oc = (OrcaCrossconnect)if1Node;
					bw1 = oc.getBandwidth();
				} 
				if (if2Node instanceof OrcaCrossconnect) {
					OrcaCrossconnect oc = (OrcaCrossconnect)if2Node;
					bw2 = oc.getBandwidth();
				}
				ol.setBandwidth(bw1 > bw2 ? bw1 : bw2);
				
				// have to be there
				if ((if1Node != null) && (if2Node != null)) {
					NDLLIB.logger().debug("  Creating a link " + ol.getName() + " from " + if1Node + " to " + if2Node);
					Manifest.getInstance().getGraph().addEdge(ol, new Pair<OrcaNode>(if1Node, if2Node), 
							EdgeType.UNDIRECTED);
				}
			}
			// state
			ol.setState(NdlCommons.getResourceStateAsString(l));
			
			if (ol.getState() != null)
				ol.setIsResource();
			
			// reservation notice
			ol.setReservationNotice(NdlCommons.getResourceReservationNotice(l));
			links.put(getTrueName(l), ol);
		} else {			
			NDLLIB.logger().debug("  Adding multi-point crossconnect " + getTrueName(l) + " (has " + interfaces.size() + " interfaces)");
			// multi-point link
			// create a crossconnect then use interfaceToNode mapping to create links to it
			OrcaCrossconnect ml = new OrcaCrossconnect(getPrettyName(l));

			ml.setLabel(label);
			ml.setReservationNotice(NdlCommons.getResourceReservationNotice(l));
			ml.setState(NdlCommons.getResourceStateAsString(l));
			ml.setDomain(RequestSaver.reverseLookupDomain(NdlCommons.getDomain(l)));

			if (ml.getState() != null)
				ml.setIsResource();
			
			nodes.put(getTrueName(l), ml);
			
			// remember the interfaces
			while(it.hasNext()) {
				Resource intR = it.next();
				NDLLIB.logger().debug("  Remembering interface " + intR + " of " + ml);
				interfaceToNode.put(getTrueName(intR), ml);
			}
			
			// add crossconnect to the graph
			Manifest.getInstance().getGraph().addVertex(ml);
			
			// link to this later from interface information
			
			// link nodes (we've already seen them) to it
//			for(Resource intf: interfaces) {
//				if (interfaceToNode.get(getTrueName(intf)) != null) {
//					NDLLIB.logger().debug("  Creating a link " + lcount + " from " + ml + " to " + interfaceToNode.get(getTrueName(intf)));
//					OrcaLink ol = new OrcaLink("Link " + lcount++);
//					NDLLIBManifestState.getInstance().getGraph().addEdge(ol, new Pair<OrcaNode>(ml, interfaceToNode.get(getTrueName(intf))), EdgeType.UNDIRECTED);
//				}
//			}
		}
	}

	public void ndlManifest(Resource i, OntModel m) {
		// nothing to do in this case
		
		// ignore request items
		if (requestPhase)
			return;
		
		NDLLIB.logger().debug("Manifest: " + i);
	}

	public void ndlInterface(Resource intf, OntModel om, Resource conn,
			Resource node, String ip, String mask) {
		
		// ignore request items
		if (requestPhase)
			return;
		
		// System.out.println("Interface " + l + " has IP/netmask" + ip + "/" + mask);
		NDLLIB.logger().debug("Interface " + intf + " between " + node + " and " + conn + " has IP/netmask " + ip + "/" + mask);
		
		if (intf == null)
			return;
		OrcaNode on = null;
		OrcaLink ol = null;
		OrcaCrossconnect crs = null;
		if (node != null)
			on = nodes.get(getTrueName(node));
		
		if (conn != null) {
			ol = links.get(getTrueName(conn));
			if (ol == null) 
				// maybe it is a crossconnect and not a link connection
				crs = (OrcaCrossconnect)nodes.get(getTrueName(conn));
		}
		
		// extract the IP address from label, if it is not set on
		// the interface in the request (basically we favor manifest
		// setting over the request because in node groups that's the
		// correct one)
		String nmInt = null;
		if (ip == null) {
			String ifIpLabel = NdlCommons.getLabelID(intf);
			// x.y.z.w/24
			if (ifIpLabel != null) {
				String[] ipnm = ifIpLabel.split("/");
				if (ipnm.length == 2) {
					ip = ipnm[0];
					nmInt = ipnm[1];
				}
			}
		} else {
			if (mask != null)
				nmInt = "" + RequestSaver.netmaskStringToInt(mask);
		}
		
		if (on != null) {
			if (ol != null) {
				on.setIp(ol, ip, nmInt);
				on.setInterfaceName(ol, getTrueName(intf));
				on.setMac(ol, NdlCommons.getAddressMAC(intf));
			} else if (crs != null) {
				if (intf.toString().matches(node.toString() + "/IP/[0-9]+")) {
					// include only interfaces that have nodename/IP/<number> format - those
					// are generated by Yufeng. 

					// create link from node to crossconnect and assign IP if it doesn't exist
					NDLLIB.logger().debug("  Creating a link  from " + on + " to " + crs);
					ol = Manifest.getInstance().getLinkCreator().create("Unnamed");
					Manifest.getInstance().getGraph().addEdge(ol, new Pair<OrcaNode>(on, crs), 
							EdgeType.UNDIRECTED);
					on.setIp(ol, ip, nmInt);
					on.setMac(ol, NdlCommons.getAddressMAC(intf));
				}
			}
			else {
				// this could be a disconnected node group
				if (on instanceof OrcaComputeNode) {
					OrcaComputeNode ong = (OrcaComputeNode)on;
					ong.setInternalIp(ip, "" + RequestSaver.netmaskStringToInt(mask));
				}
			}
		}
	}

	public void ndlNetworkConnection(Resource l, OntModel om, long bandwidth,
			long latency, List<Resource> interfaces) {
		
		// ignore request items
		if (requestPhase)
			return;
		
		// nothing to do in this case
		NDLLIB.logger().debug("Network Connection: " + l);

	}

	public void ndlCrossConnect(Resource c, OntModel m, 
			long bw, String label, List<Resource> interfaces, Resource parent) {
		
		// ignore request items
		if (requestPhase)
			return;
		
		if (c == null)
			return;

		NDLLIB.logger().debug("CrossConnect: " + c);
		
		OrcaCrossconnect oc = new OrcaCrossconnect(getPrettyName(c));
		oc.setLabel(label);
		
		setCommonNodeProperties(oc, c);
		
		// later set bandwidth on adjacent links (crossconnects in NDL have
		// bandwidth but for users we'll show it on the links)
		oc.setBandwidth(bw);
		
		// process interfaces
		for (Iterator<Resource> it = interfaces.iterator(); it.hasNext();) {
			Resource intR = it.next();
			interfaceToNode.put(getTrueName(intR), oc);
		}
		
		nodes.put(getTrueName(c), oc);
		
		// add nodes to the graph
		Manifest.getInstance().getGraph().addVertex(oc);
	}
	
	public void ndlNode(Resource ce, OntModel om, Resource ceClass,
			List<Resource> interfaces) {
		
		// ignore request items
		if (requestPhase)
			return;
		
		if (ce == null)
			return;
		
		NDLLIB.logger().debug("Node: " + ce);
		
		OrcaNode newNode;
		
		if (NdlCommons.isStitchingNodeInManifest(ce)) {
			NDLLIB.logger().debug("  is a stitching port");
			OrcaStitchPort sp = new OrcaStitchPort(getPrettyName(ce));
			// get the interface (first)
			if (interfaces.size() == 1) {
				sp.setLabel(NdlCommons.getLayerLabelLiteral(interfaces.get(0)));
				if (NdlCommons.getLinkTo(interfaces.get(0)) != null)
					sp.setPort(NdlCommons.getLinkTo(interfaces.get(0)).toString());
			} 
			newNode = sp;
		} else if (NdlCommons.isNetworkStorage(ce)) {
			NDLLIB.logger().debug("  is a storage node");
			newNode = new OrcaStorageNode(getPrettyName(ce));
			newNode.setIsResource();
		} else if (NdlCommons.isMulticastDevice(ce)) {
			NDLLIB.logger().debug("  is a multicast root");
			newNode = new OrcaCrossconnect(getPrettyName(ce));
			newNode.setIsResource();
		} else {
			NDLLIB.logger().debug("  is a regular node");
			newNode = new OrcaNode(getPrettyName(ce));
		}
		
		for (Resource ii: interfaces)
			NDLLIB.logger().debug("  With interface " + ii);
		
		// set common properties
		setCommonNodeProperties(newNode, ce);
		
		// process interfaces
		for (Iterator<Resource> it = interfaces.iterator(); it.hasNext();) {
			Resource intR = it.next();
			interfaceToNode.put(getTrueName(intR), newNode);
		}
		
		// disk image
		Resource di = NdlCommons.getDiskImage(ce);
		if (di != null) {
			try {
				String imageURL = NdlCommons.getIndividualsImageURL(ce);
				String imageHash = NdlCommons.getIndividualsImageHash(ce);
				Request.getInstance().addImage(new OrcaImage(di.getLocalName(), 
						new URL(imageURL), imageHash), null);
				//newNode.setImage(di.getLocalName());
			} catch (Exception e) {
				// FIXME: ?
				;
			}
		}
		
		nodes.put(getTrueName(ce), newNode);
		
		// add nodes to the graph
		Manifest.getInstance().getGraph().addVertex(newNode);
		
		// are there nodes hanging off of it as elements? if so, link them in
		processDomainVmElements(ce, om, newNode);
	}

	// add collection elements
	private void processDomainVmElements(Resource vm, OntModel om, OrcaNode parent) {
		
		// HACK - if we added real interfaces to inner nodes, we don't need link to parent
		boolean innerNodeConnected = false;
		
		for (StmtIterator vmEl = vm.listProperties(NdlCommons.collectionElementProperty); vmEl.hasNext();) {
			Resource tmpR = vmEl.next().getResource();
			OrcaNode on = new OrcaNode(getTrueName(tmpR), parent);
			nodes.put(getTrueName(tmpR), on);
			Manifest.getInstance().getGraph().addVertex(on);
			OrcaLink ol = Manifest.getInstance().getLinkCreator().create("Unnamed");
			
			// link to parent (a visual HACK)
			links.put(ol.getName(), ol);
			Manifest.getInstance().getGraph().addEdge(ol, new Pair<OrcaNode>(parent, on), 
					EdgeType.UNDIRECTED);
			
			// add various properties
			setCommonNodeProperties(on, tmpR);
			
			// process interfaces. if there is an interface that leads to
			// a link, this is an intra-domain case, so we can delete the parent later
			for (Resource intR: NdlCommons.getResourceInterfaces(tmpR)) {
				interfaceToNode.put(getTrueName(intR), on);
				// HACK: for now check that this interface connects to something
				// and is not just hanging there with IP address
				List<Resource> hasI = NdlCommons.getWhoHasInterface(intR, om);
				if (hasI.size() > 1)
					innerNodeConnected = true;
			}
		}
		
		// Hack - remove parent if nodes are linked between themselves
		if (innerNodeConnected)
			Manifest.getInstance().getGraph().removeVertex(parent);
	}

	// set common node properties from NDL
	private void setCommonNodeProperties(Node on, Resource nr) {
		// post boot script
		on.setPostBootScript(NdlCommons.getPostBootScript(nr));
		
		// management IP/port access
		on.setManagementAccess(NdlCommons.getNodeServices(nr));
		
		// state
		on.setState(NdlCommons.getResourceStateAsString(nr));
		
		if (on.getState() != null) {
			on.setIsResource();
		}
		
		// reservation notice
		on.setReservationNotice(NdlCommons.getResourceReservationNotice(nr));
		
		// domain
		Resource domain = NdlCommons.getDomain(nr);
		if (domain != null)
			on.setDomain(RequestSaver.reverseLookupDomain(domain));
		
		// url
		on.setUrl(nr.getURI());
		
		// group (if any)
		String groupUrl = NdlCommons.getRequestGroupURLProperty(nr);
		// group URL same as my URL means I'm a single node
		if ((groupUrl != null) &&
				groupUrl.equals(on.getUrl()))
			groupUrl = null;
		on.setGroup(groupUrl);
		
		// specific ce type
		Resource ceType = NdlCommons.getSpecificCE(nr);
		if (ceType != null)
			on.setNodeType(RequestSaver.reverseNodeTypeLookup(ceType));
		
		// substrate info if present
		if (NdlCommons.getEC2WorkerNodeId(nr) != null)
			on.setSubstrateInfo("worker", NdlCommons.getEC2WorkerNodeId(nr));
		if (NdlCommons.getEC2InstanceId(nr) != null)
			on.setSubstrateInfo("instance", NdlCommons.getEC2InstanceId(nr));
		
	}
	
	public void ndlParseComplete() {
		// ignore request items
		if (requestPhase)
			return;
		
		// nothing to do in this case
		NDLLIB.logger().debug("Parse complete.");
	}

	public void ndlNetworkConnectionPath(Resource c, OntModel m,
			List<List<Resource>> path, List<Resource> roots) {

		// ignore request items
		if (requestPhase)
			return;

		// nothing to do in this case
		NDLLIB.logger().debug("Network Connection Path: " + c);
		if (path != null) {
			NDLLIB.logger().debug("Printing paths");
			StringBuilder sb =  new StringBuilder();
			for (List<Resource> p: path) {
				sb.append("   Path: ");
				for (Resource r: p) {
					sb.append(r + " ");
				}
				NDLLIB.logger().debug(sb.toString());
			}
		} else 
			NDLLIB.logger().debug("   None");
	} 

	*//**
	 * Request items - mostly ignored
	 * 
	 *//*
	
	
	public void ndlBroadcastConnection(Resource bl, OntModel om,
			long bandwidth, List<Resource> interfaces) {
		// TODO Auto-generated method stub
		
	}

	
	public void ndlNodeDependencies(Resource ni, OntModel m,
			Set<Resource> dependencies) {
		// TODO Auto-generated method stub
		
	}

	
	public void ndlReservation(Resource i, OntModel m) {
		// TODO Auto-generated method stub
		
	}

	
	public void ndlReservationEnd(Literal e, OntModel m, Date end) {
		expirationTime = end;
		
	}

	
	public void ndlReservationResources(List<Resource> r, OntModel m) {
		// TODO Auto-generated method stub
		
	}

	
	public void ndlReservationStart(Literal s, OntModel m, Date start) {
		creationTime = start;
		
	}

	
	public void ndlReservationTermDuration(Resource d, OntModel m, int years,
			int months, int days, int hours, int minutes, int seconds) {
		if (creationTime == null)
			return;
		if ((years == 0) && (months == 0) && (days == 0) && (hours == 0) && (minutes == 0) && (seconds == 0))
			return;
		Calendar cal = Calendar.getInstance();
		cal.setTime(creationTime);
		cal.add(Calendar.YEAR, years);
		cal.add(Calendar.MONTH, months);
		cal.add(Calendar.DAY_OF_YEAR, days);
		cal.add(Calendar.HOUR, hours);
		cal.add(Calendar.MINUTE, minutes);
		cal.add(Calendar.SECOND, seconds);
		expirationTime = cal.getTime();
	}

	
	public void ndlSlice(Resource sl, OntModel m) {
		// TODO Auto-generated method stub
		
	}
{
		
		try {
			// parse as request
			NdlRequestParser nrp = new NdlRequestParser(s, this);
			// something wrong with request model that is part of manifest
			// some interfaces belong only to nodes, and no connections
			// for now do less strict checking so we can get IP info
			// 07/2012/ib
			nrp.doLessStrictChecking();
			nrp.processRequest();
			nrp.freeModel();
			
			// parse as manifest
			requestPhase = false;
			NdlManifestParser nmp = new NdlManifestParser(s, this);
			nmp.processManifest();	
			nmp.freeModel();			
			Manifest.getInstance().setManifestTerm(creationTime, expirationTime);
			//NDLLIBManifestState.getInstance().launchResourceStateViewer(creationTime, expirationTime);
			
		} catch (Exception e) {
//			ExceptionDialog ed = new ExceptionDialog(NDLLIB.getInstance().getFrame(), "Exception");
//			ed.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//			ed.setException("Exception encountered while parsing manifest(m): ", e);
//			ed.setVisible(true);
			return false;
		} 
		return true;
	}

	// sometimes getLocalName is not good enough
	// so we strip off orca name space and call it a day
	private String getTrueName(Resource r) {
		if (r == null)
			return null;
		
		return StringUtils.removeStart(r.getURI(), NdlCommons.ORCA_NS);
	}
	
	private String getPrettyName(Resource r) {
		String rname = getTrueName(r);
		int ind = rname.indexOf('#');
		if (ind > 0) {
			rname = rname.substring(ind + 1);
		}
		return rname;
	}
	
	// get domain name from inter-domain resource name
	private String getInterDomainName(Resource r) {
		String trueName = getTrueName(r);
		
		if (r == null)
			return null;
		
		String[] split = trueName.split("#");
		if (split.length >= 2) {
			String rem = split[1];
			String[] split1 = rem.split("/");
			return split1[0];
		}	
		return null;
	}
	
	public void ndlLinkConnection(Resource l, OntModel m,
			List<Resource> interfaces, Resource parent) {
		//System.out.println("Found link connection " + l + " connecting " + interfaces);
		assert(l != null);
		
		// ignore request items
		if (requestPhase)
			return;
		
		NDLLIB.logger().debug("Link Connection: " + l);
		
		// find what nodes it connects (should be two)
		Iterator<Resource> it = interfaces.iterator(); 
		
		String label = NdlCommons.getResourceLabel(l);
		
		if (interfaces.size() == 2) {
			NDLLIB.logger().debug("  Adding p-to-p link");
			OrcaLink ol = Manifest.getInstance().getLinkCreator().create(getPrettyName(l), NdlCommons.getResourceBandwidth(l));
			ol.setLabel(label);

			// maybe point-to-point link
			// the ends
			Resource if1 = it.next(), if2 = it.next();
			
			if ((if1 != null) && (if2 != null)) {
				OrcaNode if1Node = interfaceToNode.get(getTrueName(if1));
				OrcaNode if2Node = interfaceToNode.get(getTrueName(if2));
				
				if ((if1Node != null) && if1Node.equals(if2Node)) {
					// degenerate case of a node on a shared vlan
					OrcaCrossconnect oc = new OrcaCrossconnect(getPrettyName(l));
					oc.setLabel(label);
					oc.setDomain(RequestSaver.reverseLookupDomain(NdlCommons.getDomain(l)));
					nodes.put(getTrueName(l), oc);
					// save one interface
					interfaceToNode.put(getTrueName(if1), oc);
					Manifest.getInstance().getGraph().addVertex(oc);
					return;
				}
				
				// get the bandwidth of crossconnects if possible
				long bw1 = 0, bw2 = 0;
				if (if1Node instanceof OrcaCrossconnect) {
					OrcaCrossconnect oc = (OrcaCrossconnect)if1Node;
					bw1 = oc.getBandwidth();
				} 
				if (if2Node instanceof OrcaCrossconnect) {
					OrcaCrossconnect oc = (OrcaCrossconnect)if2Node;
					bw2 = oc.getBandwidth();
				}
				ol.setBandwidth(bw1 > bw2 ? bw1 : bw2);
				
				// have to be there
				if ((if1Node != null) && (if2Node != null)) {
					NDLLIB.logger().debug("  Creating a link " + ol.getName() + " from " + if1Node + " to " + if2Node);
					Manifest.getInstance().getGraph().addEdge(ol, new Pair<OrcaNode>(if1Node, if2Node), 
							EdgeType.UNDIRECTED);
				}
			}
			// state
			ol.setState(NdlCommons.getResourceStateAsString(l));
			
			if (ol.getState() != null)
				ol.setIsResource();
			
			// reservation notice
			ol.setReservationNotice(NdlCommons.getResourceReservationNotice(l));
			links.put(getTrueName(l), ol);
		} else {			
			NDLLIB.logger().debug("  Adding multi-point crossconnect " + getTrueName(l) + " (has " + interfaces.size() + " interfaces)");
			// multi-point link
			// create a crossconnect then use interfaceToNode mapping to create links to it
			OrcaCrossconnect ml = new OrcaCrossconnect(getPrettyName(l));

			ml.setLabel(label);
			ml.setReservationNotice(NdlCommons.getResourceReservationNotice(l));
			ml.setState(NdlCommons.getResourceStateAsString(l));
			ml.setDomain(RequestSaver.reverseLookupDomain(NdlCommons.getDomain(l)));

			if (ml.getState() != null)
				ml.setIsResource();
			
			nodes.put(getTrueName(l), ml);
			
			// remember the interfaces
			while(it.hasNext()) {
				Resource intR = it.next();
				NDLLIB.logger().debug("  Remembering interface " + intR + " of " + ml);
				interfaceToNode.put(getTrueName(intR), ml);
			}
			
			// add crossconnect to the graph
			Manifest.getInstance().getGraph().addVertex(ml);
			
			// link to this later from interface information
			
			// link nodes (we've already seen them) to it
//			for(Resource intf: interfaces) {
//				if (interfaceToNode.get(getTrueName(intf)) != null) {
//					NDLLIB.logger().debug("  Creating a link " + lcount + " from " + ml + " to " + interfaceToNode.get(getTrueName(intf)));
//					OrcaLink ol = new OrcaLink("Link " + lcount++);
//					NDLLIBManifestState.getInstance().getGraph().addEdge(ol, new Pair<OrcaNode>(ml, interfaceToNode.get(getTrueName(intf))), EdgeType.UNDIRECTED);
//				}
//			}
		}
	}

	public void ndlManifest(Resource i, OntModel m) {
		// nothing to do in this case
		
		// ignore request items
		if (requestPhase)
			return;
		
		NDLLIB.logger().debug("Manifest: " + i);
	}

	public void ndlInterface(Resource intf, OntModel om, Resource conn,
			Resource node, String ip, String mask) {
		
		// ignore request items
		if (requestPhase)
			return;
		
		// System.out.println("Interface " + l + " has IP/netmask" + ip + "/" + mask);
		NDLLIB.logger().debug("Interface " + intf + " between " + node + " and " + conn + " has IP/netmask " + ip + "/" + mask);
		
		if (intf == null)
			return;
		OrcaNode on = null;
		OrcaLink ol = null;
		OrcaCrossconnect crs = null;
		if (node != null)
			on = nodes.get(getTrueName(node));
		
		if (conn != null) {
			ol = links.get(getTrueName(conn));
			if (ol == null) 
				// maybe it is a crossconnect and not a link connection
				crs = (OrcaCrossconnect)nodes.get(getTrueName(conn));
		}
		
		// extract the IP address from label, if it is not set on
		// the interface in the request (basically we favor manifest
		// setting over the request because in node groups that's the
		// correct one)
		String nmInt = null;
		if (ip == null) {
			String ifIpLabel = NdlCommons.getLabelID(intf);
			// x.y.z.w/24
			if (ifIpLabel != null) {
				String[] ipnm = ifIpLabel.split("/");
				if (ipnm.length == 2) {
					ip = ipnm[0];
					nmInt = ipnm[1];
				}
			}
		} else {
			if (mask != null)
				nmInt = "" + RequestSaver.netmaskStringToInt(mask);
		}
		
		if (on != null) {
			if (ol != null) {
				on.setIp(ol, ip, nmInt);
				on.setInterfaceName(ol, getTrueName(intf));
				on.setMac(ol, NdlCommons.getAddressMAC(intf));
			} else if (crs != null) {
				if (intf.toString().matches(node.toString() + "/IP/[0-9]+")) {
					// include only interfaces that have nodename/IP/<number> format - those
					// are generated by Yufeng. 

					// create link from node to crossconnect and assign IP if it doesn't exist
					NDLLIB.logger().debug("  Creating a link  from " + on + " to " + crs);
					ol = Manifest.getInstance().getLinkCreator().create("Unnamed");
					Manifest.getInstance().getGraph().addEdge(ol, new Pair<OrcaNode>(on, crs), 
							EdgeType.UNDIRECTED);
					on.setIp(ol, ip, nmInt);
					on.setMac(ol, NdlCommons.getAddressMAC(intf));
				}
			}
			else {
				// this could be a disconnected node group
				if (on instanceof OrcaComputeNode) {
					OrcaComputeNode ong = (OrcaComputeNode)on;
					ong.setInternalIp(ip, "" + RequestSaver.netmaskStringToInt(mask));
				}
			}
		}
	}

	public void ndlNetworkConnection(Resource l, OntModel om, long bandwidth,
			long latency, List<Resource> interfaces) {
		
		// ignore request items
		if (requestPhase)
			return;
		
		// nothing to do in this case
		NDLLIB.logger().debug("Network Connection: " + l);

	}

	public void ndlCrossConnect(Resource c, OntModel m, 
			long bw, String label, List<Resource> interfaces, Resource parent) {
		
		// ignore request items
		if (requestPhase)
			return;
		
		if (c == null)
			return;

		NDLLIB.logger().debug("CrossConnect: " + c);
		
		OrcaCrossconnect oc = new OrcaCrossconnect(getPrettyName(c));
		oc.setLabel(label);
		
		setCommonNodeProperties(oc, c);
		
		// later set bandwidth on adjacent links (crossconnects in NDL have
		// bandwidth but for users we'll show it on the links)
		oc.setBandwidth(bw);
		
		// process interfaces
		for (Iterator<Resource> it = interfaces.iterator(); it.hasNext();) {
			Resource intR = it.next();
			interfaceToNode.put(getTrueName(intR), oc);
		}
		
		nodes.put(getTrueName(c), oc);
		
		// add nodes to the graph
		Manifest.getInstance().getGraph().addVertex(oc);
	}
	
	public void ndlNode(Resource ce, OntModel om, Resource ceClass,
			List<Resource> interfaces) {
		
		// ignore request items
		if (requestPhase)
			return;
		
		if (ce == null)
			return;
		
		NDLLIB.logger().debug("Node: " + ce);
		
		OrcaNode newNode;
		
		if (NdlCommons.isStitchingNodeInManifest(ce)) {
			NDLLIB.logger().debug("  is a stitching port");
			OrcaStitchPort sp = new OrcaStitchPort(getPrettyName(ce));
			// get the interface (first)
			if (interfaces.size() == 1) {
				sp.setLabel(NdlCommons.getLayerLabelLiteral(interfaces.get(0)));
				if (NdlCommons.getLinkTo(interfaces.get(0)) != null)
					sp.setPort(NdlCommons.getLinkTo(interfaces.get(0)).toString());
			} 
			newNode = sp;
		} else if (NdlCommons.isNetworkStorage(ce)) {
			NDLLIB.logger().debug("  is a storage node");
			newNode = new OrcaStorageNode(getPrettyName(ce));
			newNode.setIsResource();
		} else if (NdlCommons.isMulticastDevice(ce)) {
			NDLLIB.logger().debug("  is a multicast root");
			newNode = new OrcaCrossconnect(getPrettyName(ce));
			newNode.setIsResource();
		} else {
			NDLLIB.logger().debug("  is a regular node");
			newNode = new OrcaNode(getPrettyName(ce));
		}
		
		for (Resource ii: interfaces)
			NDLLIB.logger().debug("  With interface " + ii);
		
		// set common properties
		setCommonNodeProperties(newNode, ce);
		
		// process interfaces
		for (Iterator<Resource> it = interfaces.iterator(); it.hasNext();) {
			Resource intR = it.next();
			interfaceToNode.put(getTrueName(intR), newNode);
		}
		
		// disk image
		Resource di = NdlCommons.getDiskImage(ce);
		if (di != null) {
			try {
				String imageURL = NdlCommons.getIndividualsImageURL(ce);
				String imageHash = NdlCommons.getIndividualsImageHash(ce);
				Request.getInstance().addImage(new OrcaImage(di.getLocalName(), 
						new URL(imageURL), imageHash), null);
				//newNode.setImage(di.getLocalName());
			} catch (Exception e) {
				// FIXME: ?
				;
			}
		}
		
		nodes.put(getTrueName(ce), newNode);
		
		// add nodes to the graph
		Manifest.getInstance().getGraph().addVertex(newNode);
		
		// are there nodes hanging off of it as elements? if so, link them in
		processDomainVmElements(ce, om, newNode);
	}

	// add collection elements
	private void processDomainVmElements(Resource vm, OntModel om, OrcaNode parent) {
		
		// HACK - if we added real interfaces to inner nodes, we don't need link to parent
		boolean innerNodeConnected = false;
		
		for (StmtIterator vmEl = vm.listProperties(NdlCommons.collectionElementProperty); vmEl.hasNext();) {
			Resource tmpR = vmEl.next().getResource();
			OrcaNode on = new OrcaNode(getTrueName(tmpR), parent);
			nodes.put(getTrueName(tmpR), on);
			Manifest.getInstance().getGraph().addVertex(on);
			OrcaLink ol = Manifest.getInstance().getLinkCreator().create("Unnamed");
			
			// link to parent (a visual HACK)
			links.put(ol.getName(), ol);
			Manifest.getInstance().getGraph().addEdge(ol, new Pair<OrcaNode>(parent, on), 
					EdgeType.UNDIRECTED);
			
			// add various properties
			setCommonNodeProperties(on, tmpR);
			
			// process interfaces. if there is an interface that leads to
			// a link, this is an intra-domain case, so we can delete the parent later
			for (Resource intR: NdlCommons.getResourceInterfaces(tmpR)) {
				interfaceToNode.put(getTrueName(intR), on);
				// HACK: for now check that this interface connects to something
				// and is not just hanging there with IP address
				List<Resource> hasI = NdlCommons.getWhoHasInterface(intR, om);
				if (hasI.size() > 1)
					innerNodeConnected = true;
			}
		}
		
		// Hack - remove parent if nodes are linked between themselves
		if (innerNodeConnected)
			Manifest.getInstance().getGraph().removeVertex(parent);
	}
	
	// set common node properties from NDL
	private void setCommonNodeProperties(OrcaNode on, Resource nr) {
		// post boot script
		on.setPostBootScript(NdlCommons.getPostBootScript(nr));
		
		// management IP/port access
		on.setManagementAccess(NdlCommons.getNodeServices(nr));
		
		// state
		on.setState(NdlCommons.getResourceStateAsString(nr));
		
		if (on.getState() != null) {
			on.setIsResource();
		}
		
		// reservation notice
		on.setReservationNotice(NdlCommons.getResourceReservationNotice(nr));
		
		// domain
		Resource domain = NdlCommons.getDomain(nr);
		if (domain != null)
			on.setDomain(RequestSaver.reverseLookupDomain(domain));
		
		// url
		on.setUrl(nr.getURI());
		
		// group (if any)
		String groupUrl = NdlCommons.getRequestGroupURLProperty(nr);
		// group URL same as my URL means I'm a single node
		if ((groupUrl != null) &&
				groupUrl.equals(on.getUrl()))
			groupUrl = null;
		on.setGroup(groupUrl);
		
		// specific ce type
		Resource ceType = NdlCommons.getSpecificCE(nr);
		if (ceType != null)
			on.setNodeType(RequestSaver.reverseNodeTypeLookup(ceType));
		
		// substrate info if present
		if (NdlCommons.getEC2WorkerNodeId(nr) != null)
			on.setSubstrateInfo("worker", NdlCommons.getEC2WorkerNodeId(nr));
		if (NdlCommons.getEC2InstanceId(nr) != null)
			on.setSubstrateInfo("instance", NdlCommons.getEC2InstanceId(nr));
		
	}
	
	public void ndlParseComplete() {
		// ignore request items
		if (requestPhase)
			return;
		
		// nothing to do in this case
		NDLLIB.logger().debug("Parse complete.");
	}

	public void ndlNetworkConnectionPath(Resource c, OntModel m,
			List<List<Resource>> path, List<Resource> roots) {

		// ignore request items
		if (requestPhase)
			return;

		// nothing to do in this case
		NDLLIB.logger().debug("Network Connection Path: " + c);
		if (path != null) {
			NDLLIB.logger().debug("Printing paths");
			StringBuilder sb =  new StringBuilder();
			for (List<Resource> p: path) {
				sb.append("   Path: ");
				for (Resource r: p) {
					sb.append(r + " ");
				}
				NDLLIB.logger().debug(sb.toString());
			}
		} else 
			NDLLIB.logger().debug("   None");
	} 

	*//**
	 * Request items - mostly ignored
	 * 
	 *//*
	
	
	public void ndlBroadcastConnection(Resource bl, OntModel om,
			long bandwidth, List<Resource> interfaces) {
		// TODO Auto-generated method stub
		
	}

	
	public void ndlNodeDependencies(Resource ni, OntModel m,
			Set<Resource> dependencies) {
		// TODO Auto-generated method stub
		
	}

	
	public void ndlReservation(Resource i, OntModel m) {
		// TODO Auto-generated method stub
		
	}

	
	public void ndlReservationEnd(Literal e, OntModel m, Date end) {
		expirationTime = end;
		
	}

	
	public void ndlReservationResources(List<Resource> r, OntModel m) {
		// TODO Auto-generated method stub
		
	}

	
	public void ndlReservationStart(Literal s, OntModel m, Date start) {
		creationTime = start;
		
	}

	
	public void ndlReservationTermDuration(Resource d, OntModel m, int years,
			int months, int days, int hours, int minutes, int seconds) {
		if (creationTime == null)
			return;
		if ((years == 0) && (months == 0) && (days == 0) && (hours == 0) && (minutes == 0) && (seconds == 0))
			return;
		Calendar cal = Calendar.getInstance();
		cal.setTime(creationTime);
		cal.add(Calendar.YEAR, years);
		cal.add(Calendar.MONTH, months);
		cal.add(Calendar.DAY_OF_YEAR, days);
		cal.add(Calendar.HOUR, hours);
		cal.add(Calendar.MINUTE, minutes);
		cal.add(Calendar.SECOND, seconds);
		expirationTime = cal.getTime();
	}

	
	public void ndlSlice(Resource sl, OntModel m) {
		// TODO Auto-generated method stub
		
	}
*/
}
