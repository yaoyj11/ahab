package orca.ahab.libndl.resources.request;

import orca.ahab.libndl.SliceGraph;
import orca.ahab.libndl.Slice;

/**
 * Orca storage node implementation
 * @author ibaldin
 *
 */
public class StorageNode extends Node {
	private static final String STORAGE = "Storage";
	protected long capacity = 0;
	// is this a storage on shared or dedicated network?
	protected boolean sharedNetworkStorage = true;
	protected boolean doFormat = true;
	protected String hasFSType = "ext4", hasFSParam = "-F -b 2048", hasMntPoint = "/mnt/target"; 
	
	public StorageNode(SliceGraph sliceGraph, String name) {
		super(sliceGraph, name);
	}
	
	public void setCapacity(long cap) {
		assert(cap >= 0);
		capacity = cap;
	}
	
	public long getCapacity() {
		return capacity;
	}
	
	/** 
	 * Create a detailed printout of properties
	 * @return
	 */
//	@Override
//	public String getViewerText() {
//		String viewText = "";
//		viewText += "Storage node: " + name;
//		viewText += "\nStorage reservation state: " + (state != null ? state : NOT_SPECIFIED);
//		viewText += "\nReservation notice: " + (resNotice != null ? resNotice : NOT_SPECIFIED);
//		viewText += "Capacity: " + capacity;
//		
//		viewText += "\n\nInterfaces: ";
//		for(Map.Entry<OrcaLink, Pair<String>> e: addresses.entrySet()) {
//			viewText += "\n\t" + e.getKey().getName() + ": " + e.getValue().getFirst() + "/" + e.getValue().getSecond();
//		}
//		return viewText;
//	}
	
	public void setSharedNetwork() {
		sharedNetworkStorage = true;
	}
	
	public void setDedicatedNetwork() {
		sharedNetworkStorage = false;
	}
	
	public boolean getSharedNetwork() {
		return sharedNetworkStorage;
	}
	
	public void setDoFormat(boolean m) {
		doFormat = m;
	}
	
	public boolean getDoFormat() {
		return doFormat;
	}
	
	public void setFS(String t, String p, String m) {
		hasFSType = t;
		hasFSParam = p;
		hasMntPoint = m;
	}
	
	public String getFSType() {
		return hasFSType;
	}
	
	public String getFSParam() {
		return hasFSParam;
	}
	
	public String getMntPoint() {
		return hasMntPoint;
	}
	
	
	public Interface stitch(RequestResource r){
		Interface stitch = null;
		if (r instanceof Network){
			stitch = new InterfaceNode2Net(this,(Network)r,sliceGraph);		
		} else {
			//Can't stitch storage to r
			//Should throw exception
			System.out.println("Error: Cannot stitch OrcaStorageNode to " + r.getClass().getName());
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
