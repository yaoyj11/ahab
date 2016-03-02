package orca.ahab.libndl.ndl;

import java.io.File;

import orca.ahab.libndl.Slice;

public abstract class NDLLoader {
	protected Slice slice;
	
	abstract void load(String rdf);
	abstract boolean loadFile(File f);
}
