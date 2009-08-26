package com.energyict.protocolimpl.iec1107.as220;

import java.io.IOException;
import java.util.logging.Logger;

import com.energyict.protocolimpl.base.ContactorController;

public class AS220ContactorController implements ContactorController {

	private static final String CONTACTOR_OPEN 		= "0";
	private static final String CONTACTOR_ARMED 	= "1";
	private static final String CONTACTOR_CLOSED 	= "2";

	private AS220 as220 = null;

	/**
	 * Constructor for the AS220ContactorController
	 * @param as220 The AS220 protocol, used to get the AS220Registry and the logger
	 */
	public AS220ContactorController(AS220 as220) {
		if ((as220 == null) || (as220.getAS220Registry() == null)) {
			throw new IllegalArgumentException("Argument as220 or as220.getAS220Registry() cannot be null!");
		}
		this.as220 = as220;
	}

	/*
	 * Private getters, setters and methods
	 */

	private AS220 getAS220() {
		return this.as220;
	}

	/**
	 * Get the logger used in the AS220 protocol
	 * @return The AS220 logger
	 */
	private Logger getLogger() {
		return getAS220().getLogger();
	}

	/**
	 * Get the AS220Registry used in the AS220 protocol
	 * @return The AS220Registry
	 */
	private AS220Registry getAS220Registry() {
		return getAS220().getAS220Registry();
	}

	/*
	 * Public methods
	 */

	/**
	 * This command tries to switch off (disconnect) the contactor in the AS220 device.
	 * @throws IOException
	 */
	public void doDisconnect() throws IOException {
		getLogger().info("************************* DISCONNECT CONTACTOR *************************");
		getAS220Registry().setRegister(AS220Registry.CONTACTOR_REGISTER, CONTACTOR_OPEN);
	}

	/**
	 * This command tries to switch the contactor to ARMED mode for the AS220 device.
	 * The armed-status allows the customer to switch the relay back on by pressing
	 * the meter button for at least 4 seconds.
	 * @throws IOException
	 */
	public void doArm() throws IOException {
		getLogger().info("***************************** ARM CONTACTOR ****************************");
		getAS220Registry().setRegister(AS220Registry.CONTACTOR_REGISTER, CONTACTOR_ARMED);
	}

	/**
	 * This command tries to switch on (connect) the contactor in the AS220 device.
	 * @throws IOException
	 */
	public void doConnect() throws IOException {
		getLogger().info("*************************** CONNECT CONTACTOR **************************");
		getAS220Registry().setRegister(AS220Registry.CONTACTOR_REGISTER, CONTACTOR_CLOSED);
	}

}
