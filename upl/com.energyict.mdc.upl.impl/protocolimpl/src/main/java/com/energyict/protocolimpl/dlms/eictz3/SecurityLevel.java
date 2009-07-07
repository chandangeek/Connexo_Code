package com.energyict.protocolimpl.dlms.eictz3;

/**
 * Enumerates the different security levels.
 * 
 * @author alex
 */
public enum SecurityLevel {

	NO_AUTHENTICATION(0), LOW_LEVEL(1), HIGH_LEVEL(2);
	
	private final int propertyValue;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	propertyValue		The property value.
	 */
	private SecurityLevel(final int propertyValue) {
		this.propertyValue = propertyValue;
	}
	
	/**
	 * Returns the security level that corresponds to the given property value.
	 * 
	 * @param 	propertyValue		The property value.
	 * 
	 * @return	The matching {@link SecurityLevel}, <code>null</code> if none matches.
	 */
	public static final SecurityLevel getByPropertyValue(final int propertyValue) {
		for (final SecurityLevel level : values()) {
			if (level.propertyValue == propertyValue) {
				return level;
			}
		}
		
		return null;
	}

	/**
	 * Returns the property value associated with the level.
	 * 
	 * @return	The property value associated with the level.
	 */
	public final int getPropertyValue() {
		return this.propertyValue;
	}
}
