/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.a1440;

import com.energyict.mdc.protocol.api.device.data.BreakerStatus;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import com.energyict.protocolimpl.base.AbstractContactorController;
import com.energyict.protocolimpl.base.ContactorController;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * The {@link ContactorController} for the {@link A1440} {@link MeterProtocol}
 *
 * @author jme
 */
public class A1440ContactorController extends AbstractContactorController {

	private static final String	CONTACTOR_OPEN		= "0";
	private static final String	CONTACTOR_ARMED		= "1";
	private static final String	CONTACTOR_CLOSED	= "2";

	/**
	 * Constructor for the A1440ContactorController
	 *
	 * @param a1440 The A1440 protocol, used to get the A1440Registry and the
	 * logger
	 */
	public A1440ContactorController(A1440 a1440) {
		super(a1440);
		if ((a1440 == null) || (a1440.getA1440Registry() == null)) {
			throw new IllegalArgumentException("Argument a1440 or a1440.getA1440Registry() cannot be null!");
		}
	}

	/**
	 * Getter for the A1440 protocol, used to get the {@link Logger} and the
	 * {@link A1440Registry}
	 *
	 * @return The A1440 protocol
	 */
	private A1440 getA1440() {
		return (A1440) getProtocol();
	}

	/**
	 * Get the logger used in the A1440 protocol
	 *
	 * @return The A1440 logger
	 */
	private Logger getLogger() {
		return getA1440().getLogger();
	}

	/**
	 * Get the A1440Registry used in the A1440 protocol
	 *
	 * @return The A1440Registry
	 */
	private A1440Registry getA1440Registry() {
		return getA1440().getA1440Registry();
	}

	/*
	 * (non-Javadoc)
	 * @see com.energyict.protocolimpl.base.ContactorController#doDisconnect()
	 */
	public void doDisconnect() throws IOException {
		getLogger().info("************************* DISCONNECT CONTACTOR *************************");
		getA1440Registry().setRegister(A1440Registry.CONTACTOR_REGISTER, CONTACTOR_OPEN);
	}

	/*
	 * (non-Javadoc)
	 * @see com.energyict.protocolimpl.base.ContactorController#doArm()
	 */
	public void doArm() throws IOException {
		getLogger().info("***************************** ARM CONTACTOR ****************************");
		getA1440Registry().setRegister(A1440Registry.CONTACTOR_REGISTER, CONTACTOR_ARMED);
	}

	/*
	 * (non-Javadoc)
	 * @see com.energyict.protocolimpl.base.ContactorController#doConnect()
	 */
	public void doConnect() throws IOException {
		getLogger().info("*************************** CONNECT CONTACTOR **************************");
		getA1440Registry().setRegister(A1440Registry.CONTACTOR_REGISTER, CONTACTOR_CLOSED);
	}

	@Override
	public BreakerStatus getContactorState() throws IOException {
		String contactorState = (String) getA1440Registry().getRegister(A1440Registry.CONTACTOR_STATUS);
		try {
			int breakerStatusCode = Integer.parseInt(contactorState);
			switch (breakerStatusCode) {
				case 0:
					return BreakerStatus.DISCONNECTED;
				case 10000:
					return BreakerStatus.CONNECTED;
				case 20000:
					return BreakerStatus.ARMED;
				default:
					throw new IOException("Failed to parse the contactor status: received invalid state '" + breakerStatusCode + "'");
			}
		} catch (NumberFormatException e) {
			throw new IOException("Failed to parse the value ('" + contactorState + "')for contactor status", e);
		}
	}
}
