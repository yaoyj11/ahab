/**
 * 
 */
package orca.ahab.libndl.resources.request;

import orca.ahab.libndl.SliceGraph;
import orca.ahab.libndl.resources.common.ModelResource;

/**
 * @author geni-orca
 *
 */
public abstract class Interface extends ModelResource{
	RequestResource a;
	RequestResource b;
		
	public Interface(RequestResource a, RequestResource b, SliceGraph sliceGraph){
		super(sliceGraph);
		this.a = a;
		this.b = b;
	}
}
