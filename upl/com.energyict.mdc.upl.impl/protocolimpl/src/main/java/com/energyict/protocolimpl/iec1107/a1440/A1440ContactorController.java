package com.energyict.protocolimpl.iec1107.a1440;

import java.io.IOException;
import java.util.logging.Logger;

public class A1440ContactorController {

	private static final String CONTACTOR_OPEN 		= "0";
	private static final String CONTACTOR_ARMED 	= "1";
	private static final String CONTACTOR_CLOSED 	= "2";

	private A1440Registry a1440Registry = null;
	private Logger logger = null;

	/*
	 * Constructors
	 */

	public A1440ContactorController(A1440 a1440) {
		if ((a1440 == null) || (a1440.getA1440Registry() == null)) {
			throw new IllegalArgumentException("Argument a1440 or a1440.getA1440Registry() cannot be null!");
		}
		this.a1440Registry = a1440.getA1440Registry();
		this.logger = a1440.getLogger();
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

	private A1440Registry getA1440Registry() {
		return this.a1440Registry;
	}

	/*
	 * Public methods
	 */

	public void doDisconnect() throws IOException {
		getLogger().info("************************* DISCONNECT CONTACTOR *************************");
		readContactorState();
		getA1440Registry().setRegister(A1440Registry.CONTACTOR, CONTACTOR_OPEN);
	}

	public void doArm() throws IOException {
		getLogger().info("***************************** ARM CONTACTOR ****************************");
		readContactorState();
		getA1440Registry().setRegister(A1440Registry.CONTACTOR, CONTACTOR_ARMED);
	}

	public void doConnect() throws IOException {
		getLogger().info("*************************** CONNECT CONTACTOR **************************");
		readContactorState();
		getA1440Registry().setRegister(A1440Registry.CONTACTOR, CONTACTOR_CLOSED);
	}

}
