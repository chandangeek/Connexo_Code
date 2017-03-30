/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.device.data.BreakerStatus;

import java.io.IOException;

/**
 * Interface with contactor related methods
 * @author jme
 */
public interface ContactorController {

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
	BreakerStatus getContactorState() throws IOException;

}