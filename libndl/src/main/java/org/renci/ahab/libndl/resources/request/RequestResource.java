package org.renci.ahab.libndl.resources.request;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.renci.ahab.libndl.LIBNDL;
import org.renci.ahab.libndl.Slice;
import org.renci.ahab.libndl.SliceGraph;
import org.renci.ahab.libndl.ndl.NDLModel;
import org.renci.ahab.libndl.resources.common.ModelResource;

import com.hp.hpl.jena.rdf.model.Resource;


/**
 * A generic resource with a state and a notice
 * @author ibaldin
 * @author pruth
 *
 */
public abstract class RequestResource extends ModelResource{
	
	boolean isNew;

	
	public RequestResource(SliceGraph sliceGraph) {
		super(sliceGraph);
		isNew = false;
		
	}
	

	
	//User controlled properties:
	//protected String name;
	//protected String domain; 
	
	

	public abstract Interface stitch(RequestResource r);
	
	
	public Interface getInterface(RequestResource r){
		
		System.out.println("getInterface: " +sliceGraph.getInterfaces(this));
		for (Interface iface: sliceGraph.getInterfaces(this)){
			System.out.println("Possible iface: " + iface);
			if(iface.contains(this,r)){
				System.out.println("Found it: " + iface);
				return iface;
			}
		}
		System.out.println("Iface not found");
		return null;
	}
	
	
	
	
	//non-abstract methods
//	public String getURI(){
//		if(getModelResource() != null){
//			return getModelResource().getURI();
//		} 
//		
//		return null;
//	}
	
	public void setIsNew(boolean isNew){
		this.isNew = isNew;
	}
	
	public boolean isNew(){
		return isNew;
	}

	public String getState() {
		return this.getNDLModel().getState(this);
	}


	public String getDomain() {
		return this.getNDLModel().getDomain(this);
	}
	
	public void setDomain(String d) {
		this.getNDLModel().setDomain(this,d);
	}
	
	public Collection<Interface> getInterfaces() {
		return sliceGraph.getInterfaces(this);
	}
	
	
	
	public void setURL(String url){
		this.getNDLModel().setURL(this,url);
	}
	 
	public String getURL(){
		return this.getNDLModel().getURL(this);
		
	}
	
	
	public void setGUID(String guid){
		this.getNDLModel().setGUID(this,guid);
	}
	public String getGUID(){
		return this.getNDLModel().getGUID(this);
	}
	
	public String getStitchingGUID(){
		return this.getNDLModel().getStitchingGUID(this);
	}
	
	
	//public Set<ManifestResource> getInstantiation() {
	//	return instantiation;
	//}
	
	//public void addInterface(Interface i){
	//	interfaces.add(i);
	//}
	
	//public void addInstantiationResource(ManifestResource r){
	//	instantiation.add(r);
	//}

	

	
}
