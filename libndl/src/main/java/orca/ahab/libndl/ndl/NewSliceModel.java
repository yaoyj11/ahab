package orca.ahab.libndl.ndl;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.uci.ics.jung.graph.SparseMultigraph;
import orca.ahab.libndl.LIBNDL;
import orca.ahab.libndl.SliceGraph;
import orca.ahab.libndl.resources.request.BroadcastNetwork;
import orca.ahab.libndl.resources.request.ComputeNode;
import orca.ahab.libndl.resources.request.Interface;
import orca.ahab.libndl.resources.request.InterfaceNode2Net;
import orca.ahab.libndl.resources.request.RequestResource;
import orca.ahab.libndl.resources.request.StitchPort;
import orca.ahab.libndl.resources.request.StorageNode;
import orca.ndl.NdlCommons;
import orca.ndl.NdlException;
import orca.ndl.NdlGenerator;

public class NewSliceModel extends NDLModel {
	
	
	public NewSliceModel(SliceGraph sliceGraph, String rdf) {
		super();
		
		logger().debug("NewSliceModel");
		RequestLoader loader = new RequestLoader(sliceGraph, this);
		loader.load(rdf);
		//NdlCommons request = loader.load(rdf);
	
		try {
			String nsGuid = "1111111111"; //hack for now
			ngen = new NdlGenerator(nsGuid, LIBNDL.logger());
			reservation = ngen.declareReservation();
		} catch (NdlException e) {
			
			logger().error("NewSliceModel: " + e.getStackTrace());
		}
		
	}

	@Override
	public void add(ComputeNode cn, String name) {
		logger().debug("NewSliceModel:add(ComputeNode)");
		try {
			Individual ni;
			if (cn.getNodeCount() > 0){
				if (cn.getSplittable())
					ni = ngen.declareServerCloud(name, cn.getSplittable());
				else
					ni = ngen.declareServerCloud(name);
			} else {
				ni = ngen.declareComputeElement(name);
				ngen.addVMDomainProperty(ni);
			}

			ngen.addResourceToReservation(reservation, ni);
		} catch (NdlException e) {
			logger().error("NewSliceModel:add(ComputeNode):" + e.getStackTrace());
		}	
		
	}

	@Override
	public void add(BroadcastNetwork bn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add(StitchPort sp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add(InterfaceNode2Net i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add(StorageNode sn) {
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
	public String getName(RequestResource cn) {
		return this.getModelResource(cn).getLocalName();
	}

	@Override
	public void setName(RequestResource cn) {
		logger().info("NDLModel:setName not impelemented");		
	}

	@Override
	public String getRequest() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
