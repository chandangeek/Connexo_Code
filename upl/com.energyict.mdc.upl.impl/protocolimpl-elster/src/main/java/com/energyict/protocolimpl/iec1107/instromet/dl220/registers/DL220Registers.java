/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.registers;

import com.energyict.obis.ObisCode;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of a DL220Register.<br>
 * It contains an {@link ObisCode}, a startingAddress and an instanceID 
 * 
 * @author gna
 * @since 22-mrt-2010
 *
 */
public enum DL220Registers {
	
	//TODO add the minimum values
	
	MAINVALUETOTAL1(ObisCode.fromString("8.1.1.0.0.255"), "203.0", 1, false),
	MAINVALUEHT1(ObisCode.fromString("8.1.1.0.1.255"), "200.0", 1, false),
	MAINVALUELT1(ObisCode.fromString("8.1.1.0.2.255"), "201.0", 1, false),
	FLOWRATEINPUT1(ObisCode.fromString("8.1.2.0.0.255"), "210.0", 1, false),
	MAXMESPERIOD1(ObisCode.fromString("8.1.1.5.1.255"), "160.0", 3, true),
	MAXMESDAY1(ObisCode.fromString("8.1.1.5.2.255"), "160.0", 4, true),
	
	MAINVALUETOTAL2(ObisCode.fromString("8.2.1.0.0.255"), "203.0", 2, false),
	MAINVALUEHT2(ObisCode.fromString("8.2.1.0.1.255"), "200.0", 2, false),
	MAINVALUELT2(ObisCode.fromString("8.2.1.0.2.255"), "201.0", 2, false),
	FLOWRATEINPUT2(ObisCode.fromString("8.2.2.0.0.255"), "210.0", 2, false),
	MAXMESPERIOD2(ObisCode.fromString("8.2.1.5.1.255"), "160.0", 7, true),
	MAXMESDAY2(ObisCode.fromString("8.2.1.5.2.255"), "160.0", 8, true);
	
	/** The address in the meter */
	private final String address;
	/** The obisCode from the register */
	private final ObisCode obiscode;
	/** The ObjectInstance in the meter */
	private final int instanceID;
	/** Indication if it is a MaximumDemand register */
	private final boolean maxDemand;
	
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
	private DL220Registers(ObisCode obisCode, String address, int instance, boolean maxDemand){
		this.obiscode = obisCode;
		this.address = address;
		this.instanceID = instance;
		this.maxDemand = maxDemand;
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
	 * @param oc
	 * 			- the {@link ObisCode} to check
	 * 
	 * @return true if the given ObisCode is in the list AND is a maximum Demand Register 
	 */
	public static boolean containsMaxDemandRegister(ObisCode oc){
		if(instances.containsKey(oc)){
			if(forObisCode(oc).isMaxDemand()){
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
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
	
	/**
	 * @return true if it is a maximum demand register
	 */
	public boolean isMaxDemand(){
		return maxDemand;
	}
}
