/*
* Copyright (c) 2011 RENCI/UNC Chapel Hill 
*
* @author Ilia Baldine
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
* and/or hardware specification (the "Work") to deal in the Work without restriction, including 
* without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
* sell copies of the Work, and to permit persons to whom the Work is furnished to do so, subject to 
* the following conditions:  
* The above copyright notice and this permission notice shall be included in all copies or 
* substantial portions of the Work.  
*
* THE WORK IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
* OUT OF OR IN CONNECTION WITH THE WORK OR THE USE OR OTHER DEALINGS 
* IN THE WORK.
*/
package orca.ahab.libndl.resources.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import orca.ahab.libndl.LIBNDL;
import orca.ahab.libndl.SliceGraph;
import orca.ahab.libndl.ndl.NDLModel;
import orca.ahab.libndl.Slice;

public class ComputeNode extends Node {	
	protected int nodeCount = 1; //The actual count of the nodes for a group
	protected int maxNodeCount = 1; //The max nodes a group can expand to. Used for autoip. Default = nodeCount
	protected boolean splittable = false;

	//protected Image image = null;
	protected String group = null;
	//protected String nodeType = null;
	//protected String postBootScript = null;
		
	//list of nodes that instantiate this group
	ArrayList<orca.ahab.libndl.resources.manifest.Node> manifestNodes; 
		
	


	protected List<String> managementAccess = null;

	// list of open ports
	protected String openPorts = null;

	public ComputeNode(SliceGraph sliceGraph, String name){
		super(sliceGraph,name);
		
		nodeCount = 1;
		manifestNodes = new ArrayList<orca.ahab.libndl.resources.manifest.Node>();
	}
	
	public void addManifestNode(orca.ahab.libndl.resources.manifest.Node node){
		manifestNodes.add(node);
		nodeCount++;
	}
	
	public Collection<orca.ahab.libndl.resources.manifest.Node> getManifestNodes(){
		return manifestNodes;
	}
	
	public void setImage(String url, String hash, String shortName){
		this.getNDLModel().setImage(this, url, hash, shortName);
	}
	
	
	//get image properties
	public String getImageUrl(){
		String url = null;
		try{
			url = this.getNDLModel().getImageURL(this); 
		} catch (Exception e){
			url = null;
		}
		return url;
	}
	
	public String getImageHash(){
		return this.getNDLModel().getImageHash(this); 
	}
	
	public String getImageShortName(){
		return this.getNDLModel().getImageShortName(this); 
	}
	
	
	public void setPostBootScript(String postBootScript){
		this.getNDLModel().setPostBootScript(this,postBootScript); 
	}
	
	public String getPostBootScript(){
		return this.getNDLModel().getPostBootScript(this); 
		//return postBootScript;
	}
	

	public int getMaxNodeCount(){
		return maxNodeCount;
	}
	
	public void setMaxNodeCount(int count){
		//if (sliceGraph.isNewRequest()){
		//	maxNodeCount = count;
		//	
		//	if(nodeCount > maxNodeCount){
		//		setNodeCount(maxNodeCount);
		//	}
		//}
		maxNodeCount = count;
		
	}
	
	
	public void initializeNodeCount(int nc) {
		nodeCount = nc;
		if(nodeCount > maxNodeCount){
			maxNodeCount = nodeCount;
		}
	}
	public int getNodeCount() {
		return nodeCount;
	}
	
	public void setNodeCount(int nc) {
		//LIBNDL.logger().debug("setNodeCount: nc = " + nc + ", nodeCount = " + nodeCount + ", isNewReqeust = " + sliceGraph.isNewRequest());
		
		if (nc <= 0){
			LIBNDL.logger().warn("setNodeCount: Node group size must be greater than 0");
			return;
		}
		
		if (nc < nodeCount ){
			LIBNDL.logger().warn("setNodeCount: Reducing node group size is not supported.  Please delete indivudual nodes.");
			return;
		}
		
		if (nc == nodeCount){
			LIBNDL.logger().warn("setNodeCount: Setting node group size to the current nodoe group size");
			return;
		}
		
		//if it is a modify
//		if (!sliceGraph.isNewRequest()){
//			if(nc > nodeCount){
//				LIBNDL.logger().debug("setNodeCount: " + nc);
//				sliceGraph.increaseComputeNodeCount(this, nc-nodeCount);
//			}
//		} else {
//			//if it is a new request
//			if(nodeCount > maxNodeCount){
//				maxNodeCount = nodeCount;
//			}
//		}
		LIBNDL.logger().debug("setNodeCount: Setting node group size to " + nc);
		nodeCount = nc;
				
	}	
	
	public void deleteNode(String uri){
		LIBNDL.logger().debug("ComputeNode.deleteNode: uri = " + uri); 
		nodeCount--;
		sliceGraph.deleteComputeNode(this, uri);
	}
	
	public String getNodeType() {
		return this.getNDLModel().getNodeType(this);
	}
	
	public void setNodeType(String nt) {
		this.getNDLModel().setNodeType(this, nt);
	}
	public void setSplittable(boolean f) {
		splittable = f;
	}
	
	public boolean getSplittable() {
		return splittable;
	}

		
	public Interface stitch(RequestResource r){
		LIBNDL.logger().debug("ComputeNode.stitch"); 
		Interface stitch = null;
		if (r instanceof Network){
			LIBNDL.logger().debug("ComputeNode.stitch:  calling InterfaceNode2Net");
			stitch = new InterfaceNode2Net(this,(Network)r,sliceGraph);
		} else {
			//Can't stitch computenode to r
			//Should throw exception
			LIBNDL.logger().error("Error: Cannot stitch OrcaComputeNode to " + r.getClass().getName());
			return null;
		}
		sliceGraph.addStitch(this,r,stitch);
		
		return stitch;
	}
	
	
	@Override
	public String getPrintText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete() {
		sliceGraph.deleteResource(this);
	}

	
	
}
