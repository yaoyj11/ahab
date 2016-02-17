/**
 * 
 */
package orca.ahab.libndl.resources.request;

/**
 * @author geni-orca
 *
 */
public abstract class Interface{
	RequestResource a;
	RequestResource b;
		
	public Interface(RequestResource a, RequestResource b){
		this.a = a;
		this.b = b;
	}
}
