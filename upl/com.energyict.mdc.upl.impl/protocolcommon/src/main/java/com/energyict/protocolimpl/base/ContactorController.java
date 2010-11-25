package com.energyict.protocolimpl.base;

import java.io.IOException;

/**
 * Interface with contactor related methods
 * @author jme
 */
public interface ContactorController {

	enum ContactorState{DISCONNECTED, CONNECTED, ARMED, UNKNOWN};

	/**
	 * This command tries to switch off (disconnect) the contactor in the device.
	 * @throws IOException
	 */
	void doDisconnect() throws IOException;

	/**
	 * This command tries to switch the contactor to ARMED mode for the device.
	 * The armed-status allows the customer to switch the relay back on by pressing
	 * the meter button for at certain amount of seconds seconds.
	 * @throws IOException
	 */
	void doArm() throws IOException;

	/**
	 * This command tries to switch on (connect) the contactor in the device.
	 * @throws IOException
	 */
	void doConnect() throws IOException;

	/**
	 * This command tries to read the state of the contactor in the device.
	 * @throws IOException
	 */
	ContactorState getContactorState() throws IOException;

}