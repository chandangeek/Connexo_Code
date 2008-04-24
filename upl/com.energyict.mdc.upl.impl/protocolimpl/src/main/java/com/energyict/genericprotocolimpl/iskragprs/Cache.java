/**
 * 
 */
package com.energyict.genericprotocolimpl.iskragprs;

import java.io.Serializable;
import java.util.ArrayList;

import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;

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
    
	public int genericInterval1;
	public int genericInterval2;
	public byte[] genericInterval3;
	public byte[] genericInterval4;
	public ArrayList monthlyProfileConfig;

	/**
	 * 
	 */
	public Cache() {
		this(null, -1, false, -1, -1, null, null, null);
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
	
    public Cache(UniversalObject[] objectList,int confProgChange,boolean changed, int gInterval1, 
    		int gInterval2, byte[] gInterval3, byte[] gInterval4, ArrayList profileConfig) {
        this.objectList=objectList;
        this.confProgChange=confProgChange;
        this.changed=changed;
        this.genericInterval1 = gInterval1;
        this.genericInterval2 = gInterval2;
        this.genericInterval3 = gInterval3;
        this.genericInterval4 = gInterval4;
        this.monthlyProfileConfig = profileConfig;
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

	public int getGenericInterval1() {
		return genericInterval1;
	}

	public void setGenericInterval1(int genericInterval1) {
		this.genericInterval1 = genericInterval1;
	}

	public int getGenericInterval2() {
		return genericInterval2;
	}

	public void setGenericInterval2(int genericInterval2) {
		this.genericInterval2 = genericInterval2;
	}

	public byte[] getGenericInterval3() {
		return genericInterval3;
	}

	public void setGenericInterval3(byte[] bs) {
		this.genericInterval3 = bs;
	}

	public byte[] getGenericInterval4() {
		return genericInterval4;
	}

	public void setGenericInterval4(byte[] bs) {
		this.genericInterval4 = bs;
	}

	public void setMonthlyProfileConfig(ArrayList profileConfig) {
		this.monthlyProfileConfig = profileConfig;
	}
	
	public ArrayList getMonthlyProfileConfig(){
		return monthlyProfileConfig;
	}

}
