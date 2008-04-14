/**
 * 
 */
package com.energyict.genericprotocolimpl.iskragprs;

import java.io.Serializable;

import com.energyict.dlms.UniversalObject;

/**
 * @author kvd
 *
 */
public class Cache implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5702561538312339908L;
	UniversalObject[] objectList;
    int confProgChange;
    boolean changed;

	/**
	 * 
	 */
	public Cache() {
		this(null,-1);
	}

	public Cache(UniversalObject[] objectList,int confProgChange) {
        this.objectList=objectList;
        this.confProgChange=confProgChange;
        setChanged(false);
	}
	
	public Cache(UniversalObject[] objectList,int confProgChange,boolean changed) {
        this.objectList=objectList;
        this.confProgChange=confProgChange;
        this.changed=changed;
	}
	
    public boolean isChanged() {
        return changed;   
    }
    
    public void setChanged(boolean changed) {
        this.changed = changed;
    }
    
    public void saveObjectList(UniversalObject[] objectList) {
        this.objectList=objectList;
        setChanged(true);
    }
    
    public UniversalObject[] getObjectList () {
        return objectList;   
    }
    
    public void setConfProgChange(int confProgChange) {
        this.confProgChange=confProgChange;
        setChanged(true);
    }
    
    public int getConfProgChange() {
        return confProgChange;   
    }

}
