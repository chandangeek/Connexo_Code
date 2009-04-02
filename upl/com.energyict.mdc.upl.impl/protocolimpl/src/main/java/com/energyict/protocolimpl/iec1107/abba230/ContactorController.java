package com.energyict.protocolimpl.iec1107.abba230;

import java.io.IOException;
import java.util.logging.Logger;

public class ContactorController {

	private static final int DEBUG 							= 0;
	private static final int CONTACTOR_STATE_CONNECTED 		= 0x00;
	private static final int CONTACTOR_STATE_DISCONNECTED 	= 0x01;
	private static final int CONTACTOR_STATE_ARMED 			= 0x03;
	private static final long CONTACTOR_DELAY 				= 2000;
	private static final int CONTACTOR_RETRIES 				= 5;

	private ABBA230RegisterFactory rFactory = null;
    private Logger logger = null;
	
	/*
	 * Constructors
	 */

	public ContactorController(ABBA230RegisterFactory rFactory, Logger logger) {
		this.rFactory = rFactory;
		this.logger = logger;
	}

	/*
	 * Private getters, setters and methods
	 */

	private int armContactor() throws IOException {
    	int contactorState = readContactorState();
    	if (contactorState != CONTACTOR_STATE_DISCONNECTED) return contactorState;
		rFactory.setRegister("ContactorStatus",new byte[]{0}); // arm the contactor for closing
    	return checkContactorState(CONTACTOR_STATE_ARMED);
	}
	
	private int connectContactor() throws IOException {
    	int contactorState = readContactorState();
    	if (contactorState != CONTACTOR_STATE_ARMED) return contactorState;
    	rFactory.setRegister("ContactorCloser",new byte[]{0}); // close the armed contactor
    	return checkContactorState(CONTACTOR_STATE_CONNECTED);
	}
	
	private int disconnectContactor() throws IOException {
    	int contactorState = readContactorState();
		if ((contactorState != CONTACTOR_STATE_CONNECTED)) return contactorState;
    	rFactory.setRegister("ContactorStatus",new byte[]{1}); // open the contactor
    	return checkContactorState(CONTACTOR_STATE_DISCONNECTED);
	}
	
	private int checkContactorState(int expected) throws IOException {
		int state = readContactorState();
		for (int i = 0; i < CONTACTOR_RETRIES; i++) {
			if (state == expected) return state;
			try {Thread.sleep(CONTACTOR_DELAY);} catch (InterruptedException e) {}
			state = readContactorState();
		}
		return state;
	}
	
	private int readContactorState() throws IOException {
		long val = ((Long)rFactory.getRegister("ContactorStatus")).longValue();
		if (DEBUG > 0) System.out.println("readContactorState: contactorStatus = " + val);
		return (int)(val & 0x00000003);
	}

	/*
	 * Public methods
	 */

	public void doConnect() throws IOException {
		logger.info("*************************** CONNECT CONTACTOR **************************");

		int state = armContactor();
		if (state == CONTACTOR_STATE_ARMED) {
			logger.info(" [CONNECT CONTACTOR] Meter set to ARMED state.");
		} else	if (state == CONTACTOR_STATE_CONNECTED) {
			logger.warning(" [CONNECT CONTACTOR] Unable to set meter to armed state. Meter is already in CONNECTED state.");
		} else if (state == CONTACTOR_STATE_DISCONNECTED) {
			logger.warning(" [CONNECT CONTACTOR] Unable to set meter to armed state. Meter is still in DISCONNECTED state.");
		}
		
		state = connectContactor();
		if (state == CONTACTOR_STATE_CONNECTED) {
			logger.info(" [CONNECT CONTACTOR] Meter connected load.");
		} else if (state == CONTACTOR_STATE_ARMED){
			String message = "Unable to connect load. Meter is still in ARMED state.";
			logger.warning(" [CONNECT CONTACTOR] " + message);
			throw new IOException(message);
		} else if (state == CONTACTOR_STATE_DISCONNECTED){
			String message = "Unable to connect load. Meter is still in DISCONNECTED state.";
			logger.warning(" [CONNECT CONTACTOR] " + message);
			throw new IOException(message);
		}

	}

	public void doDisconnect() throws IOException {
		logger.info("************************* DISCONNECT CONTACTOR *************************");
		int state = disconnectContactor();
		
		if (state == CONTACTOR_STATE_ARMED) {
			String message = "Meter is in ARMED state! Unable to disconnect an armed contactor. First connect the contactor.";
			logger.warning(" [DISCONNECT CONTACTOR] " + message);
			throw new IOException(message);
		} else if (state == CONTACTOR_STATE_CONNECTED) {
			String message = "Meter is still in CONNECTED state! Disconnect failed.";
			logger.warning(" [DISCONNECT CONTACTOR] " + message);
			throw new IOException(message);
		} else 	if (state == CONTACTOR_STATE_DISCONNECTED) {
			String message = "Meter disconnected load successfully.";
			logger.info(" [DISCONNECT CONTACTOR] " + message);
		} 
	}
	
	public void doArm() throws IOException {
		logger.info("***************************** ARM CONTACTOR ****************************");
    	if (armContactor() == CONTACTOR_STATE_CONNECTED) {
			String message = "Meter is in CONNECTED state! Unable to ARM contactor. First disconnect the contactor.";
    		logger.warning(" [ARM CONTACTOR] " + message);
    		throw new IOException(message);
    	} else if (armContactor() == CONTACTOR_STATE_DISCONNECTED) {
			String message = "Unable to ARM contactor. Meter is still in DISCONNECTED state.";
    		logger.warning(" [ARM CONTACTOR] " + message);
    		throw new IOException(message);
    	} else if (armContactor() == CONTACTOR_STATE_ARMED) {
    		logger.info(" [ARM CONTACTOR] Contactor is armed!");
    	}
	}


}
