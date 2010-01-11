package com.energyict.protocolimpl.dlms.as220.gmeter;

import com.energyict.protocolimpl.dlms.as220.AS220;

public class GMeter {

	private final GasValveController	gasValveController;

	public GMeter(AS220 as220) {
		this.gasValveController = new GasValveController(as220);
	}

	public GasValveController getGasValveController() {
		return gasValveController;
	}

}
