package com.energyict.protocolimpl.dlms.eictz3;

/**
 * Enumerates the client addressing modes.
 * 
 * @author alex
 */
public enum ClientAddressingMode {
	
	/** Different values. */
	DEFAULT(-1), ONE_BYTE(1), TWO_BYTES(2), FOUR_BYTES(4);

	/** The number of bytes used in the scheme. */
	private final int numberOfBytes;
	
	/**
	 * Create a new instance indicating the number of bytes.
	 * 
	 * @param 	numberOfBytes		The number of bytes.
	 */
	private ClientAddressingMode(final int numberOfBytes) {
		this.numberOfBytes = numberOfBytes;
	}
	
	/**
	 * Returns the number of bytes.
	 * 
	 * @return	The number of bytes.
	 */
	public final int getNumberOfBytes() {
		return this.numberOfBytes;
	}
	
	/**
	 * Gets the addressing mode specified by the property.
	 * 
	 * @param	propertyValue		The value of the property. This is the number of bytes used in the addressing mode.
	 * 
	 * @return	The corresponding addressing mode, <code>null</code> if no corresponding addressing mode found.
	 */
	public static final ClientAddressingMode getByPropertyValue(final int propertyValue) {
		for (final ClientAddressingMode mode : values()) {
			if (mode.getNumberOfBytes() == propertyValue) {
				return mode;
			}
		}
		
		return null;
	}
}
