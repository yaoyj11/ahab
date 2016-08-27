package orca.ahab.libndl.resources.request;

import java.util.Collection;

import orca.ahab.libndl.SliceGraph;
import orca.ahab.libndl.resources.common.ModelResource;


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

	// reservation state - should probably be an enumeration
	protected String state = null;
	
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
		return state;
	}

	protected void setState(String s) {
		state = s;
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
