package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.base.AbstractContactorController;

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
	 * Getter for the {@link AS220} {@link MeterProtocol}
	 *
	 * @return the parent {@link AS220} {@link MeterProtocol}
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
		return ContactorState.UNKNOWN;
	}

}
