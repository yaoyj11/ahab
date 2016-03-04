package orca.ahab.libndl.ndl;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Resource;

import orca.ahab.libndl.LIBNDL;
import orca.ahab.libndl.resources.request.BroadcastNetwork;
import orca.ahab.libndl.resources.request.ComputeNode;
import orca.ahab.libndl.resources.request.InterfaceNode2Net;
import orca.ahab.libndl.resources.request.RequestResource;
import orca.ahab.libndl.resources.request.StitchPort;
import orca.ahab.libndl.resources.request.StorageNode;
import orca.ndl.NdlCommons;
import orca.ndl.NdlException;
import orca.ndl.NdlGenerator;

public abstract class NDLModel {
	
	
	/* map of RequestResource in slice changes to ndl Resource */
	protected Map<RequestResource,Resource> request2NDLMap; 
	
	/* ndl generation */
	protected NdlGenerator ngen;
	protected Individual reservation; 
	
	abstract public void add(ComputeNode cn, String name);
	abstract public void add(BroadcastNetwork bn);
	abstract public void add(StitchPort sp);
	abstract public void add(InterfaceNode2Net i);
	abstract public void add(StorageNode sn);
	
	abstract public void remove(ComputeNode cn);
	abstract public void remove(BroadcastNetwork bn);
	abstract public void remove(StitchPort sp);
	abstract public void remove(InterfaceNode2Net i);
	abstract public void remove(StorageNode sn);
	
	protected NDLModel(){
		request2NDLMap = new HashMap<RequestResource,Resource>();
		

	}

	protected void mapRequestResource2ModelResource(RequestResource r, Resource i){
		request2NDLMap.put(r,i);
	}
	
	
	protected Resource getModelResource(RequestResource r){
		return request2NDLMap.get(r);
	}
	
	protected Logger logger(){
		return LIBNDL.logger();
	}
	
	abstract public String getRequest();
	
	abstract public String getName(RequestResource cn);
	abstract public void setName(RequestResource cn);
	
	
	
	
}
