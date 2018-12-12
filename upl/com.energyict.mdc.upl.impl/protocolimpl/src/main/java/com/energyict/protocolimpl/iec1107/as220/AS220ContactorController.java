package com.energyict.protocolimpl.iec1107.as220;

import com.energyict.protocolimpl.base.AbstractContactorController;

import java.io.IOException;
import java.util.logging.Logger;

class AS220ContactorController extends AbstractContactorController {

	private static final String	CONTACTOR_OPEN		= "0";
	private static final String	CONTACTOR_ARMED		= "1";
	private static final String	CONTACTOR_CLOSED	= "2";

	AS220ContactorController(AS220 as220) {
		super(as220);
		if ((as220 == null) || (as220.getAS220Registry() == null)) {
			throw new IllegalArgumentException("Argument as220 or as220.getAS220Registry() cannot be null!");
		}
	}

	private AS220 getAS220() {
		return (AS220) getProtocol();
	}

	private Logger getLogger() {
		return getAS220().getLogger();
	}

	private AS220Registry getAS220Registry() {
		return getAS220().getAS220Registry();
	}

	@Override
	public void doDisconnect() throws IOException {
		getLogger().info("************************* DISCONNECT CONTACTOR *************************");
		getAS220Registry().setRegister(AS220Registry.CONTACTOR_REGISTER, CONTACTOR_OPEN);
	}

	@Override
	public void doArm() throws IOException {
		getLogger().info("***************************** ARM CONTACTOR ****************************");
		getAS220Registry().setRegister(AS220Registry.CONTACTOR_REGISTER, CONTACTOR_ARMED);
	}

	@Override
	public void doConnect() throws IOException {
		getLogger().info("*************************** CONNECT CONTACTOR **************************");
		getAS220Registry().setRegister(AS220Registry.CONTACTOR_REGISTER, CONTACTOR_CLOSED);
	}

}