package orca.ahab.libndl.ndl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import orca.ahab.libndl.LIBNDL;
import orca.ahab.libndl.SliceGraph;
import orca.ahab.libndl.resources.common.ModelResource;
import orca.ahab.libndl.resources.request.BroadcastNetwork;
import orca.ahab.libndl.resources.request.ComputeNode;
import orca.ahab.libndl.resources.request.Interface;
import orca.ahab.libndl.resources.request.InterfaceNode2Net;
import orca.ahab.libndl.resources.request.Network;
import orca.ahab.libndl.resources.request.Node;
import orca.ahab.libndl.resources.request.RequestResource;
import orca.ahab.libndl.resources.request.StitchPort;
import orca.ahab.libndl.resources.request.StorageNode;
import orca.ndl.NdlCommons;
import orca.ndl.NdlException;
import orca.ndl.NdlGenerator;

public class NewSliceModel extends NDLModel {
	public NewSliceModel(){
		super();
		
	}
	
	
	public void init(SliceGraph sliceGraph){
		try{

			String nsGuid = UUID.randomUUID().toString();
			ngen = new NdlGenerator(nsGuid, LIBNDL.logger()); 
			reservation = ngen.declareReservation();
			
			
			Individual term = ngen.declareTerm();
			
			Date start = new Date();
			Individual tStart = ngen.declareTermBeginning(start);
			ngen.addBeginningToTerm(tStart, term);
		
			Individual duration = ngen.declareTermDuration(1,0,0);  // days, hours, mins
			
			ngen.addDurationToTerm(duration, term);
			ngen.addTermToReservation(term, reservation);
			
		} catch (NdlException e) {
			logger().error("NewSliceModel: " + e.getStackTrace());
		}
	}
	
	public void init(SliceGraph sliceGraph, String rdf) {
		logger().debug("NewSliceModel");
		RequestLoader loader = new RequestLoader(sliceGraph, this);
		loader.load(rdf);
		//NdlCommons request = loader.load(rdf);

		try {
			
			ngen = loader.getGenerator();
			reservation = ngen.declareReservation();
		} catch (NdlException e) {
			logger().error("NewSliceModel: " + e.getStackTrace());
		}
		
	}

	public boolean isNewSlice(){ return true; };
	
	@Override
	public void add(ComputeNode cn, String name) {
		logger().debug("NewSliceModel:add(ComputeNode)");
		try {
			Individual ni = null;
			
			//if (cn.getNodeCount() > 0){
			//	if (cn.getSplittable())
			//		ni = ngen.declareServerCloud(name, cn.getSplittable());
			//	else
			//		ni = ngen.declareServerCloud(name);
			//} else {sp.getName()
				ni = ngen.declareComputeElement(name);
				ngen.addGuid(ni, UUID.randomUUID().toString());
				ngen.addVMDomainProperty(ni);
			//}
			mapRequestResource2ModelResource(cn, ni);
			
			ngen.addResourceToReservation(reservation, ni);
		} catch (NdlException e) {
			logger().error("NewSliceModel:add(ComputeNode):" + e.getStackTrace());
		}	
		
	}

	@Override
	public void add(BroadcastNetwork bn, String name, long bandwidth) {
		logger().debug("NewSliceModel:add(BroadcastNetwork)" + name);
		try {
			Individual ci = ngen.declareBroadcastConnection(name);;
			ngen.addGuid(ci, UUID.randomUUID().toString());
			ngen.addLayerToConnection(ci, "ethernet", "EthernetNetworkElement");
			ngen.addBandwidthToConnection(ci, bandwidth);  //TODO: Should be constant default value
		
			mapRequestResource2ModelResource(bn, ci);
			ngen.addResourceToReservation(reservation, ci);
		} catch (NdlException e) {
			logger().error("NewSliceModel:add(ComputeNode):" + e.getStackTrace());
		}	
	}

	@Override 
	public void add(StitchPort sp, String name, String label, String port) {
		// TODO Auto-generated method stub
		logger().debug("add(StitchPort sp) sp: " + sp);
        Individual ni = null;
        try{
        		//Add the stitchport
                ni = ngen.declareStitchingNode(name);
                mapRequestResource2ModelResource(sp, ni);
                ngen.addResourceToReservation(reservation, ni);
                ngen.addGuid(ni, UUID.randomUUID().toString());
                
                
                //Add a link to the stitchport 
                Individual ei = ngen.declareNetworkConnection(name+"-net");
                ngen.addGuid(ei, UUID.randomUUID().toString());
        		if (reservation != null)
        			ngen.addResourceToReservation(reservation, ei);

        		if (sp.getBandwidth() > 0)
        			ngen.addBandwidthToConnection(ei, sp.getBandwidth());
        		
        		ngen.addLabelToIndividual(ei, label);
        		
                ngen.addLayerToConnection(ei, "ethernet", "EthernetNetworkElement");
                
                //processNodeAndLink(pn.getFirst(), e, ei);
                logger().debug("add(StitchPort sp) port: " + port);
                logger().debug("add(StitchPort sp) label: " + label);
                Individual spIface = ngen.declareStitchportInterface(port, label);
                
                ngen.addInterfaceToIndividual(spIface, ei);
                ngen.addInterfaceToIndividual(spIface, ni);
              
		} catch (NdlException e){
			logger().error("ERROR: NewSliceModel::add(StitchPort) " );
			e.printStackTrace();
		}
        
	}

	@Override
	public void add(InterfaceNode2Net i) {
		Resource r = this.getModelResource(i);
		Node node = i.getNode();
		Network net = i.getLink();
		
		try{
		
		Individual blI  = null; //ngen.getRequestIndividual(net.getName()+"-net"); //not sure this is right
		Individual nodeI = ngen.getRequestIndividual(node.getName());
		
		Individual intI;
		if (net instanceof StitchPort) {
			//StitchPort sp = (StitchPort)net;
			blI  = ngen.getRequestIndividual(net.getName()+"-net");
			//if ((sp.getLabel() == null) || (sp.getLabel().length() == 0))
			//	throw new NdlException("URL and label must be specified in StitchPort");
			//intI = ngen.declareStitchportInterface(sp.getPort(), sp.getLabel());
		} else {
			blI  = ngen.getRequestIndividual(net.getName());
		}
		intI = ngen.declareInterface(net.getName()+"-"+node.getName());
		
		ngen.addInterfaceToIndividual(intI, blI);
		
		if (nodeI == null)
			throw new NdlException("Unable to find or create individual for node " + node);
		
		if (intI == null)
			throw new NdlException("Unable to find or create individual for node " + node);
		
		ngen.addInterfaceToIndividual(intI, nodeI);

		// see if there is an IP address for this link on this node
//		if (node.getIp(link) != null) {
//			// create IP object, attach to interface
//			Individual ipInd = ngen.addUniqueIPToIndividual(n.getIp(l), oc.getName()+"-"+n.getName(), intI);
//			if (n.getNm(l) != null)
//				ngen.addNetmaskToIP(ipInd, netmaskIntToString(Integer.parseInt(n.getNm(l))));
//		}
		ngen.addResourceToReservation(reservation, nodeI);
		this.mapRequestResource2ModelResource(i, intI); 
		} catch (NdlException e){
			logger().error("ERROR: NewSliceModel::add(InterfaceNode2Net) " );
			e.printStackTrace();
		}
	}

	@Override
	public void add(StorageNode sn, String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(ComputeNode cn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(BroadcastNetwork bn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(StitchPort sp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(InterfaceNode2Net i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(StorageNode sn) {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public String getRequest() {
		RequestGenerator saver = new RequestGenerator(ngen);
		return saver.getRequest();
	}



	

}
