package com.energyict.protocolimpl.iec1107.as220;

import java.io.IOException;
import java.util.logging.Logger;

public class AS220ContactorController {

	private static final String CONTACTOR_OPEN 		= "0100";
	private static final String CONTACTOR_ARMED 	= "0100";
	private static final String CONTACTOR_CLOSED 	= "0100";

	private AS220Registry as220Registry = null;
	private Logger logger = null;

	/*
	 * Constructors
	 */

	public AS220ContactorController(AS220 as220) {
		if ((as220 == null) || (as220.getAS220Registry() == null)) {
			throw new IllegalArgumentException("Argument as220 or as220.getAS220Registry() cannot be null!");
		}
		this.as220Registry = as220.getAS220Registry();
		this.logger = as220.getLogger();
	}

	/*
	 * Private getters, setters and methods
	 */

	private int readContactorState() {
		return 0;
	}

	private Logger getLogger() {
		if (this.logger == null) {
			this.logger = Logger.global;
		}
		return this.logger;
	}

	private AS220Registry getAs220Registry() {
		return this.as220Registry;
	}

	/*
	 * Public methods
	 */

	public void doDisconnect() throws IOException {
		getLogger().info("************************* DISCONNECT CONTACTOR *************************");
		readContactorState();
		getAs220Registry().setRegister(AS220Registry.CONTACTOR, CONTACTOR_OPEN);
	}

	public void doArm() throws IOException {
		getLogger().info("***************************** ARM CONTACTOR ****************************");
		readContactorState();
		getAs220Registry().setRegister(AS220Registry.CONTACTOR, CONTACTOR_ARMED);
	}

	public void doConnect() throws IOException {
		getLogger().info("*************************** CONNECT CONTACTOR **************************");
		readContactorState();
		getAs220Registry().setRegister(AS220Registry.CONTACTOR, CONTACTOR_CLOSED);
	}

}
