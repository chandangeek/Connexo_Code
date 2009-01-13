/**
 * 
 */
package com.energyict.genericprotocolimpl.Z3;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.ScalerUnit;
import com.energyict.obis.ObisCode;

/**
 * @author gna
 *
 * This interface replaces the objectList from the meter. This way we do not need to read it from the device, saves time and money ...
 *
 */
public interface Constant {
	
	public final static ObisCode digitalOutputObisCode[] = {ObisCode.fromString("0.129.0.0.1.255"), ObisCode.fromString("0.129.0.0.2.255")};
	
	// Enable or disable this state to activate the prepaid functionality
	public final static ObisCode prepaidStateObisCode = ObisCode.fromString("1.0.96.51.17.255");	 			
	
	public final static ObisCode prepaidSetBudgetObisCode = ObisCode.fromString("1.0.96.51.18.255");					
	public final static ObisCode prepaidAddBudgetObisCode = ObisCode.fromString("1.0.96.51.19.255");
	public final static ScalerUnit prepaidBudgetScalerUnit = new ScalerUnit(3, Unit.get(BaseUnit.WATTHOUR)); //kWh
	
	public final static ObisCode prepaidThresholdObisCode = ObisCode.fromString("1.0.96.51.20.255");				
	public final static ScalerUnit prepaidThresholdScalerUnit = new ScalerUnit(3, Unit.get(BaseUnit.WATTHOUR)); //kWh
	
	public final static ObisCode prepaidMultiplierObisCode[] = {ObisCode.fromString("1.0.96.51.1.255"),
																ObisCode.fromString("1.0.96.51.2.255"),
																ObisCode.fromString("1.0.96.51.3.255"),
																ObisCode.fromString("1.0.96.51.4.255"),
																ObisCode.fromString("1.0.96.51.5.255"),
																ObisCode.fromString("1.0.96.51.6.255"),
																ObisCode.fromString("1.0.96.51.7.255"),
																ObisCode.fromString("1.0.96.51.8.255")};		
	public final static ScalerUnit prepaidMultiplierScalerUnit = new ScalerUnit(1, Unit.get(BaseUnit.UNITLESS)); //Just unitless
	
	public final static ObisCode prepaidReadFrequencyObisCode = ObisCode.fromString("1.0.96.51.16.255"); 	
	public final static ScalerUnit prepaidReadFrequencyScalerUnit = new ScalerUnit(1, Unit.get(BaseUnit.MINUTE)); //minutes
	
	public final static ObisCode loadLimitStateObisCode = ObisCode.fromString("1.0.96.51.30.255");
	
	public final static ObisCode loadLimitReadFrequencyObisCode = ObisCode.fromString("1.0.96.51.31.255");
	public final static ScalerUnit loadLimitReadFrequencyScalerUnit = new ScalerUnit(1, Unit.get(BaseUnit.MINUTE));	//minutes
	
	public final static ObisCode loadLimitThresholdObisCode = ObisCode.fromString("1.0.96.51.32.255");
	public final static ScalerUnit loadLimitThresholdScalerUnit = new ScalerUnit(3, Unit.get(BaseUnit.WATT));	//kW
	
	public final static ObisCode loadLimitDurationObisCode = ObisCode.fromString("1.0.96.51.33.255");
	public final static ScalerUnit loadLimitDurationScalerUnit = new ScalerUnit(1, Unit.get(BaseUnit.MINUTE)); 	// minutes
	
}
