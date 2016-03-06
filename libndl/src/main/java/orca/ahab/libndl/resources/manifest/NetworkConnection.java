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
package orca.ahab.libndl.resources.manifest;

import orca.ahab.libndl.Slice;

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
public class NetworkConnection extends LinkConnection {
	private static final String BROADCAST_LINK = "Broadcast link";
	// vlan or other path label
	protected String label = null;
	protected long bandwidth;
	
	
	public NetworkConnection(Slice slice, String name) {
		super(slice,name);
	}

	public void setLabel(String l) {
    	if ((l != null) && l.length() > 0)
    		label = l;
    	else
    		label = null;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setBandwidth(long b) {
		bandwidth = b;
	}
	
	public long getBandwidth() {
		return bandwidth;
	}
	
	/** 
	 * Create a detailed printout of properties
	 * @return
	 */
//	@Override
//	public String getViewerText() {
//		String viewText = "";
//		viewText += "Node name: " + name;
//		viewText += "\nNode reservation state: " + state;
//		viewText += "\nReservation notice: " + (resNotice != null ? resNotice : NOT_SPECIFIED);
//		if (label != null)
//			viewText += "\nLabel/Tag: " + label;
//		if (interfaces.size() > 0) {
//			viewText += "\nInterfaces: ";
//			for(Entry<OrcaNetwork, String> e: interfaces.entrySet()) {
//				viewText += "\n    " + e.getKey().getName() + " : " + e.getValue();
//			}
//		}
//		return viewText;
//	}
	
	// is this crossconnect linked to shared storage?
    public boolean linkToSharedStorage() {
    	
//    	Collection<OrcaLink> iLinks = Request.getInstance().getGraph().getIncidentEdges(this);
//		for(OrcaLink l: iLinks) {
//			Pair<OrcaNode> pn = Request.getInstance().getGraph().getEndpoints(l);
//			OrcaNode n = null;
//			// find the non-crossconnect side
//			if (!(pn.getFirst() instanceof OrcaBroadcastLink))
//				n = pn.getFirst();
//			else if (!(pn.getSecond() instanceof OrcaBroadcastLink))
//				n = pn.getSecond();
//			
//			if (n == null) 
//				continue;
//			
//			if (n instanceof OrcaStorageNode) {
//				OrcaStorageNode snode = (OrcaStorageNode)n;
//				if (snode.getSharedNetwork())
//					return true;
//			}
//		}
		return false;
    }

    

	
	public Interface stitch(ManifestResource r){
		Interface stitch = null;
		if (r instanceof Node){
			//stitch = ((Node)r).stitch(this);	
		} else {
			//Can't stitch link to r
			//Should throw exception
			System.out.println("Error: Cannot stitch link to " + r.getClass().getName());
			return null;
		}
		
		return stitch;
	}
	
	
	@Override
	public String getPrintText() {
		// TODO Auto-generated method stub
		return null;
	}
}
