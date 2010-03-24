/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.registers;

import java.util.HashMap;
import java.util.Map;

import com.energyict.obis.ObisCode;

/**
 * Definition of a DL220Register.<br>
 * It contains an {@link ObisCode}, a startingAddress and an instanceID 
 * 
 * @author gna
 * @since 22-mrt-2010
 *
 */
public enum DL220Registers {
	
	MAINVALUETOTAL1(ObisCode.fromString("8.1.1.0.0.255"), "202.0", 1),
	MAINVALUEHT1(ObisCode.fromString("8.1.1.0.1.255"), "200.0", 1),
	MAINVALUELT1(ObisCode.fromString("8.1.1.0.2.255"), "201.0", 1),
	FLOWRATEINPUT1(ObisCode.fromString("8.1.2.0.0.255"), "210.0", 1),
	
	MAINVALUETOTAL2(ObisCode.fromString("8.2.1.0.0.255"), "202.0", 2),
	MAINVALUEHT2(ObisCode.fromString("8.2.1.0.0.255"), "200.0", 2),
	MAINVALUELT2(ObisCode.fromString("8.2.1.0.0.255"), "201.0", 2),
	FLOWRATEINPUT2(ObisCode.fromString("8.2.2.0.0.255"), "210.0", 2);
	
	/** The address in the meter */
	private final String address;
	/** The obisCode from the register */
	private final ObisCode obiscode;
	/** The ObjectInstance in the meter */
	private final int instanceID;
	
	/** Contains a list of possible {@link DL220Registers} */
	private static Map<ObisCode, DL220Registers> instances;
	
	/**
	 * Create for each value an entry in the instanceMap
	 * @return
	 */
	private static Map<ObisCode, DL220Registers> getInstances() {
		if (instances == null) {
			instances = new HashMap<ObisCode, DL220Registers>(8);
		}
		return instances;
	}

	/**
	 * Private constructor
	 * 
	 * @param obisCode
	 * 				- the {@link ObisCode} from the register
	 * 
	 * @param address
	 * 				- the address of the register in the meter
	 * 
	 * @param instance
	 * 				- the instance to call
	 * 
	 * @param meterIndex
	 * 				- the index of the meter (1 or 2)
	 * 
	 */
	private DL220Registers(ObisCode obisCode, String address, int instance){
		this.obiscode = obisCode;
		this.address = address;
		this.instanceID = instance;
		getInstances().put(obisCode, this);
	}
	
	/**
	 * Get the DL220Register(definition) for the given ObisCode
	 * 
	 * @param oc
	 * 			- the {@link ObisCode} from the register
	 * 
	 * @return the requested {@link DL220Registers}
	 */
	public static DL220Registers forObisCode(ObisCode oc){
		return instances.get(oc);
	}
	
	/**
	 * @param oc
	 * 			- the {@link ObisCode} to check
	 * 
	 * @return true if the given ObisCode is a part of the list, otherwise false
	 */
	public static boolean contains(ObisCode oc){
		return instances.containsKey(oc);
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @return the obiscode
	 */
	public ObisCode getObiscode() {
		return obiscode;
	}

	/**
	 * @return the instanceID
	 */
	public int getInstanceID() {
		return instanceID;
	}
}
