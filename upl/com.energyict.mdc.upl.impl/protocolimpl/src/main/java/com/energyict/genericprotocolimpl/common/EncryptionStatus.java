package com.energyict.genericprotocolimpl.common;

import java.util.HashMap;
import java.util.Map;


/**
 * Enumeration of all supported MBus encryption states
 * @author gna
 *
 */
public enum EncryptionStatus {

	/**
	 * No encryption
	 */
	NO_ENCRYPTION(0, "No encryption is applied"),
	
	/**
	 * Received encryption keys CAS
	 */
	RECEIVED_ENCRYPTION_KEYS(1, "E-meter received encryption keys"),
	
	/**
	 * Sent encryption keys to the Mbus device
	 */
	SENT_ENCRYPTION_KEYS(2, "E-meter has sent keys to the Mbus device"),
	
	/**
	 * DES encryption is applied
	 */		
	DES_ENCRYPTION(3, "DES encryption is applied on P2"),
		
	/**
	 * AES encryption is applied
	 */
	AES_ENCRYPTION(4, "AES encryption is applied on P2");
	
	
	
	
	private String labelKey;
	private int value;
	private static Map<Integer, EncryptionStatus> instances;
	
	/**
	 * Returns the EncryptionStatus instance that matches the given value.
	 * 
	 * @param value - the value
	 * @return the matching EncryptionStatus instance or null if none matched
	 */
	public static EncryptionStatus forValue(int value){
		return getInstances().get(value);
	}
	
	private static Map<Integer, EncryptionStatus> getInstances() {
		if (instances == null) {
			instances = new HashMap<Integer, EncryptionStatus>(8);
		}
		return instances;
	}
	
	private EncryptionStatus(int value, String key){
		this.value = value;
		this.labelKey = key;
		getInstances().put(value, this);
	}
	
	/**
	 * @return getter
	 */
	public String getLabelKey() {
		return labelKey;
	}
	
	/**
	 * @return getter
	 */
	public int getValue() {
		return value;
	}
}
