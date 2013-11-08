package com.energyict.protocolimpl.cynet;
/**
 * Enumerates the signal strengths.
 * 
 * @author alex
 *
 */
public enum SignalStrength {

	/** These are the different values. */
	EXCELLENT(2101, "Excellent"), GOOD(2001, "Good"), LOW(1901, "Low"), POOR(1499, "Poor"), UNKNOWN(-1, "Unknown");
	
	/** The RSSI value. */
	private final int rssiValue;
	
	/** The display name. */
	private final String displayName;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	rssiValue		
	 * @param 	displayName
	 */
	private SignalStrength(final int rssiValue, final String displayName) {
		this.rssiValue = rssiValue;
		this.displayName = displayName;
	}
	
	/**
	 * Returns the display name.
	 * 
	 * @return	The display name.
	 */
	public final String getDisplayName() {
		return this.displayName;
	}
	
	/**
	 * Returns the RSSI value returned from the board firmware.
	 * 
	 * @return	The returned RSSI value.
	 */
	public final int getRSSIValue() {
		return this.rssiValue;
	}
	
	/**
	 * Gets the signal strength by the RSSI value.
	 * 
	 * @param 		rssiValue		The value that was returned by the FW.
	 * 
	 * @return		The {@link SignalStrength} instance.
	 */
	static final SignalStrength byRSSIValue(final int rssiValue) {
		for (final SignalStrength strength : SignalStrength.values()) {
			if (strength.getRSSIValue() == rssiValue) {
				return strength;
			}
		}
		
		return SignalStrength.UNKNOWN;
	}
}
