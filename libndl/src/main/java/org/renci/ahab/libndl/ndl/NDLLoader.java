package org.renci.ahab.libndl.ndl;


import org.renci.ahab.libndl.Slice;
import org.renci.ahab.libndl.SliceGraph;

import orca.ndl.NdlCommons;
import orca.ndl.NdlRequestParser;

public abstract class NDLLoader {
	protected SliceGraph sliceGraph;
	
	abstract NdlCommons load(String rdf);
}
