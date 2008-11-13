package com.energyict.genericprotocolimpl.webrtukp;

import java.io.Serializable;
import java.util.HashMap;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;

public class Cache implements Serializable{

	/**
	 * @author gna
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean changed = false;
	private int confProgChange;
	private HashMap<ObisCode, ProfileGeneric> genericProfiles;
	
	private UniversalObject[] objectList;
	
	public Cache() {
		this(null, -1);
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
	
    public UniversalObject[] getObjectList () {
        return objectList;   
    }
    
    public void saveObjectList(UniversalObject[] objectList) {
        this.objectList=objectList;
        setChanged(true);
    }
    
    public void setConfProfChange(int change){
    	if(change != this.confProgChange){
    		setChanged(true);
    		this.confProgChange = change;
    	} else {
    		setChanged(false);
    		this.confProgChange = change;
    	}
    	
    }
    
    protected void setChanged(boolean state){
    	this.changed = state;
    }
    
    public boolean isChanged(){
    	return this.changed;
    }

	public HashMap<ObisCode, ProfileGeneric> getGenericProfiles() {
		return this.genericProfiles;
	}

	public void setGenericProfiles(HashMap<ObisCode, ProfileGeneric> genericProfiles) {
		this.genericProfiles = genericProfiles;
	}
}
