package orca.ahab.libndl.resources.request;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import orca.ahab.libndl.SliceGraph;
import orca.ahab.libndl.Slice;

import com.hp.hpl.jena.rdf.model.Resource;


/**
 * A generic resource with a state and a notice
 * @author ibaldin
 * @author pruth
 *
 */
public abstract class RequestResource{
	protected Slice slice;

	protected Set<RequestResource> dependencies = new HashSet<RequestResource>(); 
	//protected Set<Interface> interfaces = new HashSet<Interface>(); 
	//protected Set<ManifestResource> instantiation = new HashSet<ManifestResource>();
	
	//Jena model references
	protected Resource modelResource;
	
	public Resource getModelResource() {
		return modelResource;
	}

	public void setModelResource(Resource modelResource) {
		this.modelResource = modelResource;
	}

	// reservation state - should probably be an enumeration
	protected String state = null;
	
	//User controlled properties:
	protected String name;
	protected String domain; 
	
	
	public RequestResource(Slice slice){
		this.slice = slice;
	}
	
	//abstract methods 
	public abstract String getPrintText();
	public abstract Interface stitch(RequestResource r);

	//non-abstract methods
	public String getURI(){
		if(getModelResource() != null){
			return getModelResource().getURI();
		} 
		
		return null;
	}
	
	public String getName(){ 
		return name; 
	}

	public void setName(String s) {
		name = s;
	}

	public String getState() {
		return state;
	}

	protected void setState(String s) {
		state = s;
	}

	public String getDomain() {
		return domain;
	}
	
	public void setDomain(String d) {
		domain = d;
	}
	
	public Collection<Interface> getInterfaces() {
		return slice.getInterfaces(this);
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

	
	public void delete(){
		slice.deleteResource(this);
	}
	
}
