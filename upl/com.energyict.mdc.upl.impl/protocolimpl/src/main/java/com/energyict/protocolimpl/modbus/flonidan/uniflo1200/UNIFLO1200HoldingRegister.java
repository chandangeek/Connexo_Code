/**
 * UNIFLO1200HoldingRegister.java
 * 
 * Created on 15-dec-2008, 11:40:55 by jme
 * 
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;

/**
 * @author jme
 *
 */
public class UNIFLO1200HoldingRegister extends HoldingRegister {
	private int slaveID = 0;
	
	public UNIFLO1200HoldingRegister(int reg, int range, ObisCode obisCode,	String name) {
		super(reg, range, obisCode, name);
	}

	public UNIFLO1200HoldingRegister(int reg, int range, ObisCode obisCode,
			Unit unit, String name) {
		super(reg, range, obisCode, unit, name);
	}

	public UNIFLO1200HoldingRegister(int reg, int range, ObisCode obisCode,
			Unit unit) {
		super(reg, range, obisCode, unit);
	}

	public UNIFLO1200HoldingRegister(int reg, int range, ObisCode obisCode) {
		super(reg, range, obisCode);
	}

	public UNIFLO1200HoldingRegister(int reg, int range, String name) {
		super(reg, range, name);
	}

	public UNIFLO1200HoldingRegister(int reg, int range) {
		super(reg, range);
	}

	public UNIFLO1200HoldingRegister(int reg, int range, ObisCode obisCode,
			Unit unit, String name, int slaveID) {
		super(reg, range, obisCode, unit, name);
		this.slaveID = slaveID;
	}

	public void setSlaveID(int slaveID) {
		this.slaveID = slaveID;
	}

	public int getSlaveID() {
		return slaveID;
	}


	
}
