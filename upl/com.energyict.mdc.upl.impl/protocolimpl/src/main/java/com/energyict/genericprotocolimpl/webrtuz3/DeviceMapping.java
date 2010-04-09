package com.energyict.genericprotocolimpl.webrtuz3;

/**
 * Copyrights EnergyICT
 *
 * @since 9-apr-2010 16:40:52
 * @author jme
 */
public class DeviceMapping {

	private final int from;
	private final int to;

	/**
	 *
	 */
	public DeviceMapping(int from, int to) {
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
