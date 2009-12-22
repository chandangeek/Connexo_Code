package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;

import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.ContactorController;

/**
 * This class is used to change the contactor state of an AS220 device
 *
 * @author jme
 */
public class AS220ContactorController implements ContactorController {

	public void doArm() throws IOException {
		// TODO Auto-generated method stub

	}

	public void doConnect() throws IOException {
		// TODO Auto-generated method stub

	}

	public void doDisconnect() throws IOException {
		// TODO Auto-generated method stub

	}

	public ContactorState getContactorState() throws IOException {
		throw new UnsupportedException("Reading the contactor state is not suported yet for this protocol.");
	}

}
