package com.energyict.protocolimpl.dlms.eictz3;

/**
 * Enumerates the different connection modes.
 * 
 * @author 	alex
 */
public enum DLMSConnectionMode {

	/** Connection over TCP/IP. */
	TCPIP(1), 
	
	/** Connection over HDLC. */
	HDLC(0);
	
	private final int propertyValue;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	propertyValue		As these are set by properties, indicate which one.
	 */
	private DLMSConnectionMode(final int propertyValue) {
		this.propertyValue = propertyValue;
	}
	
	/**
	 * Gets the connection mode based on the value of the property.
	 * 
	 * @param 	propertyValue		The value of the property.
	 * 
	 * @return	The mode corresponding to the value of the property, null if no mode corresponds to it.
	 */
	public static final DLMSConnectionMode getByPropertyValue(final int propertyValue) {
		for (final DLMSConnectionMode mode : values()) {
			if (mode.propertyValue == propertyValue) {
				return mode;
			}
		}
		
		return null;
	}
}
