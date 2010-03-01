package com.energyict.protocolimpl.dlms.as220.emeter;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractContactorController;
import com.energyict.protocolimpl.dlms.as220.AS220;

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
		getAs220().getCosemObjectFactory().getDisconnector(DISCONNECTOR_OBISCODE).writeControlState(new TypeEnum(ARM));
	}

	public void doConnect() throws IOException {
		getAs220().getLogger().info("CONNECT message received");
		getAs220().getCosemObjectFactory().getDisconnector(DISCONNECTOR_OBISCODE).writeControlState(new TypeEnum(CONNECT));
	}

	public void doDisconnect() throws IOException {
		getAs220().getLogger().info("DISCONNECT message received");
		getAs220().getCosemObjectFactory().getDisconnector(DISCONNECTOR_OBISCODE).writeControlState(new TypeEnum(DISCONNECT));
	}

	public ContactorState getContactorState() throws IOException {
		TypeEnum currentState = null;
		try {
			currentState = getAs220().getCosemObjectFactory().getDisconnector(DISCONNECTOR_OBISCODE).getControlState();
		} catch (IOException e) {
		}

		if (currentState == null) {
			return ContactorState.UNKNOWN;
		} else {
			switch (currentState.getValue()) {
				case ARM:
					return ContactorState.ARMED;
				case CONNECT:
					return ContactorState.CONNECTED;
				case DISCONNECT:
					return ContactorState.DISCONNECTED;
				default:
					return ContactorState.UNKNOWN;
			}
		}
	}

}
