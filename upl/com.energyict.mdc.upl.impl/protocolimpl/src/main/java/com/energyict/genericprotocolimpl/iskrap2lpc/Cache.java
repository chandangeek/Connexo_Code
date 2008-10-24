/**
 * 
 */
package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.io.Serializable;
import java.util.logging.Logger;

import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.CosemDateTime;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.ObjectDef;

/**
 * @author gna
 *
 */
public class Cache implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 123456L;
	
	public int confProgChange;
	public int loadProfilePeriod1;
	public int loadProfilePeriod2;
	
	public boolean changed;
	
	public ObjectDef[] loadProfileConfig1;
	public ObjectDef[] loadProfileConfig2;
	public ObjectDef[] loadProfileConfig3;
	public ObjectDef[] loadProfileConfig4;
	
	public CosemDateTime billingReadTime;
	public CosemDateTime captureObjReadTime;

	private int mbusCount;
	private int[] mbusPhysicalAddress;
	private int[] mbusMedium;
	private String[] mbusCustomerID;
	private long[] mbusAddress;
	private Unit[] mbusUnit;
	
	public Cache() {
		this(-1, -1, -1, false, null, null, null, null, null, null, 0, new int[MeterReadTransaction.MBUS_MAX], new int[MeterReadTransaction.MBUS_MAX], new String[MeterReadTransaction.MBUS_MAX],
				new long[MeterReadTransaction.MBUS_MAX], new Unit[MeterReadTransaction.MBUS_MAX]);
	}
	
	public Cache(int confProgChange, int loadProfilePeriod1,
			int loadProfilePeriod2, boolean changed,
			ObjectDef[] loadProfileConfig1, ObjectDef[] loadProfileConfig2,
			ObjectDef[] loadProfileConfig3, ObjectDef[] loadProfileConfig4,
			CosemDateTime billingReadTime, CosemDateTime captureObjReadTime,
			int mbusCount, int[] mbusPhysicalAddress, int[] mbusMedium, String[] mbusCustomerID,
			long[] mbusAddress, Unit[] mbusUnit) {
		super();
		this.confProgChange = confProgChange;
		this.loadProfilePeriod1 = loadProfilePeriod1;
		this.loadProfilePeriod2 = loadProfilePeriod2;
		this.changed = changed;
		this.loadProfileConfig1 = loadProfileConfig1;
		this.loadProfileConfig2 = loadProfileConfig2;
		this.loadProfileConfig3 = loadProfileConfig3;
		this.loadProfileConfig4 = loadProfileConfig4;
		this.billingReadTime = billingReadTime;
		this.captureObjReadTime = captureObjReadTime;
		this.mbusCount = mbusCount;
		this.mbusPhysicalAddress = mbusPhysicalAddress;
		this.mbusMedium = mbusMedium;
		this.mbusCustomerID = mbusCustomerID;
		this.mbusAddress = mbusAddress;
		this.mbusUnit = mbusUnit;
	}
	

	public int getConfProgChange() {
		return confProgChange;
	}

	public void setConfProgChange(int confProgChange) {
		this.confProgChange = confProgChange;
		setChanged(true);
	}

	public int getLoadProfilePeriod1() {
		return loadProfilePeriod1;
	}

	public void setLoadProfilePeriod1(int loadProfilePeriod1) {
		this.loadProfilePeriod1 = loadProfilePeriod1;
	}

	public int getLoadProfilePeriod2() {
		return loadProfilePeriod2;
	}

	public void setLoadProfilePeriod2(int loadProfilePeriod2) {
		this.loadProfilePeriod2 = loadProfilePeriod2;
	}

	public ObjectDef[] getLoadProfileConfig1() {
		return loadProfileConfig1;
	}

	public void setLoadProfileConfig1(ObjectDef[] loadProfileConfig1) {
		this.loadProfileConfig1 = loadProfileConfig1;
	}

	public ObjectDef[] getLoadProfileConfig2() {
		return loadProfileConfig2;
	}

	public void setLoadProfileConfig2(ObjectDef[] loadProfileConfig2) {
		this.loadProfileConfig2 = loadProfileConfig2;
	}

	public ObjectDef[] getLoadProfileConfig3() {
		return loadProfileConfig3;
	}

	public void setLoadProfileConfig3(ObjectDef[] loadProfileConfig3) {
		this.loadProfileConfig3 = loadProfileConfig3;
	}

	public ObjectDef[] getLoadProfileConfig4() {
		return loadProfileConfig4;
	}

	public void setLoadProfileConfig4(ObjectDef[] loadProfileConfig4) {
		this.loadProfileConfig4 = loadProfileConfig4;
	}
	
	public CosemDateTime getBillingReadTime() {
		return billingReadTime;
	}

	public void setBillingReadTime(CosemDateTime billingReadTime) {
		this.billingReadTime = billingReadTime;
	}

	public CosemDateTime getCaptureObjReadTime() {
		return captureObjReadTime;
	}

	public void setCaptureObjReadTime(CosemDateTime captureObjReadTime) {
		this.captureObjReadTime = captureObjReadTime;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public void setMbusParameters(MbusDevice[] mbusDevices) {
		mbusCount = 0;
		for(int i = 0; i < MeterReadTransaction.MBUS_MAX; i++){
			if(mbusDevices[i] != null){
				setPhysicalAddress(mbusDevices[i].getPhysicalAddress(), i);
				setCustomerID(mbusDevices[i].getCustomerID(), i);
				setMbusAddress(mbusDevices[i].getMbusAddress(), i);
				setMbusUnit(mbusDevices[i].getMbusUnit(), i);
				setMbusMedium(mbusDevices[i].getMbusMedium(), i);
				mbusCount++;
			} else {
				setPhysicalAddress(-1, i);
				setCustomerID(null, i);
				setMbusAddress(-1, i);
				setMbusUnit(null, i);
				setMbusMedium(15, i);
			}
		}
	}

	private void setMbusUnit(Unit mbusUnit, int i) {
		this.mbusUnit[i] = mbusUnit;
	}

	private void setMbusAddress(long mbusAddress, int i) {
		this.mbusAddress[i] = mbusAddress;
	}

	private void setCustomerID(String customerID, int i) {
		this.mbusCustomerID[i] = customerID;
	}

	private void setPhysicalAddress(int physicalAddress, int i) {
		this.mbusPhysicalAddress[i] = physicalAddress; 
	}

	public int getMbusDeviceCount() {
		return this.mbusCount;
	}

	public int getPhysicalAddress(int i) {
		return mbusPhysicalAddress[i];
	}

	public String getCustomerID(int i) {
		return mbusCustomerID[i];
	}

	public long getMbusAddress(int i) {
		return mbusAddress[i];
	}

	public Unit getUnit(int i) {
		return mbusUnit[i];
	}
	
	private void setMbusMedium(int medium, int i){
		this.mbusMedium[i] = medium;
	}

	public int getMbusMedium(int i){
		return this.mbusMedium[i];
	}
}
