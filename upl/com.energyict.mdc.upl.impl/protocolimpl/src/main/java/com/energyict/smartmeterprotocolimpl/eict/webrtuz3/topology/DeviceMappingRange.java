package com.energyict.smartmeterprotocolimpl.eict.webrtuz3.topology;

/**
 * Copyrights EnergyICT
 *
 * @since 9-apr-2010 16:40:52
 * @author jme
 */
public class DeviceMappingRange {

	private final int from;
	private final int to;

	/**
	 *
	 */
	public DeviceMappingRange(int from, int to) {
		this.from = from;
		this.to = to;
	}

    /**
	 * @return the from
	 */
	public int getFrom() {
		return from;
	}

	/**
	 * @return the to
	 */
	public int getTo() {
		return to;
	}

	public int getNumberOfDevices() {
		return to - from;
	}

}
