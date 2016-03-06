package orca.ahab.libndl.ndl;

import orca.ahab.libndl.Slice;

public abstract class NDLGenerator {
	protected Slice slice;
	
	abstract void generate(String rdf);
}
