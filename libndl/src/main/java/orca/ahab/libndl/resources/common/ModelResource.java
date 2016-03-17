package orca.ahab.libndl.resources.common;

import java.util.HashSet;
import java.util.Set;

import orca.ahab.libndl.LIBNDL;
import orca.ahab.libndl.SliceGraph;
import orca.ahab.libndl.ndl.NDLModel;
import orca.ahab.libndl.resources.request.RequestResource;

public abstract class ModelResource {
	protected SliceGraph sliceGraph;

	protected Set<RequestResource> dependencies = new HashSet<RequestResource>(); 
	//protected Set<Interface> interfaces = new HashSet<Interface>(); 
	//protected Set<ManifestResource> instantiation = new HashSet<ManifestResource>();
	
	
	private NDLModel getNDLModel() {
		if(sliceGraph == null) LIBNDL.logger().debug("ModelResource::getNDLModel sliceGraph is null");
		
		return sliceGraph.getNDLModel();
	}
	
	public ModelResource(SliceGraph sliceGraph){
		this.sliceGraph = sliceGraph;
	}
	
	//abstract methods 
	public abstract String getPrintText();
	
	public String getName(){ 
		try{
			return getNDLModel().getName(this); 
		} catch (Exception e){
			LIBNDL.logger().debug("resouce name not found");
			LIBNDL.logger().debug(e.toString());
		}
		return "resource name not found";
	}

	public void setName(String s) {
		getNDLModel().setName(this);
		//name = s;
	}
	
	public abstract void delete(); 
	//{
	//	sliceGraph.deleteResource(this);
	//}
	
}
