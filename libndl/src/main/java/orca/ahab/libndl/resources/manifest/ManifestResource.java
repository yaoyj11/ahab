package orca.ahab.libndl.resources.manifest;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import orca.ahab.libndl.Manifest;
import orca.ahab.libndl.Slice;
import orca.ndl.NdlCommons;

import com.hp.hpl.jena.rdf.model.Resource;


/**
 * A generic resource with a state and a notice
 * @author ibaldin
 * @author pruth
 *
 */
public abstract class ManifestResource{
	protected Manifest manifest;
	protected Slice slice;
	
	protected Set<ManifestResource> dependencies = new HashSet<ManifestResource>(); 
	protected Set<Interface> interfaces = new HashSet<Interface>(); 
	
	protected Map<String, String> substrateInfo = new HashMap<String, String>();
	
	//Properties:
	protected String name;

	// reservation state
	protected String state = null;
	// reservation notice:  PRUTH--what is this?
	protected String resNotice = null;
	
	protected String domain; 
	
	//Jena model references
	protected Resource modelResource;
	
	public Resource getModelResource() {
		return modelResource;
	}

	public void setModelResource(Resource modelResource) {
		this.modelResource = modelResource;
	}
	
	
	public ManifestResource(Slice slice, Manifest manifest){
		this.manifest = manifest;
		this.slice = slice;
	}

	
	//abstact methods 
	public abstract String getPrintText();
	
	public String getURI(){
		return getModelResource().getURI();
	}

	public String getName(){ 
		return name; 
	}

	public void setName(String s) {
		name = s;
	}

	public String getState() {
		return NdlCommons.getResourceStateAsString(this.getModelResource());
	}

	public String getReservationNotice() {
		return resNotice;
	}

	public void setReservationNotice(String s) {
		resNotice = s;
	}

	public String getDomain() {
		return domain;
	}
	
	public void setDomain(String d) {
		domain = d;
	}
	
	public Collection<Interface> getInterfaces() {
		return interfaces;
	}
	
	
	/**
	 * Substrate info is just an associative array. 
	 * Describes some information about the substrate of the resource
	 */
	public void setSubstrateInfo(String t, String o) {
		substrateInfo.put(t, o);
	}

	public String getSubstrateInfo(String t) {
		return substrateInfo.get(t);
	}



	//
}
