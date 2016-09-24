package orca.ahab.libndl.ndl;


import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.uci.ics.jung.graph.util.Pair;
import orca.ahab.libndl.LIBNDL;
import orca.ahab.libndl.SliceGraph;
import orca.ahab.libndl.resources.common.ModelResource;
import orca.ahab.libndl.resources.request.BroadcastNetwork;
import orca.ahab.libndl.resources.request.ComputeNode;
import orca.ahab.libndl.resources.request.InterfaceNode2Net;
import orca.ahab.libndl.resources.request.Network;
import orca.ahab.libndl.resources.request.RequestResource;
import orca.ahab.libndl.resources.request.StitchPort;
import orca.ahab.libndl.resources.request.StorageNode;
import orca.ndl.NdlCommons;
import orca.ndl.NdlException;
import orca.ndl.NdlGenerator;

public abstract class NDLModel {
	
	
	/* map of RequestResource in slice changes to ndl Resource */
	protected Map<ModelResource, Resource> request2NDLMap; 
	
	/* ndl generation */
	protected NdlGenerator ngen;
	protected Individual reservation; 
	
	abstract public void init(SliceGraph sliceGraph, String rdf);
	abstract public boolean isNewSlice();
	
	abstract public void add(ComputeNode cn, String name);
	abstract public void add(BroadcastNetwork bn, String name);
	abstract public void add(StitchPort sp);
	abstract public void add(InterfaceNode2Net i);
	abstract public void add(StorageNode sn);
	
	abstract public void remove(ComputeNode cn);
	abstract public void remove(BroadcastNetwork bn);
	abstract public void remove(StitchPort sp);
	abstract public void remove(InterfaceNode2Net i);
	abstract public void remove(StorageNode sn);
	
	//Methods that are the same for all model types
	//abstract public void setImage(ComputeNode cn, String imageURL, String imageHash, String shortName);
	//abstract public String getImageURL(ComputeNode cn);
	//abstract public String getImageHash(ComputeNode cn);
	//abstract public String getImageShortName(ComputeNode cn);
	
	protected NDLModel(){
		request2NDLMap = new HashMap<ModelResource,Resource>();
	}

	protected void mapRequestResource2ModelResource(ModelResource r, Resource i){
		request2NDLMap.put(r,i);
	}
	
	
	protected Resource getModelResource(ModelResource cn){
		return request2NDLMap.get(cn);
	}
	
	public void printRequest2NDLMap(){
		LIBNDL.logger().debug("NDLModle::printRequest2NDLMap: " + request2NDLMap);
	}
	
	protected Logger logger(){
		return LIBNDL.logger();
	}
	
	abstract public String getRequest();
	
	//abstract public String getName(ModelResource modelResource);
	//abstract public void setName(ModelResource modelResource);
	
	//abstract public String getNodeType(ComputeNode computeNode);
	//abstract public void setNodeType(ComputeNode computeNode, String nodeType);
	
	//abstract public void setPostBootScript(ComputeNode computeNode, String postBootScript);
	//abstract public String getPostBootScript(ComputeNode computeNode);
	
	//abstract public String getDomain(RequestResource requestResource);
    //abstract public void setDomain(RequestResource requestResource, String d);
    
	
    public String getURL(ModelResource modelResource){
    	return this.getModelResource(modelResource).getURI();
    	//return NdlCommons.getURL(this.getModelResource(requestResource));
    }
    public void setURL(ModelResource modelResource, String url){
    	//not implemented.  should it be?  i'm not sure.
    }
    
	public String getGUID(ModelResource modelResource){
		return NdlCommons.getGuidProperty(this.getModelResource(modelResource));
	}
	public void setGUID(ModelResource modelResource, String guid){
		//not implemented
	}

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

	public String getImageURL(ComputeNode cn) {
		return NdlCommons.getIndividualsImageURL(this.getModelResource(cn));
	}

	public String getImageHash(ComputeNode cn) {
		return NdlCommons.getIndividualsImageHash(this.getModelResource(cn));
	}

	public String getImageShortName(ComputeNode cn) {
		//getImageShortName not implemented
		return "getImageShortName not implemented";
		//return NdlCommons.getIndividualsImageURL(this.getModelResource(cn));		
	}
	
	public String getName(ModelResource cn) {
		//return this.getModelResource(cn).getLocalName();
		return this.getPrettyName(this.getModelResource(cn));
	}

	public void setName(ModelResource cn) {
		// TODO Auto-generated method stub
	}
	
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

	
	public String getNodeType(ComputeNode computeNode) {
		// TODO Auto-generated method stub
		Resource ceType = NdlCommons.getSpecificCE(this.getModelResource(computeNode));
		return RequestGenerator.reverseNodeTypeLookup(ceType); 
	}

	public String getReservationNotice(RequestResource rr) {
		return NdlCommons.getResourceReservationNotice(getModelResource(rr));
	}
	
	public String getState(RequestResource rr) {
		return NdlCommons.getResourceStateAsString(getModelResource(rr));
	}
	
	
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

	public String getPostBootScript(ComputeNode computeNode) {
		return  NdlCommons.getPostBootScript(this.getModelResource(computeNode));
	}

	public List<String> getManagementServices(ComputeNode computeNode) {
		List<String> services = NdlCommons.getNodeServices(this.getModelResource(computeNode));
		//LIBNDL.logger().debug("NDLModel::getManagementIP: " + services);
		return services;
	}
	
	public long getBandwidth(Network network){
		return NdlCommons.getResourceBandwidth(getModelResource(network));
	}
	
	public void setBandwidth(Network network, long bandwidth){
		try {
			Individual ifaceIndividual = (Individual)getModelResource(network);
			ngen.addBandwidthToConnection(ifaceIndividual, bandwidth);
		} catch (NdlException e) {
			e.printStackTrace();
		}
	}
	
	public String getIP(InterfaceNode2Net interfaceNode2Net) {
		return NdlCommons.getAddressIP(getModelResource(interfaceNode2Net));
	}
	public void setIP(InterfaceNode2Net interfaceNode2Net, String ipAddress) {
		//LIBNDL.logger().debug("NDLModel::setIP:  " + this.getModelResource(interfaceNode2Net));	
		try {
			Individual interfaceIndividual = (Individual) this.getModelResource(interfaceNode2Net);
			LIBNDL.logger().debug("NDLModel::setIP:  interfaceIndivdual = " + interfaceIndividual);
			LIBNDL.logger().debug("NDLModel::setIP:  interfaceIndivdual.getName = " + interfaceNode2Net.getName());
			ngen.addUniqueIPToIndividual(ipAddress, interfaceNode2Net.getName(), interfaceIndividual);
//			ngen.addNetmaskToIP(ipInd, "255.255.0.0");
		} catch (NdlException e) {
			e.printStackTrace();
		}
		
	}
	public String getNetMask(InterfaceNode2Net interfaceNode2Net) {
		return NdlCommons.getInterfaceNetmask(getModelResource(interfaceNode2Net));
	}
	public void setNetMask(InterfaceNode2Net interfaceNode2Net, String netmask) {
		try {
			//Individual interfaceIndivdual = (Individual) this.getModelResource(interfaceNode2Net);
			Individual ipInd = ngen.getRequestIndividual(interfaceNode2Net.getName());
			ngen.addNetmaskToIP(ipInd, netmask);
		} catch (NdlException e) {
			e.printStackTrace();
		}
	}
	
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
	
	
	public String getDomain(RequestResource requestResource) {
		if(this.getModelResource(requestResource) instanceof com.hp.hpl.jena.rdf.model.impl.ResourceImpl){
			//Special case for nodes that are already in the manifest (i.e. are instances of ResourceImpl
			return RequestGenerator.reverseLookupDomain(NdlCommons.getDomain(this.getModelResource(requestResource)));
		} 
		
		//General case for regular resources
		return NdlCommons.getDomain((Individual)this.getModelResource(requestResource)).getLocalName();
		
	}
    
	/**
	 * Hacks that should be in ndlcommons
	 */
	
	// sometimes getLocalName is not good enough
		// so we strip off orca name space and call it a day
		protected String getTrueName(Resource r) {
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
	
  
	
	
}
