package orca.ahab.libndl.ndl;

import java.util.UUID;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.uci.ics.jung.graph.SparseMultigraph;
import orca.ahab.libndl.LIBNDL;
import orca.ahab.libndl.SliceGraph;
import orca.ahab.libndl.resources.common.ModelResource;
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
	public NewSliceModel(){
		super();
		
	}
	
	
	public void init(SliceGraph sliceGraph){
		try{
			String nsGuid = UUID.randomUUID().toString();
			ngen = new NdlGenerator(nsGuid, LIBNDL.logger()); 
			reservation = ngen.declareReservation();
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
			
			ni = ngen.declareComputeElement(name);
			ngen.addVMDomainProperty(ni);
			mapRequestResource2ModelResource(cn, ni);
			
			ngen.addResourceToReservation(reservation, ni);
		} catch (NdlException e) {
			logger().error("NewSliceModel:add(ComputeNode):" + e.getStackTrace());
		}	
		
	}

	@Override
	public void add(BroadcastNetwork bn, String name) {
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
	public String getName(ModelResource cn) {
		return this.getModelResource(cn).getLocalName();
	}

	@Override
	public void setName(ModelResource cn) {
		logger().info("NDLModel:setName not impelemented");		
	}

	@Override
	public String getRequest() {
		RequestGenerator saver = new RequestGenerator(ngen);
		return saver.getRequest();
	}

	//setImage is the same for new and existing models
	@Override
	public void setImage(ComputeNode cn, String imageURL, String imageHash, String shortName) {
		try{
			Individual imageIndividual = ngen.declareDiskImage(imageURL, imageHash, shortName);	
			ngen.addDiskImageToIndividual(imageIndividual, (Individual)this.getModelResource(cn));
		}catch (ClassCastException e){
			LIBNDL.logger().error("Cannot cast ComputeNode resource to individual. " + cn.getName());
		}catch (NdlException e){
			LIBNDL.logger().error("NdlException setting image for " + cn.getName());
		}
		
	}

	@Override
	public String getImageURL(ComputeNode cn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getImageHash(ComputeNode cn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getImageShortName(ComputeNode cn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNodeType(ComputeNode computeNode, String nodeType) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getNodeType(ComputeNode computeNode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPostBootScript(ComputeNode computeNode, String postBootScript) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getPostBootScript(ComputeNode computeNode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDomain(RequestResource requestResource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDomain(RequestResource requestResource, String d) {
		// TODO Auto-generated method stub
		
	}

	

}
