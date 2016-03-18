package orca.ahab.libndl.resources.request;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import orca.ahab.libndl.SliceGraph;
import orca.ahab.libndl.ndl.NDLModel;
import orca.ahab.libndl.resources.common.ModelResource;
import orca.ahab.libndl.LIBNDL;
import orca.ahab.libndl.Slice;

import com.hp.hpl.jena.rdf.model.Resource;


/**
 * A generic resource with a state and a notice
 * @author ibaldin
 * @author pruth
 *
 */
public abstract class RequestResource extends ModelResource{
	


	public RequestResource(SliceGraph sliceGraph) {
		super(sliceGraph);
	}

	// reservation state - should probably be an enumeration
	protected String state = null;
	
	//User controlled properties:
	//protected String name;
	//protected String domain; 
	
	

	public abstract Interface stitch(RequestResource r);

	
	
	
	
	//non-abstract methods
//	public String getURI(){
//		if(getModelResource() != null){
//			return getModelResource().getURI();
//		} 
//		
//		return null;
//	}
	


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
