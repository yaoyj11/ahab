package orca.ahab.libndl;

import java.util.Collection;

import orca.ahab.libndl.ndl.RequestSaver;
import orca.ahab.libndl.resources.request.BroadcastNetwork;
import orca.ahab.libndl.resources.request.ComputeNode;
import orca.ahab.libndl.resources.request.Interface;
import orca.ahab.libndl.resources.request.Network;
import orca.ahab.libndl.resources.request.Node;
import orca.ahab.libndl.resources.request.RequestResource;
import orca.ahab.libndl.resources.request.StitchPort;
import orca.ahab.libndl.resources.request.StorageNode;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;



public class Slice {
	Request request;
	Manifest manifest;
	
	//Manifest state (if available)
	String state;
	
	protected static Logger logger;
	
	private boolean isNewSlice;
	
	public Slice(){
		logger = Logger.getLogger(NDLLIBCommon.class.getCanonicalName());
		logger.setLevel(Level.DEBUG);
		
		request = new Request(this);
		manifest = new Manifest(this);
		
		isNewSlice = true;
		
		state = null;
	}
	
	
	/************************ User API Methods ****************************/
	
	public ComputeNode addComputeNode(String name){
		return request.addComputeNode(name);		
	}

	public StorageNode addStorageNode(String name){
		return request.addStorageNode(name);
	}

	public StitchPort addStitchPort(String name){
		return request.addStitchPort(name);
	}

	public Network addLink(String name){
		return request.addLink(name);
	}

	public BroadcastNetwork addBroadcastLink(String name){
		return request.addBroadcastLink(name);
	}
		
	public RequestResource getResourceByName(String nm){
		return request.getResourceByName(nm);
	}
	
	public RequestResource getResouceByURI(String uri){
		return request.getResourceByURI(uri);
	}
	
	public void deleteResource(RequestResource r){
		request.deleteResource(r);
	}
	
	public Interface stitch(RequestResource r1, RequestResource r2){
		logger.error("slice.stitch is unimplemented");
		return null;
	}
	
	public void autoIP(){
		for (Network n : request.getLinks()){
			n.autoIP();
		}
	}
	
	
	/**************************** Get Slice Info ***********************************/
	public Collection<RequestResource> getAllResources(){
		return request.getResources();
	}
	
	public Collection<Interface> getInterfaces(){
		return request.getInterfaces();
	}

	public Collection<Network> getLinks(){
		return request.getLinks();
	}
		
	public Collection<BroadcastNetwork> getBroadcastLinks(){
		return request.getBroadcastLinks();
	}
	
	public Collection<Node> getNodes(){
		return request.getNodes();
	}
	
	public Collection<ComputeNode> getComputeNodes(){
		return request.getComputeNodes();
	}
	
	public Collection<StorageNode> getStorageNodes(){
		return request.getStorageNodes();
	}	

	public Collection<StitchPort> getStitchPorts(){
		return request.getStitchPorts();
	}	
	
	public String getState(){
		return state;
	}
	
	public void setState(String state){
		this.state = state;
	}
	
	public static Collection<String> getDomains(){
		return RequestSaver.domainMap.keySet();
	}
	
	/**************************** Load/Save Methods **********************************/
	public void loadFile(String file){
		request.loadFile(file);
		isNewSlice = manifest.loadFile(file);
		request.setIsNewRequest(isNewSlice);
		logger.debug("Slice has manifest? " + isNewSlice);
	}
	
	public void loadRDF(String rdf){
		request.loadRDF(rdf);
		isNewSlice = manifest.loadRDF(rdf);
		request.setIsNewRequest(isNewSlice);
		logger.debug("Slice has manifest? " + isNewSlice);
	}
	
	public void save(String file){
		if(isNewSlice){
			request.save(file);
		} else { 
			request.saveModifyRequest(file);
		}
	}
	
	public String getRequest(){
		
		if(isNewSlice){
			return request.getRDFString();
		} else { 
			return request.getModifyRDFString();
		}
	}

	/**************************** Logger Methods *************************************/
	public Logger logger(){
		return logger;
	}
	
	/***************************** User debug methods ********************************/
	public String getRequestString(){
		return request.getDebugString();
	}
	public String getManifestString(){
		return manifest.getDebugString();
	}
	 
}
