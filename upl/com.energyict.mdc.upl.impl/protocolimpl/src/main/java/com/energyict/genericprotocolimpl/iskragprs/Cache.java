/**
 * 
 */
package com.energyict.genericprotocolimpl.iskragprs;

import java.io.Serializable;
import java.util.ArrayList;

import com.energyict.cbo.Unit;
import com.energyict.dlms.UniversalObject;

/**
 * @author kvd
 *
 */
public class Cache implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1234L;
	/**
	 * 
	 */
	UniversalObject[] objectList;
    int confProgChange;
    boolean changed;
    
	public int genericInterval1;
	public int genericInterval2;
	public byte[] genericInterval3;
	public byte[] genericInterval4;
	public ArrayList monthlyProfileConfig;
	
	private int mbusCount = -1;
	private int[] mbusPhysicalAddress = new int[IskraMx37x.MBUS_MAX];
	private int[] mbusMedium = new int[IskraMx37x.MBUS_MAX];
	private String[] mbusCustomerID = new String[IskraMx37x.MBUS_MAX];
	private long[] mbusAddress = new long[IskraMx37x.MBUS_MAX];
	private Unit[] mbusUnit = new Unit[IskraMx37x.MBUS_MAX];

	/**
	 * 
	 */
	public Cache() {
		this(null, -1, false, -1, -1, null, null, null,
				-1, new int[IskraMx37x.MBUS_MAX], new int[IskraMx37x.MBUS_MAX], 
				new long[IskraMx37x.MBUS_MAX], new Unit[IskraMx37x.MBUS_MAX], new String[IskraMx37x.MBUS_MAX]);
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
    		int gInterval2, byte[] gInterval3, byte[] gInterval4, ArrayList profileConfig,
    		int mbusCount, int[] mbusPhysicalAddress, int[] mbusMedium,
    		long[] mbusAddress, Unit[] mbusUnit, String[] mbusCustomerID) {
        this.objectList=objectList;
        this.confProgChange=confProgChange;
        this.changed=changed;
        this.genericInterval1 = gInterval1;
        this.genericInterval2 = gInterval2;
        this.genericInterval3 = gInterval3;
        this.genericInterval4 = gInterval4;
        this.monthlyProfileConfig = profileConfig;
        this.mbusCount = mbusCount;
        this.mbusPhysicalAddress = mbusPhysicalAddress;
        this.mbusMedium = mbusMedium;
        this.mbusCustomerID = mbusCustomerID;
        this.mbusAddress = mbusAddress;
        this.mbusUnit = mbusUnit;
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

	public int getMbusCount() {
		return mbusCount;
	}

	public void setMbusCount(int mbusCount) {
		this.mbusCount = mbusCount;
	}

	public int getMbusPhysicalAddress(int i) {
		if(this.mbusPhysicalAddress == null){
			this.mbusPhysicalAddress = new int[IskraMx37x.MBUS_MAX];
		}
		return mbusPhysicalAddress[i];
	}

	public void setMbusPhysicalAddress(int mbusPhysicalAddress, int i) {
		if(this.mbusPhysicalAddress == null){
			this.mbusPhysicalAddress = new int[IskraMx37x.MBUS_MAX];
		}
		this.mbusPhysicalAddress[i] = mbusPhysicalAddress;
	}

	public int getMbusMedium(int i) {
		if(this.mbusMedium == null){
			this.mbusMedium = new int[IskraMx37x.MBUS_MAX];
		}
		return mbusMedium[i];
	}

	public void setMbusMedium(int mbusMedium, int i) {
		if(this.mbusMedium == null){
			this.mbusMedium = new int[IskraMx37x.MBUS_MAX];
		}
		this.mbusMedium[i] = mbusMedium;
	}

	public String getMbusCustomerID(int i) {
		if(this.mbusCustomerID == null){
			this.mbusCustomerID = new String[IskraMx37x.MBUS_MAX];
		}
		return mbusCustomerID[i];
	}

	public void setMbusCustomerID(String mbusCustomerID, int i) {
		if(this.mbusCustomerID == null){
			this.mbusCustomerID = new String[IskraMx37x.MBUS_MAX];
		}
		this.mbusCustomerID[i] = mbusCustomerID;
	}

	public long getMbusAddress(int i) {
		if(this.mbusAddress == null){
			this.mbusAddress = new long[IskraMx37x.MBUS_MAX];
		}
		return mbusAddress[i];
	}

	public void setMbusAddress(long mbusAddress, int i) {
		if(this.mbusAddress == null){
			this.mbusAddress = new long[IskraMx37x.MBUS_MAX];
		}
		this.mbusAddress[i] = mbusAddress;
	}

	public Unit getMbusUnit(int i) {
		if(this.mbusUnit == null){
			this.mbusUnit = new Unit[IskraMx37x.MBUS_MAX];
		}
		return mbusUnit[i];
	}

	public void setMbusUnit(Unit mbusUnit, int i) {
		if(this.mbusUnit == null){
			this.mbusUnit = new Unit[IskraMx37x.MBUS_MAX];
		}
		this.mbusUnit[i] = mbusUnit;
	}

	public void setMbusParameters(MbusDevice[] mbusDevices) {
		mbusCount = 0;
		for(int i = 0; i < IskraMx37x.MBUS_MAX; i++){
			if(mbusDevices[i] != null){
				setMbusPhysicalAddress(mbusDevices[i].getPhysicalAddress(), i);
				setMbusCustomerID(mbusDevices[i].getCustomerID(), i);
				setMbusAddress(mbusDevices[i].getMbusAddress(), i);
				setMbusUnit(mbusDevices[i].getMbusUnit(), i);
				setMbusMedium(mbusDevices[i].getMedium(), i);
				mbusCount++;
			} else {
				setMbusPhysicalAddress(-1, i);
				setMbusCustomerID(null, i);
				setMbusAddress(-1, i);
				setMbusUnit(null, i);
				setMbusMedium(15, i);
			}
		}
	}

}
