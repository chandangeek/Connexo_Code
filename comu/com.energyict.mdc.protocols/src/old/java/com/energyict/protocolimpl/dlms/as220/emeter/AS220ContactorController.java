package com.energyict.protocolimpl.dlms.as220.emeter;

import com.energyict.mdc.protocol.api.device.data.BreakerStatus;

import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractContactorController;
import com.energyict.protocolimpl.base.RetryHandler;
import com.energyict.protocolimpl.dlms.as220.AS220;

import java.io.IOException;

/**
 * This class is used to change the contactor state of an AS220 device
 *
 * @author jme
 */
public class AS220ContactorController extends AbstractContactorController {

	public static final ObisCode	DISCONNECTOR_OBISCODE	= ObisCode.fromString("0.0.96.3.10.255");

	private static final int DISCONNECT	= 0;
	private static final int CONNECT	= 1;
	private static final int ARM		= 2;

	public AS220ContactorController(AS220 protocol) {
		super(protocol);
	}

	/**
	 * Getter for the {@link AS220}
	 *
	 * @return the parent {@link AS220}
	 */
	public AS220 getAs220() {
		return (AS220) getProtocol();
	}

	public void doArm() throws IOException {
		getAs220().getLogger().info("ARM message received");
		RetryHandler rh = new RetryHandler();
		do {
			try {
				getAs220().getCosemObjectFactory().getDisconnector(DISCONNECTOR_OBISCODE).remoteReconnect();
				return;
			} catch (DataAccessResultException e) {
				rh.logFailure(e, "Error while trying to perform a remote remoteReconnect on the eMeter.");
			}
		} while (true);
	}

	public void doConnect() throws IOException {
		getAs220().getLogger().info("CONNECT message received");
		RetryHandler rh = new RetryHandler();
		do {
			try {
				getAs220().getCosemObjectFactory().getDisconnector(DISCONNECTOR_OBISCODE).remoteReconnect();
				return;
			} catch (DataAccessResultException e) {
				rh.logFailure(e, "Error while trying to perform a remote remoteReconnect on the eMeter.");
			}
		} while (true);
	}

	public void doDisconnect() throws IOException {
		getAs220().getLogger().info("DISCONNECT message received");
		RetryHandler rh = new RetryHandler();
		do {
			try {
				getAs220().getCosemObjectFactory().getDisconnector(DISCONNECTOR_OBISCODE).remoteDisconnect();
				return;
			} catch (DataAccessResultException e) {
				rh.logFailure(e, "Error while trying to perform a remote remoteDisconnect on the eMeter.");
			}
		} while (true);
	}

	public BreakerStatus getContactorState() throws IOException {
		TypeEnum currentState = null;
		try {
			currentState = getAs220().getCosemObjectFactory().getDisconnector(DISCONNECTOR_OBISCODE).readControlState();
		} catch (IOException e) {
		}

		if (currentState == null) {
			throw new IOException("Failed to parse the contactor status");
		} else {
			switch (currentState.getValue()) {
				case ARM:
					return BreakerStatus.ARMED;
				case CONNECT:
					return BreakerStatus.CONNECTED;
				case DISCONNECT:
					return BreakerStatus.DISCONNECTED;
				default:
					throw new IOException("Failed to parse the contactor status: received invalid state '" + currentState.getValue() + "'");
			}
		}
	}

}
