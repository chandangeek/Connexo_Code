package com.energyict.genericprotocolimpl.webrtukp;

import com.energyict.obis.ObisCode;

public class Constant {

	final static int MaxMbusMeters = 255;
	
	final static String MANUFACTURER = "WKP";
	
	// TODO's fill in the correct ObisCodes.
	// if we use it as a Custom property, then delete this one
	public final static ObisCode	loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
	public final static ObisCode 	mbusProfileObisCode = ObisCode.fromString("0.1.24.3.0.255");
//	final static ObisCode 	dailyObisCode 		= ObisCode.fromString("1.0.99.3.0.255");
//	final static ObisCode	monthlyObisCode		= ObisCode.fromString("1.0.99.4.0.255");
//	final static ObisCode  	eventLogObisCode	= ObisCode.fromString("1.0.");	// not used so far
	
}
