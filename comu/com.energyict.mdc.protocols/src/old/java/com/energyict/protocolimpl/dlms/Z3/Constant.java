/**
 *
 */
package com.energyict.protocolimpl.dlms.Z3;

import com.energyict.obis.ObisCode;

/**
 * @author gna
 *
 * This interface replaces the objectList from the meter. This way we do not
 * need to read it from the device, saves time and money ...
 *
 */
public interface Constant {

	ObisCode digitalOutputObisCode[] = { ObisCode.fromString("0.129.0.0.1.255"), ObisCode.fromString("0.129.0.0.2.255") }; // BOOLEANS

	// Enable or disable this state to activate the prepaid functionality
	ObisCode prepaidStateObisCode = ObisCode.fromString("1.0.96.51.17.255"); // BOOLEAN

	ObisCode prepaidSetBudgetObisCode = ObisCode.fromString("1.0.96.51.18.255"); // Signed32
	ObisCode prepaidAddBudgetObisCode = ObisCode.fromString("1.0.96.51.19.255"); // Unsigned32
	//	ScalerUnit prepaidBudgetScalerUnit = new ScalerUnit(3, Unit.get(BaseUnit.WATTHOUR)); //kWh

	ObisCode prepaidThresholdObisCode = ObisCode.fromString("1.0.96.51.20.255"); // Unsigned32
	//	ScalerUnit prepaidThresholdScalerUnit = new ScalerUnit(3, Unit.get(BaseUnit.WATTHOUR)); //kWh

	ObisCode prepaidMultiplierObisCode[] = {
			ObisCode.fromString("1.0.96.51.1.255"), // All are Signed32
			ObisCode.fromString("1.0.96.51.2.255"), ObisCode.fromString("1.0.96.51.3.255"), ObisCode.fromString("1.0.96.51.4.255"),
			ObisCode.fromString("1.0.96.51.5.255"), ObisCode.fromString("1.0.96.51.6.255"), ObisCode.fromString("1.0.96.51.7.255"),
			ObisCode.fromString("1.0.96.51.8.255") };
	//	ScalerUnit prepaidMultiplierScalerUnit = new ScalerUnit(1, Unit.get(BaseUnit.UNITLESS)); //Just unitless

	ObisCode prepaidReadFrequencyObisCode = ObisCode.fromString("1.0.96.51.16.255"); // Unsigned32
	//	ScalerUnit prepaidReadFrequencyScalerUnit = new ScalerUnit(1, Unit.get(BaseUnit.MINUTE)); //minutes

	ObisCode loadLimitStateObisCode = ObisCode.fromString("1.0.96.51.30.255"); // BOOLEAN

	ObisCode loadLimitReadFrequencyObisCode = ObisCode.fromString("1.0.96.51.31.255"); // Unsigned32
	//	ScalerUnit loadLimitReadFrequencyScalerUnit = new ScalerUnit(1, Unit.get(BaseUnit.MINUTE));	//minutes

	ObisCode loadLimitThresholdObisCode = ObisCode.fromString("1.0.96.51.32.255"); // Unsigned32
	//	ScalerUnit loadLimitThresholdScalerUnit = new ScalerUnit(3, Unit.get(BaseUnit.WATT));	//kW

	ObisCode loadLimitDurationObisCode = ObisCode.fromString("1.0.96.51.33.255"); //Unsigned32
	//	ScalerUnit loadLimitDurationScalerUnit = new ScalerUnit(1, Unit.get(BaseUnit.SECOND)); 	// Seconds

	ObisCode loadLimitOutputLogicObisCode[] = { ObisCode.fromString("1.0.96.51.40.255"), ObisCode.fromString("1.0.96.51.41.255") }; //BOOLEANS

}
