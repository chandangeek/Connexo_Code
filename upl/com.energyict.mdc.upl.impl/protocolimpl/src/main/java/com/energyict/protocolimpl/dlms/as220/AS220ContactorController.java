package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.AbstractContactorController;

/**
 * This class is used to change the contactor state of an AS220 device
 *
 * @author jme
 */
public class AS220ContactorController extends AbstractContactorController {

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
		getAs220().getCosemObjectFactory().getDisconnector(ObisCode.fromString("0.0.96.3.10.255")).writeControlState(new TypeEnum(2));
	}

	public void doConnect() throws IOException {
		getAs220().getLogger().info("CONNECT message received");
		getAs220().getCosemObjectFactory().getDisconnector(ObisCode.fromString("0.0.96.3.10.255")).writeControlState(new TypeEnum(1));
	}

	public void doDisconnect() throws IOException {
		getAs220().getLogger().info("DISCONNECT message received");
		getAs220().getCosemObjectFactory().getDisconnector(ObisCode.fromString("0.0.96.3.10.255")).writeControlState(new TypeEnum(0));
	}

	public ContactorState getContactorState() throws IOException {
		throw new UnsupportedException("Reading the contactor state is not suported yet for this protocol.");
	}

}
