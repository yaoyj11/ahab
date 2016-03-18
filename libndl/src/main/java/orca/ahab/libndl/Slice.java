package orca.ahab.libndl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;

import orca.ahab.libndl.ndl.ExistingSliceModel;
import orca.ahab.libndl.ndl.NDLModel;
import orca.ahab.libndl.ndl.NewSliceModel;
import orca.ahab.libndl.ndl.RequestGenerator;
import orca.ahab.libndl.resources.common.ModelResource;
import orca.ahab.libndl.resources.request.BroadcastNetwork;
import orca.ahab.libndl.resources.request.ComputeNode;
import orca.ahab.libndl.resources.request.Interface;
import orca.ahab.libndl.resources.request.Network;
import orca.ahab.libndl.resources.request.Node;
import orca.ahab.libndl.resources.request.RequestResource;
import orca.ahab.libndl.resources.request.StitchPort;
import orca.ahab.libndl.resources.request.StorageNode;
import orca.ahab.libndl.util.IP4Subnet;
import orca.ndl.NdlRequestParser;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.uci.ics.jung.graph.SparseMultigraph;



public class Slice {
	
	private SliceGraph sliceGraph;
	
	private Slice(){
		sliceGraph = new SliceGraph(this);
	}
	
	public static Slice create(){
		return new Slice();
	}
	
	public static Slice loadRequestFile(String fileName){
		return Slice.loadRequest(readRDFFile(new File(fileName)));
	}
	
	public static Slice loadManifestFile(String fileName){
		return Slice.loadManifest(readRDFFile(new File(fileName)));
	}
	
	
	public static Slice loadRequest(String requestRDFString){
		Slice s = new Slice();
		s.sliceGraph.loadRequestRDF(requestRDFString);
		return s;

	}
	
	public static Slice loadManifest(String manifestRDFString){
		Slice s = new Slice();
		s.sliceGraph.loadManifestRDF(manifestRDFString);
		
		return s; 
	}
	
	private static String readRDFFile(File f){
		BufferedReader bin = null; 
		String rawRDF = null;
		try {
			FileInputStream is = new FileInputStream(f);
			bin = new BufferedReader(new InputStreamReader(is, "UTF-8"));

			StringBuilder sb = new StringBuilder();

			String line = null;
			while((line = bin.readLine()) != null) {
				sb.append(line);
				// re-add line separator
				sb.append(System.getProperty("line.separator"));
			}

			bin.close();

			rawRDF = sb.toString();

		} catch (Exception e) {
			LIBNDL.logger().debug("error reading file " + f.toString());
			e.printStackTrace();
			return null;
		} 

		return rawRDF;
	}
	
	/************************ Private configuration methods ***************/
	
	
	/************************ User API Methods ****************************/
	
	public ComputeNode addComputeNode(String name){
		return sliceGraph.addComputeNode(name);
	}

	public StorageNode addStorageNode(String name){
		return sliceGraph.addStorageNode(name);
	}

	public StitchPort addStitchPort(String name){
		return sliceGraph.addStitchPort(name);	 
	}
	SparseMultigraph<RequestResource, Interface> g = new SparseMultigraph<RequestResource, Interface>();

	public Network addLink(String name){
		return sliceGraph.addLink(name);
	}

	public BroadcastNetwork addBroadcastLink(String name){
		return sliceGraph.addBroadcastLink(name);
	}
		
	public RequestResource getResourceByName(String nm){
		return sliceGraph.getResourceByName(nm);
	}
	
	public RequestResource getResouceByURI(String uri){
		return sliceGraph.getResourceByURI(uri);
	}
	
	//public void deleteResource(RequestResource r){
	//	sliceGraph.deleteResource(r);
	//}
	
	public Interface stitch(RequestResource r1, RequestResource r2){
		LIBNDL.logger().error("slice.stitch is unimplemented");
		return null;
	}
	
	public void autoIP(){
		for (Network n : sliceGraph.getLinks()){
			n.autoIP();
		}
	}
	
	
	/**************************** Get Slice Info ***********************************/
	public Collection<ModelResource> getAllResources(){
		return sliceGraph.getResources();
	}
	
	public Collection<Interface> getInterfaces(){
		return sliceGraph.getInterfaces();
	}

	public Collection<Network> getLinks(){
		return sliceGraph.getLinks();
	}
		
	public Collection<BroadcastNetwork> getBroadcastLinks(){
		return sliceGraph.getBroadcastLinks();
	}
	
	public Collection<Node> getNodes(){
		return sliceGraph.getNodes();
	}
	
	public Collection<ComputeNode> getComputeNodes(){
		return sliceGraph.getComputeNodes();
	}
	
	public Collection<StorageNode> getStorageNodes(){
		return sliceGraph.getStorageNodes();
	}	

	public Collection<StitchPort> getStitchPorts(){
		return sliceGraph.getStitchPorts();
	}	
	
	public String getState(){
		return "getState unimplimented";
	}
	
	
	public static Collection<String> getDomains(){
		return RequestGenerator.domainMap.keySet();
	}
	
	/**************************** Load/Save Methods **********************************/
	
	private void save(String file){
		sliceGraph.save(file);
	}
	
	public String getRequest(){
		return sliceGraph.getRDFString();
	}

	/**************************** Logger Methods *************************************/
	public Logger logger(){
		return LIBNDL.logger();
	}
	
	/***************************** Debug Methods **************/
	public String getDebugString(){
		return sliceGraph.getDebugString();
	}
	
	/*****************************  Auto generatted methods to be sorted **************/
	public Collection<Interface> getInterfaces(RequestResource requestResource) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isNewRequest() {
		// TODO Auto-generated method stub
		return false;
	}

	public void increaseComputeNodeCount(ComputeNode computeNode, int i) {
		// TODO Auto-generated method stub
		
	}

	public void deleteComputeNode(ComputeNode computeNode, String uri) {
		// TODO Auto-generated method stub
		
	}

	public void addStitch(ComputeNode computeNode, RequestResource r, Interface stitch) {
		// TODO Auto-generated method stub
		
	}

	public IP4Subnet setSubnet(String ip, int mask) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addStitch(StorageNode storageNode, RequestResource r, Interface stitch) {
		// TODO Auto-generated method stub
		
	}

	public void addStitch(StitchPort stitchPort, RequestResource r, Interface stitch) {
		// TODO Auto-generated method stub
		
	}

	public IP4Subnet allocateSubnet(int dEFAULT_SIZE) {
		// TODO Auto-generated method stub
		return null;
	}
	 
}
