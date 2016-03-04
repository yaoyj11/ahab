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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import orca.ahab.libndl.LIBNDL;
import orca.ahab.libndl.SliceGraph;
import orca.ahab.libndl.resources.request.BroadcastNetwork;
import orca.ahab.libndl.resources.request.ComputeNode;
import orca.ahab.libndl.resources.request.Interface;
import orca.ahab.libndl.resources.request.InterfaceNode2Net;
import orca.ahab.libndl.resources.request.Network;
import orca.ahab.libndl.resources.request.Node;
import orca.ahab.libndl.resources.request.StitchPort;
import orca.ahab.libndl.resources.request.StorageNode;
import orca.ndl.NdlCommons;
import orca.ndl.NdlException;
import orca.ndl.NdlGenerator;

import org.apache.commons.lang.StringUtils;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Resource;
//import com.hyperrealm.kiwi.ui.dialog.ExceptionDialog;

import edu.uci.ics.jung.graph.util.Pair;

public class RequestGenerator extends NDLGenerator{
	private SliceGraph request;
	
	private static RequestGenerator instance;
	private NdlGenerator ngen = null;
	private Individual reservation = null;
	private String outputFormat = null;
	
	// converting to netmask
	private static final String[] netmaskConverter = {
		"128.0.0.0", "192.0.0.0", "224.0.0.0", "240.0.0.0", "248.0.0.0", "252.0.0.0", "254.0.0.0", "255.0.0.0",
		"255.128.0.0", "255.192.0.0", "255.224.0.0", "255.240.0.0", "255.248.0.0", "255.252.0.0", "255.254.0.0", "255.255.0.0",
		"255.255.128.0", "255.255.192.0", "255.255.224.0", "255.255.240.0", "255.255.248.0", "255.255.252.0", "255.255.254.0", "255.255.255.0",
		"255.255.255.128", "255.255.255.192", "255.255.255.224", "255.255.255.240", "255.255.255.248", "255.255.255.252", "255.255.255.254", "255.255.255.255"
	};
		

	private static final String EUCALYPTUS_NS = "eucalyptus";
	private static final String EXOGENI_NS = "exogeni";
	public static final String BAREMETAL = "ExoGENI Bare-metal";
	public static final String DOT_FORMAT = "DOT";
	public static final String N3_FORMAT = "N3";
	public static final String RDF_XML_FORMAT = "RDF-XML";
	public static final String defaultFormat = RDF_XML_FORMAT;
	
	// helper
	public static final Map<String, String> domainMap;
	static {
		Map<String, String> dm = new HashMap<String, String>();
		dm.put("RENCI (Chapel Hill, NC USA) XO Rack", "rcivmsite.rdf#rcivmsite");
		dm.put("BBN/GPO (Boston, MA USA) XO Rack", "bbnvmsite.rdf#bbnvmsite");
		dm.put("Duke CS (Durham, NC USA) XO Rack", "dukevmsite.rdf#dukevmsite");
		dm.put("UNC BEN (Chapel Hill, NC USA)", "uncvmsite.rdf#uncvmsite");
		dm.put("RENCI BEN (Chapel Hill, NC USA)", "rencivmsite.rdf#rencivmsite");
		dm.put("NICTA (Sydney, Australia) XO Rack", "nictavmsite.rdf#nictavmsite");
		dm.put("FIU (Miami, FL USA) XO Rack", "fiuvmsite.rdf#fiuvmsite");
		dm.put("UH (Houston, TX USA) XO Rack", "uhvmsite.rdf#uhvmsite");
		dm.put("UvA (Amsterdam, The Netherlands) XO Rack", "uvanlvmsite.rdf#uvanlvmsite");
		dm.put("UFL (Gainesville, FL USA) XO Rack", "uflvmsite.rdf#uflvmsite");
		dm.put("UCD (Davis, CA USA) XO Rack", "ucdvmsite.rdf#ucdvmsite");
		dm.put("OSF (Oakland, CA USA) XO Rack", "osfvmsite.rdf#osfvmsite");
		dm.put("SL (Chicago, IL USA) XO Rack", "slvmsite.rdf#slvmsite");
		dm.put("WVN (UCS-B series rack in Morgantown, WV, USA)", "wvnvmsite.rdf#wvnvmsite");
		dm.put("NCSU (UCS-B series rack at NCSU)", "ncsuvmsite.rdf#ncsuvmsite");
		dm.put("NCSU2 (UCS-C series rack at NCSU)", "ncsu2vmsite.rdf#ncsu2vmsite");
		dm.put("TAMU (College Station, TX, USA) XO Rack", "tamuvmsite.rdf#tamuvmsite");
		dm.put("UMass (UMass Amherst, MA, USA) XO Rack", "umassvmsite.rdf#umassvmsite");
		dm.put("WSU (Detroit, MI, USA) XO Rack", "wsuvmsite.rdf#wsuvmsite");
		dm.put("UAF (Fairbanks, AK, USA) XO Rack", "uafvmsite.rdf#uafvmsite");
		dm.put("PSC (Pittsburgh, TX, USA) XO Rack", "pscvmsite.rdf#pscvmsite");
		dm.put(StitchPort.STITCHING_DOMAIN_SHORT_NAME, "orca.rdf#Stitching");

		domainMap = Collections.unmodifiableMap(dm);
	}
	
	public static final Map<String, String> netDomainMap;
	static {
		Map<String, String> ndm = new HashMap<String, String>();

		ndm.put("RENCI XO Rack Net", "rciNet.rdf#rciNet");
		ndm.put("BBN/GPO XO Rack Net", "bbnNet.rdf#bbnNet");
		ndm.put("Duke CS Rack Net", "dukeNet.rdf#dukeNet");
		ndm.put("UNC BEN XO Rack Net", "uncNet.rdf#UNCNet");
		ndm.put("NICTA XO Rack Net", "nictaNet.rdf#nictaNet");
		ndm.put("FIU XO Rack Net", "fiuNet.rdf#fiuNet");
		ndm.put("UH XO Rack Net", "uhNet.rdf#UHNet");
		ndm.put("NCSU XO Rack Net", "ncsuNet.rdf#ncsuNet");
		ndm.put("UvA XO Rack Net", "uvanlNet.rdf#uvanlNet");
		ndm.put("UFL XO Rack Net", "uflNet.rdf#uflNet");
		ndm.put("UCD XO Rack Net", "ucdNet.rdf#ucdNet");
		ndm.put("OSF XO Rack Net", "osfNet.rdf#osfNet");
		ndm.put("SL XO Rack Net", "slNet.rdf#slNet");
		ndm.put("WVN XO Rack Net", "wvnNet.rdf#wvnNet");
		ndm.put("NCSU XO Rack Net", "ncsuNet.rdf#ncsuNet");
		ndm.put("NCSU2 XO Rack Net", "ncs2Net.rdf#ncsuNet");
		
		ndm.put("I2 ION/AL2S", "ion.rdf#ion");
		ndm.put("NLR Net", "nlr.rdf#nlr");
		ndm.put("BEN Net", "ben.rdf#ben");
	
		netDomainMap = Collections.unmodifiableMap(ndm);
	}
	
	// various node types
	public static final Map<String, Pair<String>> nodeTypes;
	static {
		Map<String, Pair<String>> nt = new HashMap<String, Pair<String>>();
		nt.put(BAREMETAL, new Pair<String>(EXOGENI_NS, "ExoGENI-M4"));
		nt.put("Euca m1.small", new Pair<String>(EUCALYPTUS_NS, "EucaM1Small"));
		nt.put("Euca c1.medium", new Pair<String>(EUCALYPTUS_NS, "EucaC1Medium"));
		nt.put("Euca m1.large", new Pair<String>(EUCALYPTUS_NS, "EucaM1Large"));
		nt.put("Euca m1.xlarge", new Pair<String>(EUCALYPTUS_NS, "EucaM1XLarge"));
		nt.put("Euca c1.xlarge", new Pair<String>(EUCALYPTUS_NS, "EucaC1XLarge"));
		nt.put("XO Small", new Pair<String>(EXOGENI_NS, "XOSmall"));
		nt.put("XO Medium", new Pair<String>(EXOGENI_NS, "XOMedium"));
		nt.put("XO Large", new Pair<String>(EXOGENI_NS, "XOLarge"));
		nt.put("XO Extra large", new Pair<String>(EXOGENI_NS, "XOXlarge"));
		//nodeTypes = Collections.unmodifiableMap(nt);
		nodeTypes = nt;
	}

	
	public RequestGenerator(SliceGraph r) {
		request = r;
	}
	
	
	public boolean saveRequest(File f) {
		return saveNewRequest(f);
	}
	
	public boolean saveModifyRequest(File f) {
		//
		return true;
	}
	
	
	/**
	 * Save to file
	 * @param f
	 * @param g
	 * @param nsGuid
	 * @return
	 */
	public boolean saveNewRequest(File f) {
		assert(f != null);

		String ndl = convertGraphToNdl();
		if (ndl == null){
			return false;
		}
		
		try{
			FileOutputStream fsw = new FileOutputStream(f);
			OutputStreamWriter out = new OutputStreamWriter(fsw, "UTF-8");
			out.write(ndl);
			out.close();
			return true;
		} catch(FileNotFoundException e) {
			LIBNDL.logger().debug("saveGraph: FileNotFoundException");
			;
		} catch(UnsupportedEncodingException ex) {
			LIBNDL.logger().debug("saveGraph: UnsupportedEncodingException");
			;
		} catch(IOException ey) {
			LIBNDL.logger().debug("saveGraph: IOException");
			;
		} 
		return false;
	}

	
	
	/**
	 * Save to string
	 * @param f
	 * @param g
	 * @param nsGuid
	 * @return
	 */  
	public boolean saveRequest(String f) {
		return this.saveRequest(new File(f));
	}

	public boolean saveModifyRequest(String f){
		return this.saveModifyRequest(new File(f));
	}
	
	public String getRequest(){
		return convertGraphToNdl();
	}
	
	/**
	 * Save graph using NDL
	 * @param f
	 * @param requestGraph
	 */
	//public String convertGraphToNdl(SparseMultigraph<OrcaResource, OrcaStitch> g, String nsGuid) {
	public String convertGraphToNdl() {
		String nsGuid = null;  //TODO: if set to null ndlcommons will pick one for me
		LIBNDL.logger().debug("convertGraphToNdl");
		String res = null;


		try {
			ngen = new NdlGenerator(nsGuid, LIBNDL.logger());

			reservation = ngen.declareReservation();
			Individual term = ngen.declareTerm();

			// not an immediate reservation? declare term beginning
			if (this.request.getTerm().getStart() != null) {
				Individual tStart = ngen.declareTermBeginning(this.request.getTerm().getStart());
				ngen.addBeginningToTerm(tStart, term);
			}

			// now duration
			this.request.getTerm().normalizeDuration();
			Individual duration = ngen.declareTermDuration(this.request.getTerm().getDurationDays(), 
					this.request.getTerm().getDurationHours(), this.request.getTerm().getDurationMins());
			ngen.addDurationToTerm(duration, term);
			ngen.addTermToReservation(term, reservation);
			/*				
				// openflow
				ngen.addOpenFlowCapable(reservation, r.getOfNeededVersion());

				// add openflow details
				if (r.getOfNeededVersion() != null) {
					Individual ofSliceI = ngen.declareOfSlice("of-slice");
					ngen.addSliceToReservation(reservation, ofSliceI);
					ngen.addOfPropertiesToSlice(r.getOfUserEmail(), 
							r.getOfSlicePass(), 
							r.getOfCtrlUrl(), 
							ofSliceI);
				}
			 */

			


			Individual ni;
			//Handle stitchports
			for (StitchPort sp: request.getStitchPorts()){
				ni = ngen.declareStitchingNode(sp.getName());
				ngen.addResourceToReservation(reservation, ni);
			}

			//Handle storage nodes
			for (StorageNode snode: request.getStorageNodes()){
				ni = ngen.declareISCSIStorageNode(snode.getName(), 
						snode.getCapacity(),
						snode.getFSType(), snode.getFSParam(), snode.getMntPoint(), 
						snode.getDoFormat());
				if (snode.getDomain() != null) {
					Individual domI = ngen.declareDomain(domainMap.get(snode.getDomain()));
					ngen.addNodeToDomain(domI, ni);
				}
				ngen.addResourceToReservation(reservation, ni);
			} 

			/* Create compute elements in the request.  
			* 
			* If the compute node is only of size 1 (i.e if there is only one compute node)
			* then we could create a node instead of a group.  There is no reason to do this
			* b/c a group of one is the same as an individual node with the exception that 
			* an individual node cannot be modified to add replicas of itself.   In addition, 
			* the current controller will change a group of one to a node even if we do not 
			* want it to. 
			*  
			*/ 
			for (ComputeNode cn: request.getComputeNodes()){
				// nodes and nodegroups
				if (cn.getNodeCount() > 0){
					if (cn.getSplittable())
						ni = ngen.declareServerCloud(cn.getName(), cn.getSplittable());
					else
						ni = ngen.declareServerCloud(cn.getName());
				} else {
					ni = ngen.declareComputeElement(cn.getName());
					ngen.addVMDomainProperty(ni);
				}

				ngen.addResourceToReservation(reservation, ni);

				// for clusters, add number of nodes, declare as cluster (VM domain)
				if (cn.getNodeCount() > 0){
					ngen.addNumCEsToCluster(cn.getNodeCount(), ni);
					ngen.addVMDomainProperty(ni);
				}

				// node type 
				setNodeTypeOnInstance(cn.getNodeType(), ni);

				// check if image is set in this node
				if (cn.getImageUrl() != null) {
					Individual imI = ngen.declareDiskImage(cn.getImageUrl().toString(), cn.getImageHash(), cn.getImageShortName());
					ngen.addDiskImageToIndividual(imI, ni);
				} else {
					// only bare-metal can specify no image
					if (!NdlCommons.isBareMetal(ni))
						throw new NdlException("Node " + cn.getName() + " is not bare-metal and does not specify an image");

				}

				LIBNDL.logger().debug("About to add domain " + cn.getDomain());
				// if no global domain domain is set, declare a domain and add inDomain property
				//if (!globalDomain && (cn.getDomain() != null)) {
				if (cn.getDomain() != null) {
					LIBNDL.logger().debug("adding domain " + cn.getDomain());
					Individual domI = ngen.declareDomain(domainMap.get(cn.getDomain()));
					ngen.addNodeToDomain(domI, ni);
				}

				// post boot script
				if ((cn.getPostBootScript() != null) && (cn.getPostBootScript().length() > 0)) {
					ngen.addPostBootScriptToCE(cn.getPostBootScript(), ni);
				}
			}

			/*
				// node dependencies (done afterwards to be sure all nodes are declared)
				for (OrcaResource resource: request.getResources()) {
					Individual ni = ngen.getRequestIndividual(resource.getName());
					for(OrcaResource dep: resource.getDependencies()) {
						Individual depI = ngen.getRequestIndividual(dep.getName());
		btaessawy@gmail.com				if (depI != null) {
							ngen.addDependOnToIndividual(depI, ni);
						}
					}
				}
			 */

			/* Create networks in the request.  
			* 
			* If the network has only two compute elements then we could make it a point2point link.
			* I'm not sure why we would want this b/c a broadcast link with two end points will manifest 
			* in the same way but is flexible in that a modify could add an additional compute element 
			* to the network  
			*  
			*/ 
			for (BroadcastNetwork e: request.getBroadcastLinks()) {
				LIBNDL.logger().debug("saving OrcaBroadcastLink");
				//checkLinkSanity(e);
				boolean hasStorage = false;
				for(Interface i : e.getInterfaces()){
					if (i instanceof InterfaceNode2Net){
						if(((InterfaceNode2Net)i).getNode() instanceof StorageNode){
							hasStorage = true;
							break;
						}
						
					}
				}
				
				if(!hasStorage){
					Individual ei = ngen.declareBroadcastConnection(e.getName());
					ngen.addResourceToReservation(reservation, ei);

					if (e.getBandwidth() > 0)
						ngen.addBandwidthToConnection(ei, e.getBandwidth());

					if (e.getLabel() != null) 
						ngen.addLabelToIndividual(ei, e.getLabel());

					// TODO: deal with layers later
					ngen.addLayerToConnection(ei, "ethernet", "EthernetNetworkElement");

					// TODO: latency
				} else {			
					//networks to storage must be point 2 point
					Individual ei = ngen.declareNetworkConnection(e.getName());
					ngen.addResourceToReservation(reservation, ei);

					if (e.getBandwidth() > 0)
						ngen.addBandwidthToConnection(ei, e.getBandwidth());
				
					if (e.getLabel() != null) 
						ngen.addLabelToIndividual(ei, e.getLabel());
				
					// TODO: deal with layers later
					ngen.addLayerToConnection(ei, "ethernet", "EthernetNetworkElement");

					// TODO: latency
				}
			}

			//Process stitches
			for (Interface stitch: request.getInterfaces()){
				if (stitch instanceof InterfaceNode2Net){
					InterfaceNode2Net stitch_n2l = (InterfaceNode2Net)stitch;
					Individual ei = ngen.getRequestIndividual(stitch_n2l.getLink().getName());
					processNodeAndLink(stitch_n2l, ei);
				} else {
					LIBNDL.logger().error("Error: unkown stitch type, skipping: " + stitch);
				}
			}
			res = getFormattedOutput(ngen, outputFormat);

		} catch (Exception e) {

			e.printStackTrace();
			return null;
		} finally {
			if (ngen != null)
				ngen.done();
		}

		return res;
	}

	private String getFormattedOutput(NdlGenerator ng, String oFormat) {
		if (oFormat == null)
			return getFormattedOutput(ng, defaultFormat);
		if (oFormat.equals(RDF_XML_FORMAT)) 
			return ng.toXMLString();
		else if (oFormat.equals(N3_FORMAT))
			return ng.toN3String();
		else if (oFormat.equals(DOT_FORMAT)) {
			return ng.getGVOutput();
		}
		else
			return getFormattedOutput(ng, defaultFormat);
	}

/*	
	private void addLinkStorageDependency(StorageNode n, BroadcastNetwork e) throws NdlException {
		
		// if the other end is storage, need to add dependency
		if (e.linkToSharedStorage() && !(n instanceof StorageNode)) {
			Pair<Node> pn = r.getGraph().getEndpoints(e);
			StorageNode osn = null;
			try {
				if (pn.getFirst() instanceof StorageNode)
					osn = (StorageNode) pn.getFirst();
				else
					osn = (StorageNode) pn.getSecond();
			} catch (Exception ce) {
				;
			}
			if (osn == null)
				throw new NdlException("Link " + e.getName() + " marked as storage, but neither endpoint is storage");
			Individual storInd = ngen.getRequestIndividual(osn.getName());
			Individual nodeInd = ngen.getRequestIndividual(n.getName());
			if ((storInd == null) || (nodeInd == null))
				throw new NdlException("Unable to find individual for node " + osn + " or " + n);
			ngen.addDependOnToIndividual(storInd, nodeInd);
		}
		
	}
	*/
	/**
	 * Link node to edge, create interface and process IP address 
	 * @param n
	 * @param e
	 * @param edgeI
	 * @throws NdlException
	 */
	private void processNodeAndLink(InterfaceNode2Net s, Individual edgeI) throws NdlException {
		Node n = s.getNode();  
		Network e = s.getLink();
 		
		Individual intI;
		
		//Add storage dependency:  shouldn't the controller do this???
		if (s.getNode() instanceof StorageNode){
			Individual storInd = ngen.getRequestIndividual(s.getNode().getName());
			InterfaceNode2Net iface = null;
			for (Interface i : s.getLink().getInterfaces()){
				if(i != s && i instanceof InterfaceNode2Net){
					iface = (InterfaceNode2Net)i;
					break;
				}
			}
			Individual nodeInd = ngen.getRequestIndividual(iface.getNode().getName());
			if ((storInd == null) || (nodeInd == null))
				throw new NdlException("Unable to find individual for node " + s.getNode() + " or " + n);
			ngen.addDependOnToIndividual(storInd, nodeInd);
		}
		
		if (n instanceof StitchPort) {
			StitchPort sp = (StitchPort)n;
			if ((sp.getPort() == null) || (sp.getPort().length() == 0) || 
					(sp.getLabel() == null) || (sp.getLabel().length() == 0))
				throw new NdlException("URL and label must be specified in StitchPort");
			intI = ngen.declareStitchportInterface(sp.getPort(), sp.getLabel());
		} else {
			intI = ngen.declareInterface(e.getName()+"-"+n.getName());
		}
		// add to link
		ngen.addInterfaceToIndividual(intI, edgeI);

		//add to node
		Individual nodeI = ngen.getRequestIndividual(n.getName());
		ngen.addInterfaceToIndividual(intI, nodeI);
		
		// see if there is an IP address for this link on this node
		if (s.getIpAddress() != null) {
			// create IP object, attach to interface
			Individual ipInd = ngen.addUniqueIPToIndividual(s.getIpAddress(), e.getName()+"-"+n.getName(), intI);
			if (s.getNetmask() != null){
				ngen.addNetmaskToIP(ipInd, s.getNetmask());
			}
		}
	}
	

	
	
	/*
	private void checkLinkSanity(OrcaLink l) throws NdlException {
		// sanity checks
		// 1) if label is specified, nodes cannot be in different domains

		Pair<OrcaNode> pn = r.getGraph().getEndpoints(l);
		
		if ((l.getLabel() != null) && 
				(((pn.getFirst().getDomain() != null) && 
				(!pn.getFirst().getDomain().equals(pn.getSecond().getDomain()))) ||
				(pn.getSecond().getDomain() != null) && 
				(!pn.getSecond().getDomain().equals(pn.getFirst().getDomain()))))
			throw new NdlException("Link " + l.getName() + " is invalid: it specifies a desired VLAN tag, but the nodes are bound to different domains");
		
		if ((pn.getFirst() instanceof OrcaStorageNode) &&
				(pn.getSecond() instanceof OrcaStorageNode)) 
			throw new NdlException("Link " + l.getName() + " in invalid: it connects two storage nodes together");
	}
	
	*/
	
	
	/**
	 * Check the sanity of a crossconnect
	 * @param n
	 * @throws NdlException
	 *//*
	private void checkCrossconnectSanity(OrcaCrossconnect n) throws NdlException {
		// sanity checks
		// 1) nodes can't be from different domains (obsoleted 08/28/13 /ib)
	}
	
	private void addCrossConnectStorageDependency(OrcaCrossconnect oc) throws NdlException {
		Collection<OrcaLink> iLinks = r.getGraph().getIncidentEdges(oc);
		boolean sharedStorage = oc.linkToSharedStorage();
		
		if (!sharedStorage)
			return;
		
		List<OrcaStorageNode> snodes = new ArrayList<OrcaStorageNode>();
		List<OrcaNode> otherNodes = new ArrayList<OrcaNode>();
		
		for(OrcaLink l: iLinks) {
			Pair<OrcaNode> pn = r.getGraph().getEndpoints(l);
			OrcaNode n = null;
			// find the non-crossconnect side
			if (!(pn.getFirst() instanceof OrcaCrossconnect))
				n = pn.getFirst();
			else if (!(pn.getSecond() instanceof OrcaCrossconnect))
				n = pn.getSecond();
			
			if (n instanceof OrcaStorageNode) 
				snodes.add((OrcaStorageNode)n);
			else
				otherNodes.add(n);
		}
		
		for(OrcaNode n: otherNodes) {
			for (OrcaStorageNode s: snodes) {
				Individual storInd = ngen.getRequestIndividual(s.getName());
				Individual nodeInd = ngen.getRequestIndividual(n.getName());
				if ((storInd == null) || (nodeInd == null))
					throw new NdlException("Unable to find individual for node " + s + " or " + n);
				ngen.addDependOnToIndividual(storInd, nodeInd);
			}
		}
	}
	*/
	
	
	private void setNodeTypeOnInstance(String type, Individual ni) throws NdlException {
		if (BAREMETAL.equals(type))
			ngen.addBareMetalDomainProperty(ni);
		else
			ngen.addVMDomainProperty(ni);
		if (nodeTypes.get(type) != null) {
			Pair<String> nt = nodeTypes.get(type);
			ngen.addNodeTypeToCE(nt.getFirst(), nt.getSecond(), ni);
		}
	}
	
	/**************************************  Helper Function ***************************************/	
	
	// use different maps to try to do a reverse lookup
	private static String reverseLookupDomain_(Resource domain, Map<String, String> m, String suffix) {
		String domainName = StringUtils.removeStart(domain.getURI(), NdlCommons.ORCA_NS);
		if (domainName == null)
			return null;
		
		// remove one or the other
		domainName = StringUtils.removeEnd(domainName, suffix);
		for (Iterator<Map.Entry<String, String>> domName = m.entrySet().iterator(); domName.hasNext();) {
			Map.Entry<String, String> e = domName.next();
			if (domainName.equals(e.getValue()))
				return e.getKey();
		}
		return null;
	}
	
	// use different maps to try to do a reverse lookup
	private static String reverseLookupDomain_(String dom, Map<String, String> m, String suffix) {
		String domainName = StringUtils.removeStart(dom, NdlCommons.ORCA_NS);
		if (domainName == null)
			return null;
		
		// remove one or the other
		domainName = StringUtils.removeEnd(domainName, suffix);
		for (Iterator<Map.Entry<String, String>> domName = m.entrySet().iterator(); domName.hasNext();) {
			Map.Entry<String, String> e = domName.next();
			if (domainName.equals(e.getValue()))
				return e.getKey();
		}
		return null;
	}
	
	/**
	 * Do a reverse lookup on domain (NDL -> short name)
	 * @param domain
	 * @return
	 */
	public static String reverseLookupDomain(Resource domain) {
		if (domain == null)
			return null;
		// strip off name space and "/Domain"
		String domainName = StringUtils.removeStart(domain.getURI(), NdlCommons.ORCA_NS);
		if (domainName == null)
			return null;
		
		// try vm domain, then net domain
		String mapping = reverseLookupDomain_(domain, domainMap, "/Domain");
		if (mapping == null)
			mapping = reverseLookupDomain_(domain, domainMap, "/Domain/vm");
		if (mapping == null) 
			mapping = reverseLookupDomain_(domain, netDomainMap, "/Domain/vlan");
		if (mapping == null)
			mapping = reverseLookupDomain_(domain, domainMap, "/Domain/lun");
		
		return mapping;
		//return null;
	}
	
	public static String reverseLookupDomain(String dom) {
		if (dom == null)
			return null;
		// strip off name space and "/Domain"
		String domainName = StringUtils.removeStart(dom, NdlCommons.ORCA_NS);
		if (domainName == null)
			return null;
		
		// try vm domain, then net domain
		String mapping = reverseLookupDomain_(dom, domainMap, "/Domain");
		if (mapping == null)
			mapping = reverseLookupDomain_(dom, domainMap, "/Domain/vm");
		if (mapping == null) 
			mapping = reverseLookupDomain_(dom, netDomainMap, "/Domain/vlan");
		
		return mapping;
		//return null;
	}
	
	
	/**
	 * Do a reverse lookup on node type (NDL -> shortname )
	 */
	public static String reverseNodeTypeLookup(Resource nt) {
		if (nt == null)
			return null;
		for (Iterator<Map.Entry<String, Pair<String>>> it = nodeTypes.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Pair<String>> e = it.next();
			// convert to namespace and type in a pair
			// WARNING: this checks only the type, not the namespace.
			if (nt.getLocalName().equals(e.getValue().getSecond()))
				return e.getKey();
		}
		return null;
	}
	
	/**
	 * Post boot scripts need to be sanitized (deprecated)
	 * @param s
	 * @return
	 */
	public static String sanitizePostBootScript(String s) {
		// no longer needed
		return s;
	}

	
	/**
	 * Convert netmask string to an integer (24-bit returned if no match)
	 * @param nm
	 * @return
	 */
	public static int netmaskStringToInt(String nm) {
		int i = 1;
		for(String s: netmaskConverter) {
			if (s.equals(nm))
				return i;
			i++;
		}
		return 24;
	}
	
	/**
	 * Convert netmask int to string (255.255.255.0 returned if nm > 32 or nm < 1)
	 * @param nm
	 * @return
	 */
	public static String netmaskIntToString(int nm) {
		if ((nm > 32) || (nm < 1)) 
			return "255.255.255.0";
		else
			return netmaskConverter[nm - 1];
	}


	@Override
	void generate(String rdf) {
		// TODO Auto-generated method stub
		
	}
	
}
