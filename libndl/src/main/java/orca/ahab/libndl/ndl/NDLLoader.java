package orca.ahab.libndl.ndl;


import orca.ahab.libndl.Slice;
import orca.ahab.libndl.SliceGraph;
import orca.ndl.NdlCommons;
import orca.ndl.NdlRequestParser;

public abstract class NDLLoader {
	protected SliceGraph sliceGraph;
	
	abstract NdlCommons load(String rdf);
}
