/**
 * 
 */
package org.renci.ahab.libndl.resources.request;

import org.renci.ahab.libndl.SliceGraph;
import org.renci.ahab.libndl.resources.common.ModelResource;

/**
 * @author geni-orca
 *
 */
public abstract class Interface extends ModelResource{
	RequestResource a;
	RequestResource b;
		
	public void setURL(String url){
		this.getNDLModel().setURL(this,url);
	}
	 
	public String getURL(){
		return this.getNDLModel().getURL(this);
		
	}
	
	
	public void setGUID(String guid){
		this.getNDLModel().setGUID(this,guid);
	}
	public String getGUID(){
		return this.getNDLModel().getGUID(this);
	}
	
	
	public Interface(RequestResource a, RequestResource b, SliceGraph sliceGraph){
		super(sliceGraph);
		this.a = a;
		this.b = b;
	}

	public boolean contains(RequestResource testa, RequestResource testb) {
		if((a.equals(testa) && b.equals(testb)) ||  
		   (a.equals(testb) && b.equals(testa))	) {
				return true;
			}
		return false;
	}
}
