package orca.ahab.libndl.ndl;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import orca.ahab.libndl.LIBNDL;
import orca.ahab.libndl.SliceGraph;
import orca.ahab.libndl.resources.common.ModelResource;
import orca.ahab.libndl.resources.manifest.ManifestResource;
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
import orca.ndl.NdlManifestParser;
import orca.ndl.NdlRequestParser;

public class ExistingSliceModel extends NDLModel{
	//protected NdlManifestParser sliceModel;
	protected NdlRequestParser sliceModel;
	
	/* map of RequestResource in original slice or request to ndl Resource */
	protected Map<RequestResource,Resource> slice2NDLMap;
	
	
	public ExistingSliceModel() {
		super();
		
	}

	public void init(SliceGraph sliceGraph, String rdf){
		
		try{
			RequestLoader rloader = new RequestLoader(sliceGraph,this);
			sliceModel = rloader.load(rdf);

			String nsGuid = "1111111111"; //hack for now
		
			ngen = new NdlGenerator(nsGuid, LIBNDL.logger(), true);
			String nm = (nsGuid == null ? "my-modify" : nsGuid + "/my-modify");
			reservation = ngen.declareModifyReservation(nm);
		} catch (Exception e){
			LIBNDL.logger().debug("Fail: ExistingSliceModel::init");
		}
					
	}

	protected void mapSliceResource2ModelResource(RequestResource r, Resource i){
		slice2NDLMap.put(r,i);
	}

	
	protected Resource getModelResource(RequestResource r){
		return request2NDLMap.get(r);
	}
	
	
	@Override
	public String getRequest() {
		ModifyGenerator saver = new ModifyGenerator(ngen);
		return saver.getModifyRequest();
	}

	
	
	@Override
	public void add(ComputeNode cn, String name) {
		logger().debug("ExistingSliceModel:add(ComputeNode)");
		try {
			Individual ni = null;
			
			if (cn.getNodeCount() > 0){
				if (cn.getSplittable())
					ni = ngen.declareServerCloud(name, cn.getSplittable());
				else
					ni = ngen.declareServerCloud(name);
			} else {
				ni = ngen.declareComputeElement(name);
				ngen.addVMDomainProperty(ni);
			}
			
			mapRequestResource2ModelResource(cn, ni);
			ngen.declareModifyElementAddElement(reservation, ni);
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
		// TODO Auto-generated method stu(b
		
	}

	@Override
	public String getName(ModelResource cn) {
		return this.getModelResource(cn).getLocalName();
	}

	@Override
	public void setName(ModelResource cn) {
		// TODO Auto-generated method stub
		
	}

	/******************************** set/get ComputeNode properties **********************/
	@Override
	public void setImage(ComputeNode cn, String imageURL, String imageHash, String shortName){
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
		return NdlCommons.getIndividualsImageURL(this.getModelResource(cn));
	}

	@Override
	public String getImageHash(ComputeNode cn) {
		return NdlCommons.getIndividualsImageHash(this.getModelResource(cn));
	}

	@Override
	public String getImageShortName(ComputeNode cn) {
		//getImageShortName not implemented
		return "";
		//return NdlCommons.getIndividualsImageURL(this.getModelResource(cn));		
	}
	
	@Override
	public void setNodeType(ComputeNode computeNode, String nodeType) {
		try{
			Individual ni = (Individual)this.getModelResource(computeNode);
			
			if (NDLGenerator.BAREMETAL.equals(nodeType))
				ngen.addBareMetalDomainProperty(ni);
			else if (NDLGenerator.FORTYGBAREMETAL.equals(nodeType))
				ngen.addFourtyGBareMetalDomainProperty(ni);
			else
				ngen.addVMDomainProperty(ni);
			if (NDLGenerator.nodeTypes.get(nodeType) != null) {
				Pair<String> nt = NDLGenerator.nodeTypes.get(nodeType);
				ngen.addNodeTypeToCE(nt.getFirst(), nt.getSecond(), ni);
			}
			
		}catch (ClassCastException e){
			LIBNDL.logger().error("Cannot cast ComputeNode resource to individual. " + computeNode.getName());
		}catch (NdlException e){
			LIBNDL.logger().error("NdlException setting image for " + computeNode.getName());
		}
	}

	@Override
	public String getNodeType(ComputeNode computeNode) {
		// TODO Auto-generated method stub
		Resource ceType = NdlCommons.getSpecificCE(this.getModelResource(computeNode));
		return RequestGenerator.reverseNodeTypeLookup(ceType); 
	}

	@Override
	public void setPostBootScript(ComputeNode computeNode, String postBootScript) {
		try{
			if ((postBootScript != null) && (postBootScript.length() > 0)) {
				ngen.addPostBootScriptToCE(postBootScript, (Individual)this.getModelResource(computeNode));
			} 
		}
		catch (ClassCastException e){
			LIBNDL.logger().error("Cannot cast ComputeNode resource to individual. " + computeNode.getName());
		}catch (NdlException e){
			LIBNDL.logger().error("NdlException setting image for " + computeNode.getName());
		}
		
	}

	@Override
	public String getPostBootScript(ComputeNode computeNode) {
		return NdlCommons.getPostBootScript((Individual)this.getModelResource(computeNode));
	}



	@Override
	public void setDomain(RequestResource requestResource, String d) {
		try{
			Individual domI = ngen.declareDomain(NDLGenerator.domainMap.get(d));
			ngen.addNodeToDomain(domI, (Individual)this.getModelResource(requestResource));
		}catch (ClassCastException e){
			LIBNDL.logger().error("Cannot cast ComputeNode resource to individual. " + requestResource.getName());
		}catch (NdlException e){
			LIBNDL.logger().error("NdlException setting image for " + requestResource.getName());
		}
	}
	
	@Override
	public String getDomain(RequestResource requestResource) {
		return NdlCommons.getDomain((Individual)this.getModelResource(requestResource)).getLocalName();
	}

	

	


}
