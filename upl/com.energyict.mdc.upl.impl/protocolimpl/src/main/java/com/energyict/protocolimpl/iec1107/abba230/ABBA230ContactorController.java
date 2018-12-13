package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.base.AbstractContactorController;

import java.io.IOException;
import java.util.logging.Logger;

class ABBA230ContactorController extends AbstractContactorController {

	private static final int	CONTACTOR_STATE_CONNECTED		= 0x00;
	private static final int	CONTACTOR_STATE_DISCONNECTED	= 0x01;
	private static final int	CONTACTOR_STATE_ARMED			= 0x03;
	private static final long	CONTACTOR_DELAY					= 2000;
	private static final int	CONTACTOR_RETRIES				= 5;

	ABBA230ContactorController(ABBA230 protocol) {
		super(protocol);
	}

	private Logger getLogger() {
		return ((ABBA230) getProtocol()).getLogger();
	}

	private ABBA230RegisterFactory getRegisterFactory() {
		return ((ABBA230) getProtocol()).getRegisterFactory();
	}

	private int armContactor() throws IOException {
		int contactorState = readContactorState();
		if (contactorState != CONTACTOR_STATE_DISCONNECTED) {
			return contactorState;
		}
		getRegisterFactory().setRegister("ContactorStatus", new byte[] { 0 }); // arm the contactor for closing
		return checkContactorState(CONTACTOR_STATE_ARMED);
	}

	private int connectContactor() throws IOException {
		int contactorState = readContactorState();
		if (contactorState != CONTACTOR_STATE_ARMED) {
			return contactorState;
		}
		getRegisterFactory().setRegister("ContactorCloser", new byte[] { 0 }); // close the armed contactor
		return checkContactorState(CONTACTOR_STATE_CONNECTED);
	}

	private int disconnectContactor() throws IOException {
		int contactorState = readContactorState();
		if ((contactorState != CONTACTOR_STATE_CONNECTED)) {
			return contactorState;
		}
		getRegisterFactory().setRegister("ContactorStatus", new byte[] { 1 }); // open the contactor
		return checkContactorState(CONTACTOR_STATE_DISCONNECTED);
	}

	private int checkContactorState(int expected) throws IOException {
		int state = readContactorState();
		for (int i = 0; i < CONTACTOR_RETRIES; i++) {
			if (state == expected) {
				return state;
			}
			try {
				Thread.sleep(CONTACTOR_DELAY);
			} catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw ConnectionCommunicationException.communicationInterruptedException(e);
            }
			state = readContactorState();
		}
		return state;
	}

	private int readContactorState() throws IOException {
		long val = ((Long) getRegisterFactory().getRegister("ContactorStatus")).longValue();
		return (int) (val & 0x00000003);
	}

	@Override
	public void doConnect() throws IOException {
		getLogger().info("*************************** CONNECT CONTACTOR **************************");

		int state = armContactor();
		if (state == CONTACTOR_STATE_ARMED) {
			getLogger().info(" [CONNECT CONTACTOR] Meter set to ARMED state.");
		} else if (state == CONTACTOR_STATE_CONNECTED) {
			getLogger().warning(" [CONNECT CONTACTOR] Unable to set meter to armed state. Meter is already in CONNECTED state.");
		} else if (state == CONTACTOR_STATE_DISCONNECTED) {
			getLogger().warning(" [CONNECT CONTACTOR] Unable to set meter to armed state. Meter is still in DISCONNECTED state.");
		}

		state = connectContactor();
		if (state == CONTACTOR_STATE_CONNECTED) {
			getLogger().info(" [CONNECT CONTACTOR] Meter connected load.");
		} else if (state == CONTACTOR_STATE_ARMED) {
			String message = "Unable to connect load. Meter is still in ARMED state.";
			getLogger().warning(" [CONNECT CONTACTOR] " + message);
			throw new IOException(message);
		} else if (state == CONTACTOR_STATE_DISCONNECTED) {
			String message = "Unable to connect load. Meter is still in DISCONNECTED state.";
			getLogger().warning(" [CONNECT CONTACTOR] " + message);
			throw new IOException(message);
		}

	}

	@Override
	public void doDisconnect() throws IOException {
		getLogger().info("************************* DISCONNECT CONTACTOR *************************");
		int state = disconnectContactor();

		if (state == CONTACTOR_STATE_ARMED) {
			String message = "Meter is in ARMED state! Unable to disconnect an armed contactor. First connect the contactor.";
			getLogger().warning(" [DISCONNECT CONTACTOR] " + message);
			throw new IOException(message);
		} else if (state == CONTACTOR_STATE_CONNECTED) {
			String message = "Meter is still in CONNECTED state! Disconnect failed.";
			getLogger().warning(" [DISCONNECT CONTACTOR] " + message);
			throw new IOException(message);
		} else if (state == CONTACTOR_STATE_DISCONNECTED) {
			String message = "Meter disconnected load successfully.";
			getLogger().info(" [DISCONNECT CONTACTOR] " + message);
		}
	}

	@Override
	public void doArm() throws IOException {
		getLogger().info("***************************** ARM CONTACTOR ****************************");
		if (armContactor() == CONTACTOR_STATE_CONNECTED) {
			String message = "Meter is in CONNECTED state! Unable to ARM contactor. First disconnect the contactor.";
			getLogger().warning(" [ARM CONTACTOR] " + message);
			throw new IOException(message);
		} else if (armContactor() == CONTACTOR_STATE_DISCONNECTED) {
			String message = "Unable to ARM contactor. Meter is still in DISCONNECTED state.";
			getLogger().warning(" [ARM CONTACTOR] " + message);
			throw new IOException(message);
		} else if (armContactor() == CONTACTOR_STATE_ARMED) {
			getLogger().info(" [ARM CONTACTOR] Contactor is armed!");
		}
	}

}
