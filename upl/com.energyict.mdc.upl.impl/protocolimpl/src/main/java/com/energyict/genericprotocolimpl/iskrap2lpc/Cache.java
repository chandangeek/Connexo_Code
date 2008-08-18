/**
 * 
 */
package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.io.Serializable;

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
	private static final long serialVersionUID = 1234;
	
	public int confProgChange;
	public int loadProfilePeriod1;
	public int loadProfilePeriod2;
	
	public boolean changed;
	
	public ObjectDef[] loadProfileConfig1;
	public ObjectDef[] loadProfileConfig2;
	
	public CosemDateTime billingReadTime;
	public CosemDateTime captureObjReadTime;
	
	public Cache() {
		this(-1, -1, -1, false, null, null, null, null);
	}
	
	public Cache(int confProgChange, int loadProfilePeriod1,
			int loadProfilePeriod2, boolean changed,
			ObjectDef[] loadProfileConfig1, ObjectDef[] loadProfileConfig2,
			CosemDateTime billingReadTime, CosemDateTime captureObjReadTime) {
		super();
		this.confProgChange = confProgChange;
		this.loadProfilePeriod1 = loadProfilePeriod1;
		this.loadProfilePeriod2 = loadProfilePeriod2;
		this.changed = changed;
		this.loadProfileConfig1 = loadProfileConfig1;
		this.loadProfileConfig2 = loadProfileConfig2;
		this.billingReadTime = billingReadTime;
		this.captureObjReadTime = captureObjReadTime;
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


}
