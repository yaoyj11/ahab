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
package orca.ahab.libndl;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import orca.ahab.libndl.ndl.ManifestLoader;
import orca.ahab.libndl.resources.manifest.CrossConnect;
import orca.ahab.libndl.resources.manifest.Interface;
import orca.ahab.libndl.resources.manifest.LinkConnection;
import orca.ahab.libndl.resources.manifest.ManifestResource;
import orca.ahab.libndl.resources.manifest.NetworkConnection;
import orca.ahab.libndl.resources.manifest.Node;
import edu.uci.ics.jung.graph.SparseMultigraph;

public class Manifest extends NDLLIBCommon {
	private Date start = null, end = null, newEnd = null;
	
	SparseMultigraph<ManifestResource, Interface> g = new SparseMultigraph<ManifestResource, Interface>();
	
	ManifestLoader mloader; 
	
	public Manifest(Slice slice){
		super(slice);
				
		Set<ManifestResource> nodes = new HashSet<ManifestResource>(g.getVertices());
		for (ManifestResource n: nodes)
			g.removeVertex(n);
	}
	
	
	//public Collection<OrcaResource> getRequestResources(){
	//	return requestGraph.getVertices();
	//}
	
	private SparseMultigraph<ManifestResource, Interface> getManifestGraph() {
		return g;
	}
	
	/*************************************   Add/Delete/Get resources  ************************************/
	

	
	public CrossConnect addCrossConnect(String name){
		CrossConnect c = new CrossConnect(slice,this,name);
		g.addVertex(c);
		return c;
	}
	public LinkConnection addLinkConnection(String name){
		LinkConnection c = new LinkConnection(slice,this,name);
		g.addVertex(c);
		return c;
	}
	public NetworkConnection addNetworkConnection(String name){
		NetworkConnection n = new NetworkConnection(slice,this,name);
		g.addVertex(n);
		return n;
	}
	public Node addNode(String name){
		Node n = new Node(slice,this,name);
		g.addVertex(n);
		return n;
	}

	
	
	public ManifestResource getResourceByName(String nm){
		if (nm == null)
			return null;
		
		for (ManifestResource n: g.getVertices()) {
			if (nm.equals(n.getName()) && n instanceof ManifestResource)
				return (ManifestResource)n;
		}
		return null;
	}
	
	public void deleteResource(ManifestResource r){
		for (Interface s: r.getInterfaces()){
			g.removeEdge(s);
		}
		g.removeVertex(r);
	}
	
	public void addStitch(ManifestResource a, ManifestResource b, Interface s){
		g.addEdge(s, a, b);
	}
	
	public Collection<Interface> getStitches(){
		ArrayList<Interface> interfaces = new ArrayList<Interface>();
		
		for (Interface i: g.getEdges()) {
			if (i instanceof Interface){
				interfaces.add((Interface)i);
			}
		}
		return interfaces;
	}
	
	public void clear(){
		//reset the whole request
	}
	
	
	
	
	/*************************************   RDF Functions:  save, load, getRDFString, etc. ************************************/
	
	public boolean loadFile(String file){
		mloader = new ManifestLoader(slice,this);
		return !mloader.loadFile(new File(file));
		
		
	}
	
	public boolean loadRDF(String rdf){
		mloader = new ManifestLoader(slice,this);
		return !mloader.loadRDF(rdf);
	}
	
	public String getRDFString(){
		return null;
	}

	
	
	
	/*************************************   debugging ************************************/
	public String getDebugString(){
		String rtnStr = "getDebugString: ";
		rtnStr += "ManifestGraph:\n" + g.toString();
		return rtnStr;
	}
	
	

	public void setManifestTerm(Date s, Date e) {
		start = s;
		end = e;
	}
	
	public void setNewEndDate(Date s) {

		if ((start == null) || (end == null))
			return;
		
		Long diff = s.getTime() - start.getTime();
		if (diff < 0)
			return;

		diff = s.getTime() - end.getTime();
		if (diff < 0)
			return;
		
		newEnd = s;	
	}
	
	public void resetEndDate() {
		end = newEnd;
		newEnd = null;
	}
	
	

//	void deleteSlice(String name) {
//		if ((name == null) || 
//				(name.length() == 0)) {
//			KMessageDialog kmd = new KMessageDialog(NDLLIB.getInstance().getFrame());
//			kmd.setMessage("You must specify a slice id");
//			kmd.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//			kmd.setVisible(true);
//			return;
//		}
//		
//		try {
//			OrcaSMXMLRPCProxy.getInstance().deleteSlice(name);
//		} catch (Exception ex) {
//			ExceptionDialog ed = new ExceptionDialog(NDLLIB.getInstance().getFrame(), "Exception");
//			ed.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//			ed.setException("Exception encountered while deleting slice manifest: ", ex);
//			ed.setVisible(true);
//		}
//	}
//	
//	void modifySlice(String name, String req) {
//		if ((name == null) || 
//				(name.length() == 0)) {
//			KMessageDialog kmd = new KMessageDialog(NDLLIB.getInstance().getFrame());
//			kmd.setMessage("You must specify a slice id");
//			kmd.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//			kmd.setVisible(true);
//			return;
//		}
//		try {
//			String s = OrcaSMXMLRPCProxy.getInstance().modifySlice(name, req);
//			TextAreaDialog tad = new TextAreaDialog(NDLLIB.getInstance().getFrame(), "Modify Output", 
//					"Modify Output", 
//					30, 50);
//			KTextArea ta = tad.getTextArea();
//
//			if (s != null)
//				ta.setText(s);
//			tad.pack();
//			tad.setVisible(true);
//		} catch (Exception ex) {
//			ExceptionDialog ed = new ExceptionDialog(NDLLIB.getInstance().getFrame(), "Exception");
//			ed.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//			ed.setException("Exception encountered while modifying slice: ", ex);
//			ed.setVisible(true);
//		}
//	}
	
	private String stripManifest(String m) {
		if (m == null)
			return null;
		int ind = m.indexOf("<rdf:RDF");
		if (ind > 0)
			return m.substring(ind);
		else
			return null;
	}

	public void addStitch(orca.ahab.libndl.resources.manifest.Node orcaComputeNode,
			orca.ahab.libndl.resources.manifest.ManifestResource r, Interface stitch) {
		// TODO Auto-generated method stub
		
	}
	
//	void queryManifest() {
//		// run request manifest from controller
//		if ((sliceIdField.getText() == null) || 
//				(sliceIdField.getText().length() == 0)) {
//			KMessageDialog kmd = new KMessageDialog(NDLLIB.getInstance().getFrame());
//			kmd.setMessage("You must specify a slice id");
//			kmd.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//			kmd.setVisible(true);
//			return;
//		}
//
//		try {
//			NDLLIBManifestState.getInstance().clear();
//
//			manifestString = OrcaSMXMLRPCProxy.getInstance().sliceStatus(sliceIdField.getText());
//
//			ManifestLoader ml = new ManifestLoader();
//
//			String realM = stripManifest(manifestString);
//			if (realM != null) {
//				if (ml.loadString(realM))
//					NDLLIB.getInstance().kickLayout(GuiTabs.MANIFEST_VIEW);
//			} else {
//				KMessageDialog kmd = new KMessageDialog(NDLLIB.getInstance().getFrame());
//				kmd.setMessage("Error has occurred, check raw controller response for details.");
//				kmd.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//				kmd.setVisible(true);
//				return;
//			}
//		} catch (Exception ex) {
//			ExceptionDialog ed = new ExceptionDialog(NDLLIB.getInstance().getFrame(), "Exception");
//			ed.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//			ed.setException("Exception encountered while querying ORCA for slice manifest: ", ex);
//			ed.setVisible(true);
//		}
//	}
	
//	public class ResourceButtonListener implements ActionListener {
//		public void actionPerformed(ActionEvent e) {
//			assert(sliceIdField != null);
//
//			if (e.getActionCommand().equals("manifest")) {
//				// run request manifest from controller
//				queryManifest();
//			} else 
//				if (e.getActionCommand().equals("raw")) {
//					TextAreaDialog tad = new TextAreaDialog(NDLLIB.getInstance().getFrame(), "Raw manifest", 
//							"Raw manifest", 
//							30, 50);
//					KTextArea ta = tad.getTextArea();
//
//					if (manifestString != null)
//						ta.setText(manifestString);
//					tad.pack();
//					tad.setVisible(true);
//				} else 
//					if (e.getActionCommand().equals("delete")) {
//						if ((sliceIdField.getText() == null) || 
//								(sliceIdField.getText().length() == 0)) {
//							KMessageDialog kmd = new KMessageDialog(NDLLIB.getInstance().getFrame());
//							kmd.setMessage("You must specify a slice id");
//							kmd.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//							kmd.setVisible(true);
//							return;
//						}
//
//						KQuestionDialog kqd = new KQuestionDialog(NDLLIB.getInstance().getFrame(), "Exit", true);
//						kqd.setMessage("Are you sure you want to delete slice " + sliceIdField.getText());
//						kqd.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//						kqd.setVisible(true);
//						if (!kqd.getStatus()) 
//							return;
//						deleteSlice(sliceIdField.getText());
//
//					} else 
//						if (e.getActionCommand().equals("listSlices")) {
//							try {
//								String[] slices = OrcaSMXMLRPCProxy.getInstance().listMySlices();
//								OrcaSliceList osl = new OrcaSliceList(NDLLIB.getInstance().getFrame(), slices);
//								osl.pack();
//								osl.setVisible(true);
//							} catch (Exception ex) {
//								ExceptionDialog ed = new ExceptionDialog(NDLLIB.getInstance().getFrame(), "Exception");
//								ed.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//								ed.setException("Exception encountered while listing user slices: ", ex);
//								ed.setVisible(true);
//							}
//						} else 
//							if (e.getActionCommand().equals("modify")) {
//								try {
//									if ((sliceIdField.getText() == null) || 
//											(sliceIdField.getText().length() == 0)) {
//										KMessageDialog kmd = new KMessageDialog(NDLLIB.getInstance().getFrame());
//										kmd.setMessage("You must specify a slice id");
//										kmd.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//										kmd.setVisible(true);
//										return;
//									}
//									ModifyTextSetter mts = new ModifyTextSetter(sliceIdField.getText());
//									TextAreaDialog tad = new TextAreaDialog(NDLLIB.getInstance().getFrame(), mts, 
//											"Modify Request", 
//											"Cut and paste the modify request into the window", 30, 50);
//									String txt = ModifySaver.getInstance().getModifyRequest();
//									if (txt != null)
//										tad.getTextArea().setText(txt);
//									tad.pack();
//									tad.setVisible(true);
//								} catch(Exception ex) {
//									ExceptionDialog ed = new ExceptionDialog(NDLLIB.getInstance().getFrame(), "Exception");
//									ed.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//									ed.setException("Exception encountered while modifying slice: ", ex);
//									ed.setVisible(true);
//								} 
//							} else
//								if (e.getActionCommand().equals("modifyClear")) {
//									ModifySaver.getInstance().clear();
//								} else
//									if (e.getActionCommand().equals("extend")) {
//										ReservationExtensionDialog red = new ReservationExtensionDialog(NDLLIB.getInstance().getFrame());
//										red.setFields(new Date());
//										red.pack();
//										red.setVisible(true);
//										
//										if (newEnd != null) {
//											try {
//												Boolean res = OrcaSMXMLRPCProxy.getInstance().renewSlice(sliceIdField.getText(), newEnd);
//												KMessageDialog kd = new KMessageDialog(NDLLIB.getInstance().getFrame(), "Result", true);
//												kd.setMessage("The extend operation returned: " + res);
//												kd.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//												kd.setVisible(true);
//												if (res)
//													resetEndDate();
//												else
//													newEnd = null;
//											} catch (Exception ee) {
//												ExceptionDialog ed = new ExceptionDialog(NDLLIB.getInstance().getFrame(), "Exception");
//												ed.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//												ed.setException("Exception encountered while extending slice: ", ee);
//												ed.setVisible(true);
//											}
//										} else {
//											KMessageDialog kmd = new KMessageDialog(NDLLIB.getInstance().getFrame());
//											kmd.setMessage("Invalid new end date.");
//											kmd.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//											kmd.setVisible(true);
//											return;
//										}
//									}
//		}
//	}
	
//	public void launchResourceStateViewer(Date start, Date end) {
//		// get a list of nodes and links
//		List<OrcaResource> resources = new ArrayList<OrcaResource>();
//		
//		resources.addAll(g.getVertices());
//		resources.addAll(g.getEdges());
//		
//		OrcaResourceStateViewer viewer = new OrcaResourceStateViewer(NDLLIB.getInstance().getFrame(), resources, start, end);
//		viewer.pack();
//		viewer.setVisible(true);
//	}
//	
//	ResourceButtonListener rbl = new ResourceButtonListener();
//	@Override
//	public ActionListener getActionListener() {
//		return rbl;
//	}

//	@Override
//	public void addPane(Container c) {
//		
//		Layout<OrcaNode, OrcaLink> layout = new FRLayout<OrcaNode, OrcaLink>(g);
//		
//		//layout.setSize(new Dimension(1000,800));
//		vv = 
//			new VisualizationViewer<OrcaNode,OrcaLink>(layout);
//		// Show vertex and edge labels
//		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<OrcaNode>());
//		vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<OrcaLink>());
//		
//		// Create a graph mouse and add it to the visualization viewer
//		OrcaNode.OrcaNodeFactory onf = new OrcaNode.OrcaNodeFactory(nodeCreator);
//		OrcaLink.OrcaLinkFactory olf = new OrcaLink.OrcaLinkFactory(linkCreator);
//		gm = new EditingModalGraphMouse<OrcaNode, OrcaLink>(vv.getRenderContext(), 
//				onf, olf);
//		
//		// add the plugin
//		PopupVertexEdgeMenuMousePlugin<OrcaNode, OrcaLink> myPlugin = new PopupVertexEdgeMenuMousePlugin<OrcaNode, OrcaLink>();
//		
//		// Add some popup menus for the edges and vertices to our mouse plugin.
//		// mode menu is not set for manifests
//		myPlugin.setEdgePopup(new MouseMenus.ManifestEdgeMenu());
//		myPlugin.setVertexPopup(new MouseMenus.ManifestNodeMenu());
//		
//		gm.remove(gm.getPopupEditingPlugin());  // Removes the existing popup editing plugin
//		gm.add(myPlugin);
//
//		// Add icon and shape (so pickable area roughly matches the icon) transformer
//		OrcaNode.OrcaNodeIconShapeTransformer st = new OrcaNode.OrcaNodeIconShapeTransformer();
//		vv.getRenderContext().setVertexShapeTransformer(st);
//		
//		OrcaNode.OrcaNodeIconTransformer it = new OrcaNode.OrcaNodeIconTransformer();
//		vv.getRenderContext().setVertexIconTransformer(it);
//		
//		// add listener to add/remove checkmarks on selected nodes
//		PickedState<OrcaNode> ps = vv.getPickedVertexState();
//        ps.addItemListener(new OrcaNode.PickWithIconListener(it));
//		
//		vv.setGraphMouse(gm);
//
//		vv.setLayout(new BorderLayout(0,0));
//		
//		c.add(vv);
//
//		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING); // Start off in panning mode  
//	}
//
//	@Override
//	public void deleteEdgeCallBack(OrcaLink e) {
//		// TODO Auto-generated method stub
//	}
//
//	@Override
//	public void deleteNodeCallBack(OrcaNode n) {
//		ModifySaver.getInstance().removeNodeFromGroup(n.getGroup(), n.getUrl());
//	}
//
//	public void saveManifestToIRods() {
//		IRodsICommands irods = new IRodsICommands();
//		if (manifestString == null) {
//			KMessageDialog md = new KMessageDialog(NDLLIB.getInstance().getFrame(), "Manifest Error", true);
//			md.setMessage("Manifest is empty!");
//			md.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//			md.setVisible(true);
//			return;
//		}
//		try {
//			String realM = stripManifest(manifestString);
//			// convert if needed
//			String iRodsName = IRodsICommands.substituteManifestName();
//			if (NDLLIB.getInstance().getPreference(PrefsEnum.IRODS_FORMAT).equalsIgnoreCase("rspec")) {
//				String rspec = NDLConverter.callConverter(NDLConverter.MANIFEST_TO_RSPEC, new Object[]{realM, sliceIdField.getText()});
//				irods.saveFile(iRodsName, rspec);
//			} else if (NDLLIB.getInstance().getPreference(PrefsEnum.IRODS_FORMAT).equalsIgnoreCase("ndl"))
//				irods.saveFile(iRodsName, realM);
//			else {
//				ExceptionDialog ed = new ExceptionDialog(NDLLIB.getInstance().getFrame(), "Exception");
//				ed.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//				ed.setException("Exception encountered while saving manifest to iRods: ", 
//						new Exception("unknown format " + NDLLIB.getInstance().getPreference(PrefsEnum.IRODS_FORMAT)));
//				ed.setVisible(true);
//			}
//			KMessageDialog md = new KMessageDialog(NDLLIB.getInstance().getFrame(), "Saving to iRods", true);
//			md.setMessage("Manifest saved as " + iRodsName);
//			md.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//			md.setVisible(true);
//		} catch (IRodsException ie) {
//			ExceptionDialog ed = new ExceptionDialog(NDLLIB.getInstance().getFrame(), "Exception");
//			ed.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//			ed.setException("Exception encountered while saving manifest to iRods: ", ie);
//			ed.setVisible(true);
//		} catch (Exception e) {
//			ExceptionDialog ed = new ExceptionDialog(NDLLIB.getInstance().getFrame(), "Exception");
//			ed.setLocationRelativeTo(NDLLIB.getInstance().getFrame());
//			ed.setException("Exception encountered while saving manifest to iRods: ", e);
//			ed.setVisible(true);
//		}
//	}
}
