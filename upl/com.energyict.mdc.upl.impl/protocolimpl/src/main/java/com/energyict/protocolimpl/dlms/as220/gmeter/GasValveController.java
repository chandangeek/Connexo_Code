package com.energyict.protocolimpl.dlms.as220.gmeter;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.base.AbstractContactorController;
import com.energyict.protocolimpl.dlms.as220.AS220;

/**
 * This class is used to change the valve state of an G-Meter connected to the AS220 device
 *
 * @author jme
 */
public class GasValveController extends AbstractContactorController {

	public static final ObisCode	DISCONNECTOR_OBISCODE	= ObisCode.fromString("0.0.24.4.0.255");

	private static final int DISCONNECT	= 0;
	private static final int CONNECT	= 1;
	private static final int ARM		= 2;

	public GasValveController(AS220 protocol) {
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
