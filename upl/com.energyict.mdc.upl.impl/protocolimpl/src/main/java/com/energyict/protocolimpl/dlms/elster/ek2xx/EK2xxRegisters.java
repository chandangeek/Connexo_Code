package com.energyict.protocolimpl.dlms.elster.ek2xx;

import java.util.HashMap;
import java.util.Map;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;

public class EK2xxRegisters {

	private static final int DEBUG 	= 0;
	private Map registers = new HashMap(0);
	
	/*
	 * Constructors
	 */

	public EK2xxRegisters() {
		
		add("7.0.0.2.11.255", "Pressure sensor, serial no");
		add("7.0.0.2.12.255", "Temperature sensor, serial no");
		add("7.0.0.8.1.255", "Recording interval 1 for profile");
		
		add("7.0.13.0.0.255", "Ch. 0 Fwd abs conv volume Act value at meas conditions");
		add("7.0.13.2.0.255", "Ch. 0 Fwd abs conv volume Value at base conditions");
		add("7.0.43.0.0.255", "Ch. 0 Fwd logger conv volume Act value at meas conditions");
		add("7.0.43.2.0.255", "Ch. 0 Fwd logger conv volume Value at base conditions");
		
		add("7.0.41.0.0.255", "Current temperature, T");
		add("7.0.42.0.0.255", "Current pressure, P");
		add("7.0.53.0.0.255", "Compressibility, Z");
		add("7.0.97.97.0.255", "Error object #1");
		
		add("7.1.0.0.1.255", "Gas ID #2");
		
	}

	/*
	 * Private getters, setters and methods
	 */

	private void add(String obisString, String description) {
		registers.put(ObisCode.fromString(obisString), new RegisterInfo(description));
	}

	/*
	 * Public methods
	 */

	// TODO Auto-generated Public methods stub

	/*
	 * Public getters and setters
	 */

	public RegisterInfo getRegisterInfo(ObisCode obisCode) throws NoSuchRegisterException {
		RegisterInfo regInfo = (RegisterInfo) registers.get(obisCode);
		if (regInfo == null) throw new NoSuchRegisterException("Register with obiscode " + obisCode.toString() + " not found.");
		return regInfo;
	}
	
}
