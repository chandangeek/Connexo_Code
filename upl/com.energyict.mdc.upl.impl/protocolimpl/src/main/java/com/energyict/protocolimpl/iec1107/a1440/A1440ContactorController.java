package com.energyict.protocolimpl.iec1107.a1440;

import java.io.IOException;
import java.util.logging.Logger;

import com.energyict.protocolimpl.base.ContactorController;

public class A1440ContactorController implements ContactorController {

	private static final String CONTACTOR_OPEN 		= "0";
	private static final String CONTACTOR_ARMED 	= "1";
	private static final String CONTACTOR_CLOSED 	= "2";

	private A1440 a1440 = null;

	/**
	 * Constructor for the A1440ContactorController
	 * @param a1440 The A1440 protocol, used to get the A1440Registry and the logger
	 */
	public A1440ContactorController(A1440 a1440) {
		if ((a1440 == null) || (a1440.getA1440Registry() == null)) {
			throw new IllegalArgumentException("Argument a1440 or a1440.getA1440Registry() cannot be null!");
		}
	}

	/*
	 * Private getters, setters and methods
	 */

	private A1440 getA1440() {
		return this.a1440;
	}

	/**
	 * Get the logger used in the A1440 protocol
	 * @return The A1440 logger
	 */
	private Logger getLogger() {
		return getA1440().getLogger();
	}

	/**
	 * Get the A1440Registry used in the A1440 protocol
	 * @return The A1440Registry
	 */
	private A1440Registry getA1440Registry() {
		return getA1440().getA1440Registry();
	}

	/*
	 * Public methods
	 */

	/* (non-Javadoc)
	 * @see com.energyict.protocolimpl.base.ContactorController#doDisconnect()
	 */
	public void doDisconnect() throws IOException {
		getLogger().info("************************* DISCONNECT CONTACTOR *************************");
		getA1440Registry().setRegister(A1440Registry.CONTACTOR_REGISTER, CONTACTOR_OPEN);
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocolimpl.base.ContactorController#doArm()
	 */
	public void doArm() throws IOException {
		getLogger().info("***************************** ARM CONTACTOR ****************************");
		getA1440Registry().setRegister(A1440Registry.CONTACTOR_REGISTER, CONTACTOR_ARMED);
	}

	/* (non-Javadoc)
	 * @see com.energyict.protocolimpl.base.ContactorController#doConnect()
	 */
	public void doConnect() throws IOException {
		getLogger().info("*************************** CONNECT CONTACTOR **************************");
		getA1440Registry().setRegister(A1440Registry.CONTACTOR_REGISTER, CONTACTOR_CLOSED);
	}

}
