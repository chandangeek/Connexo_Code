package com.energyict.protocolimpl.iec1107.ppm;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

/** @author fbo */

public class MeterUnit {
	
	String name = "";
	//BaseUnit baseUnit = null; // KV 22072005 unused
	Unit unit = null;
	
	public MeterUnit( String name, BaseUnit baseUnit, Unit unit ){
		this.name = name;
		// this.baseUnit = baseUnit; // KV 22072005 unused
		this.unit = unit;
	}
	 
	public String getName( ){
		return name;
	}
	
	public Unit getUnit( ){
		return unit;
	}
	
	public String toString( ){
		return name;
	}
 
}
