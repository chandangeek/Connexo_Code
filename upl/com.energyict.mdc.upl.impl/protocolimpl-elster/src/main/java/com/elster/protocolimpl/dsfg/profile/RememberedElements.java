package com.elster.protocolimpl.dsfg.profile;

import com.elster.protocolimpl.dsfg.telegram.DataElement;

/**
 * simple return result class
 * 
 * @author heuckeg
 *
 */
public class RememberedElements {
	private DataElement first = null;
	private DataElement last = null;
	
	public RememberedElements() {
		
	}
	
	public void setFirst(DataElement value) {
		first = value;
	}
	
	public void setLast(DataElement value) {
		last = value;
	}
	
	public DataElement getFirst() {
		return first;
	}
	
	public DataElement getLast() {
		return last;
	}
	
	public boolean isFirstSet() {
		return first != null;
	}
}
