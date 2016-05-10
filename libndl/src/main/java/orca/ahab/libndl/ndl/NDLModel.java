package orca.ahab.libndl.ndl;

import java.util.HashMap;
import java.util.Map;

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
import orca.ahab.libndl.resources.request.Interface;
import orca.ahab.libndl.resources.request.InterfaceNode2Net;
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
	
	abstract public void setImage(ComputeNode cn, String imageURL, String imageHash, String shortName);
	abstract public String getImageURL(ComputeNode cn);
	abstract public String getImageHash(ComputeNode cn);
	abstract public String getImageShortName(ComputeNode cn);
	
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
	
	abstract public String getName(ModelResource modelResource);
	abstract public void setName(ModelResource modelResource);
	
	abstract public String getNodeType(ComputeNode computeNode);
	abstract public void setNodeType(ComputeNode computeNode, String nodeType);
	
	abstract public void setPostBootScript(ComputeNode computeNode, String postBootScript);
	abstract public String getPostBootScript(ComputeNode computeNode);
	
	abstract public String getDomain(RequestResource requestResource);
    abstract public void setDomain(RequestResource requestResource, String d);
    
    
    public String getURL(RequestResource requestResource){
    	return this.getModelResource(requestResource).getURI();
    	//return NdlCommons.getURL(this.getModelResource(requestResource));
    }
    public void setURL(RequestResource requestResource, String url){
    	//not implemented.  should it be?  i'm not sure.
    }
    
	public String getGUID(RequestResource requestResource){
		return NdlCommons.getGuidProperty(this.getModelResource(requestResource));
	}
	public void setGUID(RequestResource requestResource, String guid){
		//not implemented
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
